package org.opendroneid.android.app;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.opendroneid.android.R;

public class HelpMenu extends DialogFragment {

    static HelpMenu newInstance() { return new HelpMenu(); }

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.help_text, container, true);

        TextView helpView = view.findViewById(R.id.bluetoothHelpText);
        String linkText =
                "All devices should be able to receive Bluetooth 4 Legacy Advertising remote ID signals.<br><br>" +
                "To receive Bluetooth 5 Long Range remote ID signals, the device must support the \"Coded Phy\" feature and also the \"Extended Advertising\" feature.<br><br>" +
                "See the claimed support for this device for these features in the options menu.<br><br>" +
                "Please be advised that even with those features supported, the device is not necessarily able to pick up the Long Range signals or it will only do that in limited time intervals.<br>";
        helpView.setText(Html.fromHtml(linkText));

        helpView = view.findViewById(R.id.beaconHelpText);
        linkText =
                "All devices running Android 6 (Marshmallow) or newer should be able to receive Wi-Fi Beacon remote ID signals.<br><br>" +
                "The reception rate can vary a lot from one device to another, depending on the HW and SW.<br><br>" +
                "The reception rate for Wi-Fi Beacon remote ID signals can to some extent be improved by disabling Wi-Fi scan <a href='https://developer.android.com/guide/topics/connectivity/wifi-scan#wifi-scan-throttling'>throttling</a>.<br><br>" +
                "Enable the <a href='https://developer.android.com/studio/debug/dev-options'>Android Developer Mode</a> by opening the Settings application.<br><br>" +
                "Find the SW information (possibly in the \"About Phone\" menu) and click 7 times on the \"Build Number\".<br><br>" +
                "Go back, find the \"Developer options\" menu, scroll to \"Wi-Fi scan throttling\" and disable it.<br>";
        helpView.setText(Html.fromHtml(linkText));
        helpView.setMovementMethod(LinkMovementMethod.getInstance());

        helpView = view.findViewById(R.id.nanHelpText);
        linkText =
                "Reception of Wi-Fi Neighbor-aware Network remote ID signals is possible only if the device has support for Wi-Fi NaN.<br><br>" +
                "See the claimed support for this device for this feature in the options menu.<br><br>";
        helpView.setText(Html.fromHtml(linkText));
        return view;
    }
}
