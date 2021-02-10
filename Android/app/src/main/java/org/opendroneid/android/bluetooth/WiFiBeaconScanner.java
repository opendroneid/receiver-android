/*
 * Copyright (C) 2021 Skydio Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. *
 */

package org.opendroneid.android.bluetooth;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opendroneid.android.log.LogMessageEntry;
import org.opendroneid.android.log.LogWriter;

public class WiFiBeaconScanner {
  private static final int OUILen = 3;
  private static final int DriStartByteOffset = 4;
  private static final int ScanTimerInterval = 5;
  private static final int DRIOUI[] = {0x90, 0x3A, 0xE6};
  private boolean WiFiScanEnabled = true;
  private final OpenDroneIdDataManager dataManager;
  private final LogWriter logger;
  private WifiManager wifiManager;
  private Handler handler;
  Context context;
  int scanSuccess;
  int scanFailed;
  String startTime;
  CountDownTimer countDownTimer;
  boolean beaconScanDebugEnable;

  private static final String TAG = WiFiBeaconScanner.class.getSimpleName();

  @RequiresApi(api = Build.VERSION_CODES.O)
  public WiFiBeaconScanner(Context context, OpenDroneIdDataManager dataManager, LogWriter logger) {
    this.dataManager = dataManager;
    this.logger = logger;

    this.startTime = getCurrTimeStr();

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
        !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
      Toast.makeText(context, "WiFi Scanning is not supported", Toast.LENGTH_LONG).show();
      WiFiScanEnabled = false;
      return;
    }

    this.context = context;
    handler = new Handler();
    beaconScanDebugEnable = false;

    wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    if (!wifiManager.isWifiEnabled()) {
      Toast.makeText(context, "Turning WiFi ON...", Toast.LENGTH_LONG).show();
      wifiManager.setWifiEnabled(true);
    }
    IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        handleScanResults(intent);
      }
    };
    context.registerReceiver(myReceiver, filter);

    startCountDownTimer(context);
    // Kick off WiFi Scan
    startScan();

  }
  @TargetApi(Build.VERSION_CODES.R)
  void processRemoteIdVendorIE(ScanResult scanResult, ScanResult.InformationElement element) {
    ByteBuffer buf = element.getBytes();
    byte[] driOUI = new byte[OUILen];
    byte[] arr = new byte[buf.remaining()];
    buf.get(driOUI, 0, OUILen);
    if ((driOUI[0] & 0xFF) == DRIOUI[0] && (driOUI[1] & 0xFF) == DRIOUI[1] &&
            (driOUI[2] & 0xFF) == DRIOUI[2]) {
      buf.position(DriStartByteOffset);
      buf.get(arr, 0, buf.remaining());
      LogMessageEntry logMessageEntry = new LogMessageEntry();
      long timeNano = SystemClock.elapsedRealtimeNanos();
      dataManager.receiveDataWiFi(arr, scanResult.BSSID, scanResult.BSSID.hashCode(),
              scanResult.level, timeNano, logMessageEntry);
    }
  }

  @TargetApi(Build.VERSION_CODES.R)
  void handleScanResults(Intent intent){
    if (wifiManager == null) {
      Toast.makeText(context, "WiFi beacon scanner attach failed.", Toast.LENGTH_LONG).show();
      return;
    }
    boolean freshScanResult = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
    String action = intent.getAction();
    if ((freshScanResult == true) && WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
      List<ScanResult> wifiList = wifiManager.getScanResults();
      for (ScanResult scanResult : wifiList) {
        for (ScanResult.InformationElement element : scanResult.getInformationElements()) {
          if (element != null && element.getId() == 221) {
            processRemoteIdVendorIE(scanResult, element);
          }
        }
      }
      startScan();
    }
  }

  @TargetApi(Build.VERSION_CODES.O)
  public boolean startScan() {
    if (!WiFiScanEnabled) {
      return false;
    }
    boolean ret = wifiManager.startScan();
    if (ret) {
      scanSuccess++;
    } else {
      scanFailed++;
    }
    Log.d("Wifi", "start_scan:" + ret);
    printScanStats(ret);
    return ret;
  }

  @TargetApi(Build.VERSION_CODES.O)
  public void stopScan() {
    if (!WiFiScanEnabled) {
      return;
    }
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }
    Toast.makeText(context, "Stopping WiFi scanning.", Toast.LENGTH_LONG).show();
  }

  // There are 2 ways to control WiFi scan:
  // Continuous scan: Calls startSCan() from scan completion callback
  // Periodic scan: countdown timer triggers startScan after expiry of the timer.
  // If phone is debug mode and scan throttling is off, scan is triggered from onReceive() callback.
  // But if scan throttling is turned on on the phone(default setting on the phone), then scan throttling kick in.
  // In case of throttling, startScan() fails. We need timer thread to periodically kick off scanning.
  public void startCountDownTimer(Context ctx) {
    countDownTimer = new CountDownTimer(Long.MAX_VALUE, ScanTimerInterval * 1000) {
      Context context = ctx;

      // This is called after every ScanTimerInterval sec.
      public void onTick(long millisUntilFinished) {
        startScan();
      }
      public void onFinish() {
      }
    }.start();
  }

  private String getCurrTimeStr() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    String currTime = sdf.format(new Date());
    return currTime;
  }

  private void printScanStats(boolean ret){
    StringBuilder sb = new StringBuilder();
    sb.append("Started:" + startTime + " ");
    sb.append( "success:" + scanSuccess + ","
            + "failed:" + scanFailed);
    sb.append(" curr-time:" + getCurrTimeStr() + "," + " curr-status:" + ret);

    Log.d("Wifi", sb.toString());

    if (beaconScanDebugEnable) {
      Toast.makeText(context, sb, Toast.LENGTH_LONG).show();
    }
  }
  public void SetBeaconScanDebug(boolean enable){
    beaconScanDebugEnable = enable;
  }
}
