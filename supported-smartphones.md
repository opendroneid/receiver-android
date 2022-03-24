## Introduction

This document contains a list of compatible smartphones that have been tested for receiving the Broadcast Remote ID signals from an Unmanned Aircraft (UA) or add-on device following the European and US standards.

It is part of the documentation for the example Android Remote ID receiver application available [here](https://github.com/opendroneid/receiver-android).

The current specification of both the ASD-STAN prEN4709-02 (EU version) and the ASTM F3411-19 (US version) standards rely on wireless protocols in the unlicensed spectrum to broadcast the desired identification and telemetry data from UAs to ground observers.
Particularly, they define transport methods over Bluetooth 4 Legacy Advertising, Bluetooth 5 Long Range (Advertising Extensions over Coded PHY S8), Wi-Fi Beacon and Wi-Fi Neighbor Awareness Network (NAN).
The main reason for choosing these wireless technologies is that they meet the requirement of being supported on ordinary mobile devices.

Neither of the above standards cover the receiver side of the Broadcast Remote ID.
This document provides a quick overview of the compatibility of community tested smartphones with the specified Broadcast Remote ID technologies.

The current stage of the prEN4709-02 (per January 2021) is a finished draft at CEN Enquiry, and the final version will be published in the upcoming months.
To obtain a copy of the ASTM Remote ID standard, please visit this [link](https://www.astm.org/Standards/F3411.htm).

**Disclaimer:** The list of tested devices is not exhaustive and comes without any guarantees.
It might contain some errors and misleading information.
There might even be some changes via SW updates from the smartphone manufacturers that can either improve or degrade the receiving capabilities.
If you find any incomplete, inconsistent, or wrong information, please feel free to open a GitHub issue.

## Testing Methodology

To determine if your smartphone can receive the Broadcast Remote ID messages, a simple test methodology is described below.
Testing is now defined only for Android phones, since iOS devices up until version 14 do not support Bluetooth 5 Long Range, Wi-Fi NAN and the situation about Wi-Fi Beacon support is unclear.
Testing the Bluetooth 4 Legacy Advertising support is irrelevant, since all existing Android and iOS models support it and no devices have been found to behave unexpectedly.

Please note that there should also be an advanced testing methodology, covering some hidden pitfalls of receiver implementations.
One of the examples is the Bluetooth Long Range feature (Extended Advertisements on Coded PHY S8), since this is an optional part of the Bluetooth 5.0 specification.
Some Android phones claim to support Long Range and Extended Advertising via the Android feature flags, but in reality, they only support advertising from the phone to the others, not reading the Remote ID information (scanning).
Another known pitfall for Bluetooth is that even if the phone supports scanning the Long Range advertisements, a power-saving feature will cause the loss of the majority of the Remote ID messages.
The work on the advanced testing methodology is now in progress.

### Bluetooth 5 Long Range Advertising - Elimination Criteria

This simple test is a prerequisite for Bluetooth 5 Long Range functionality support, as it can easily discard phones that don’t have sufficient capabilities to support the Long Range feature.
However, it doesn’t provide all the necessary information to prove the Long Range support.

1. Install [nRF Connect for Mobile](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp).
2. Open the side menu and go to Device information.
3. Check if the rows for the Long Range (PHY Coded) and Extended advertisement are both labeled YES.

Both of the rows must say YES, in order for the device to be able to receive the Remote ID Bluetooth Long Range broadcast signals.

<p align="center">
    <img src="images/screen_nrf_connect_device_information.jpg" width="200">
</p>

<p align="center">
Figure 1: Example of OnePlus 8T smartphone passing the elimination criteria
</p>

### Bluetooth 5 Long Range Support - the Ability to Receive Data

Suppose the previous test did not eliminate the device from Long Range support.
In that case, it is now necessary to test the target device’s basic receiving capability.
At the moment, this information cannot be read from the OS information and must be verified by using another device that supports the Long Range.

1. Prepare the Device Under Test (DUT) for the Long Range receiving support by running [nRF Connect for Mobile](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp).
  * In the Settings menu of the app, select Scanner and set the Scanning period to 5 minutes (or to manual if needed).
2. Use another device (Master) that passed the Elimination Criteria and run [nRF Connect for Mobile](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp).
  * Alternatively: use any other device that supports Long Range advertisements (might be Remote ID add-on or Bluetooth development kit - e.g., [nRF52840 DK](https://www.nordicsemi.com/Software-and-Tools/Development-Kits/nRF52840-DK)).
Please note that device must advertise only Long Range messages. Some add-on device might advertise also Legacy connectable messages so they cannot be used for the test.
3. On the Master, go to the Advertiser tab and create a New advertising packet.
  * In options, select Advertising Extensions and in both Primary and Secondary PHY, select LE Coded (Long Range).
  * Press the "Add Record" and select "Complete Local Name".
  * Set the Interval to 160 (resulting in 100 ms).
  * Set the Tx power to 1 dBm (the maximum).
  * Leave the other fields at default.
4. On the Master, run the Advertisement by toggling the switch. Select "Until manually turned off" and "No maximum options".
  * Alternatively: Start the add-on device or run the [Bluetooth Long Range sample](https://developer.nordicsemi.com/nRF_Connect_SDK/doc/latest/nrf/samples/bluetooth/central_hr_coded/README.html).
5. On the DUT, run the scanning in the Scanner tab and try to find the Master device.
  * It will have the device name of the Master.

  * If the Master is close to the DUT, it should have significantly better RSSI than other devices in the list.

  * Click the found device and verify that the Advertising type is Advertising Extension and that both the Primary and Secondary PHYs are LE Coded.

  * Click the round icon on the left to add the device as a favorite (that adds a small star banner under the round icon).
	Then click the filter at the top and select the "Only favorites" checkbox.

  * Swipe on the device to the right in order to reveal the RSSI chart.
	If you have more favorites devices present, notice the color of the DUT.
	Find the correct color in the chart and observe whether there are gaps in the chart or whether the signals are received continuously.

  * If you are using other method than running nRF Connect on Master device, please ensure that you don't see any Legacy messages there.
If you do then your device also advertises simultaneously on Bluetooth 4 and it will not reveal the gaps in RSSI chart.
Testing with simultaneous Legacy and Long range advertisements is invalid and shouldn't be submitted.

  * Add this information to the report for the device.

<p align="center">
    <img src="images/screen_nrf_connect_new_advertising_packet.jpg" width="200">
    <img src="images/screen_nrf_connect_scanning.jpg" width="200">
    <img src="images/screen_nrf_connect_scanning_rssi.jpg" width="200">
</p>

<p align="center"> Figure 2: Screenshots clarifying the testing steps above</p>



### Wi-Fi Beacon Broadcast

The only testing solution so far seems to be to have a drone or add-on device that transmits the Wi-Fi Beacon messages and test their reception on the smartphone in OpenDroneID receiver app.

However, it is expected that all Android devices running Android 6 (Marshmallow) or later are able to receive Wi-Fi Beacon signals.

There will be a fair amount of variation in the reception frequency from one device to another, since the Android OS enforces various [throttling](https://developer.android.com/guide/topics/connectivity/wifi-scan#wifi-scan-throttling) mechanisms on Wi-Fi scanning.
In some cases it is possible to increase the scanning frequency by enabling the [Android Developer Mode](https://developer.android.com/studio/debug/dev-options) and disabling the WiFi scan throttling option.

### Wi-Fi NAN Broadcast

The easiest way to verify support is to read the Android Feature flags.

1. Install the [AIDA64](https://play.google.com/store/apps/details?id=com.finalwire.aida64) diagnostic app.
2. Open the Network menu.
3. Scroll down to the bottom and check the Wi-Fi Aware flag.

### Contributing to the Repository

#### I have a phone that isn’t listed and I want to add it.

That’s great! Please follow the methodology to verify its capabilities and create a Pull Request with additional information and screenshots from the tests proving the support.

For the Pull Request, please upload the screenshots to the `receiver_proofs/xxx_yyy` folder in the repository where `xxx` is the smartphone manufacturer (e.g. Samsung) and `yyy` is the smartphone model (e.g. `Galaxy_Note_10`).
Name the screenshots in the folder accordingly to tested capabilities.
We recommend to use the following template:

- `bt_basic.jpg` showing the result of passing the elimination criteria - screenshot from the nRF Connect Device information.
- `bt_lr_adv.jpg` showing the reception of the LR advertisements - screenshot from the nRF Connect scanning.
- `bt_lr_rssi.jpg` showing the LR messages’ continuous reception - screenshot from the nRF Connect RSSI graph with the Favorites filter turned on.
- `wifi_beacon.jpg` showing the reception of the Wi-Fi Beacon DRI messages - screenshot from the OpenDroneID app.
- `wifi_nan.jpg` showing the capabilities to receive the Wi-Fi NAN signals - screenshot from the AIDA64 app.

Then edit the table listing the devices with pass or fail icons, add the month and year of the test, and lastly put the link to the folder in the Proof column.
Alternatively, create an Issue with the necessary information and we will add it to the list.

#### I found misleading information in the list.

Well, that might happen.
Please create an Issue for that and we will do our best to inspect it.

#### Found a better way to test smartphones for Broadcast Remote ID capabilities?

Any contribution is welcome! Feel free to open an Issue so we can discuss it further.

### List of Devices and Their Capabilities

You can find the list of tested devices in the table below.
For each device, we provide either ✅ if it passed or ❌ if it failed.
Each test contains the approximate date it happened.
It is assumed that the device was tested with the latest OS version.

Special note for the Wi-Fi Beacon results. Only some devices have been tested, but it is expected that all devices running Android 6 or later are able to receive the Wi-Fi Beacon signals.
As a tentative confirmation, those devices have been marked with ➕.

Please note that most smartphones were tested in Q1 2020 and they do not contain proof screenshots.
Therefore, their functionality may have changed since.
We plan to continuously update this list and increase the reliability of information by adding screenshot evidence.

| Smartphone Model | Chipset | Android version | BT 5 LR Basic Support (Elimination criteria) | BT 5 LR Receiver Support | Wi-Fi Beacon | Wi-Fi NAN  | Proof | Note |
| ---------------- | ------- | --------------- | -------------------------------------------- | ------------------------ | ------------ | ---------- | ----- | ---- |
| Asus Zenfone 6                                   | Snapdragon 855    | 11 | ✅ 1/2021  | ✅ 7/2021  | ✅ 7/2021  | ✅ 1/2021  | [Link](receiver_proofs/Asus_Zenfone6) | Does not receive Long Range continuously (gaps of up to 5 sec) |
| Google Pixel 6                                   | Google Tensor     | 12 | ✅ 11/2021 | ❌ 11/2021 | ✅ 11/2021 | ✅ 11/2021 |      | Long range support is claimed but the signals are never received |
| Google Pixel 4/4XL                               | Snapdragon 855    | 10 |            |           |     ➕     | ✅ 1/2020  |      | |
| Google Pixel 3/3XL                               | Snapdragon 845    |  9 |            |           |     ➕     | ✅ 1/2020  |      | |
| Google Pixel 3A                                  | Snapdragon 670    | 10 | ❌ 1/2020  | ❌ 1/2020  |     ➕     | ✅ 1/2020  |      | |
| Google Pixel 2/2XL                               | Snapdragon 835    |  9 |            |            | ✅ 1/2020  | ✅ 1/2020  |      | |
| HMD Global Nokia 7.2                             | Snapdragon 660    |  9 | ❌ 1/2020  | ❌ 1/2020  |     ➕      | ❌ 1/2020  | [Link](receiver_proofs/Nokia_7_2) | |
| HMD Global Nokia 6.2                             | Snapdragon 636    |  9 | ❌ 1/2020  | ❌ 1/2020  |     ➕      | ❌ 1/2020  | [Link](receiver_proofs/Nokia_6_2) | |
| HMD Global Nokia 2.2                             | MT 6761 Helio A22 |  9 | ✅ 1/2020  | ❌ 1/2020  |     ➕      | ❌ 1/2020  | [Link](receiver_proofs/Nokia_2_2) | Long range support is claimed but the signals are never received |
| HTC one M9                                       | Snapdragon 810    |  7 | ❌ 1/2020  | ❌ 1/2020  | ❌ 1/2020  | ❌ 1/2020  |      | |
| Honor 50 5G                                      | Snapdragon 778G   | 11 | ✅ 3/2022  | ✅ 3/2022  | ✅ 3/2022  | ❌ 3/2022  | [Link](receiver_proofs/Honor_50_5g) | |
| Honor 50 Lite                                    | Snapdragon 662    | 11 | ❌ 3/2022  |            | ✅ 3/2022  | ❌ 3/2022  | [Link](receiver_proofs/Honor_50_Lite) | |
| Huawei Mate 20 Pro                               | Kirin 980         |  9 | ✅ 1/2020  | ✅ 1/2020  |     ➕      | ❌ 1/2020  |      | Receives Long Range continuously |
| Huawei Mate 20                                   | Kirin 980         |    | ✅ 11/2019 | ✅ 11/2019 |     ➕      |            |      | |
| Huawei Mate 10 Pro                               | Kirin 970         |  8 |            |            | ✅ 1/2020  |            |      | |
| Huawei Mate 9                                    | Kirin 960         |  7 |            |            | ❌ 1/2020  |            |      | |
| Huawei P30 Pro                                   | Kirin 980         | 10 | ✅ 11/2019 | ✅ 11/2019 | ✅ 1/2020  |            |      | Does this receive LR continuously or not? |
| Huawei P30                                       | Kirin 980         |    | ✅ 11/2019 | ✅ 11/2019 |     ➕      |            |      | Does this receive LR continuously or not? |
| Huawei P20 Lite                                  | Kirin 659         |  9 | ❌ 9/2021  | ❌ 9/2021  | ✅ 1/2020  |            |      | |
| Huawei P9                                        | Kirin 955         |  6 |            |            | ❌ 1/2020  |            |      | |
| Huawei P8 Lite                                   | Kirin 655         |  7 |            |            | ❌ 1/2020  |            |      | |
| Huawei Nova 5T                                   | Kirin 980         |    | ✅ 5/2020  |            |     ➕      |            |      | |
| Huawei Nova 8i                                   | Snapdragon 662    | 10 | ❌ 03/2022 |            |     ➕      |            |  [Link](receiver_proofs/Huawei_nova_8i) | |
| Huawei Honor Magic 2                             | Kirin 980         |    |            |            |    ➕      | ❌ 1/2020  |      | |
| Huawei Honor 10 lite                             | Kirin 710         |  9 |            |            | ✅ 1/2020  |            | [Link](receiver_proofs/Huawei_Honor_10_Lite) | |
| Huawei Honor View 10                             | Kirin 970         |  9 |            |            | ✅ 1/2020  |            |      | |
| Huawei Honor 8S                                  | MT 6761 Helio A22 |  9 | ✅ 1/2020  |            |     ➕      | ❌ 1/2020  | [Link](receiver_proofs/Huawei_Honor_8S) | Not tested but expect the same behavior as the Nokia 2.2 |
| Huawei Y6 Pro                                    | MT 6761 Helio A22 |  5 | ❌ 1/2020  | ❌ 1/2020   |            | ❌ 1/2020  |      | |
| Huawei MediaPad M5                               | Kirin 960s        |  9 | ❌ 1/2021  | ❌ 1/2021   | ✅ 1/2020   | ❌ 1/2021  | [Link](receiver_proofs/Huawei_MediaPad_M5) | |
| Huawei Nexus 6P                                  | Snapdragon 810    |  8 |            |            | ✅ 1/2020  |            | [Link](receiver_proofs/Huawei_Nexus_6P) | |
| LG velvet 5G                                     | Snapdragon 765G   |    |            |            |     ➕     | ✅ 1/2021  |      | |
| LG G8X                                           | Snapdragon 855    |    |            |            |     ➕     | ✅ 1/2021  |      | |
| LG G5                                            | Snapdragon 820    |  8 | ❌ 1/2021  | ❌ 1/2021   | ✅ 1/2020  | ❌ 1/2021  | [Link](receiver_proofs/LG_G5) | |
| LG V60                                           | Snapdragon 865    | 10 |            |            |     ➕     | ✅ 1/2020  |      | |
| LG Nexus 5X                                      | Snapdragon 808    |  8 |            |            | ✅ 1/2020  |            |      | |
| LG X Cam                                         | MT 6735           |  6 |            |            | ❌ 1/2020  |            |      | |
| Motorola Edge 20 Pro                             | Snapdragon 870 5G | 11 | ✅ 3/2022  | ❌ 3/2022  |      ➕     |           | [Link](receiver_proofs/Motorola_Edge_20_Pro) | Long range support is claimed but the signals are never received. Test done using LAUNCHXL-CC1352P-2 from Texas Instrument |
| Motorola One Vision                              | Exynos 9609       |  9 | ✅ 1/2020  | ❌ 1/2020  |      ➕     | ❌ 1/2020  | [Link](receiver_proofs/Motorola_One_Vision) | Long range support is claimed but the signals are never received |
| Motorola Moto G 6 plus                           | Snapdragon 630    |  9 | ❌ 1/2020  | ❌ 1/2020  |      ➕     | ❌ 1/2020  |      | |
| Nokia 9 Pureview                                 | Snapdragon 845    |  9 |            |            | ✅ 1/2020  |            |      | |
| Nokia X20                                        | Snapdragon 480    | 12 | ✅ 3/2022  | ✅ 3/2022  | ✅ 3/2022  | ✅ 3/2022  | [Link](receiver_proofs/Nokia_X20) | Long Range received partially, with long gaps (15sec) |
| OnePlus 8T                                       | Snapdragon 865    | 11 | ✅ 1/2021  | ✅ 1/2021  |      ➕     | ❌ 1/2021  | [Link](receiver_proofs/OnePlus_8T) | Long Range receive is active only part of the time |
| One Plus 7 Pro                                   | Snapdragon 855    | 10 | ✅ 1/2020  |            |     ➕      | ❌ 1/2020  | [Link](receiver_proofs/OnePlus_7_Pro) | Probably similar LR receive behavior as in One Plus 6T and 8T (unconfirmed) |
| One Plus 7T                                      | Snapdragon 855+   | 10 | ✅ 1/2020  |            |     ➕      | ❌ 1/2020  | [Link](receiver_proofs/OnePlus_7T) | Probably similar LR receive behavior as in One Plus 6T and 8T (unconfirmed) |
| One Plus 6 / 6T                                  | Snapdragon 845    | 10 | ✅ 1/2021  | ✅ 1/2021  | ✅ 1/2020  | ❌ 1/2021  | [Link](receiver_proofs/OnePlus_6), [Link](receiver_proofs/OnePlus_6T) | Long Range receive is active only part of the time |
| One Plus Nord 5G                                 | Snapdragon 765G   | 10 | ✅ 1/2021  | ✅ 1/2021  |      ➕     | ❌ 1/2021  | [Link](receiver_proofs/OnePlus_Nord_5G) | Receives Long Range continuously |
| One Plus N10 5G                                  | Snapdragon 690    | 10 | ✅ 1/2021  | ✅ 1/2021  |      ➕     | ❌ 1/2021  | [Link](receiver_proofs/OnePlus_N10_5G) | Receives Long Range continuously |
| Razer phone 2                                    | Snapdragon 845    |    |            |            |     ➕      |            |      | |
| Realme GT Master Edition                         | Snapdragon 778G   | 11 | ✅ 3/2022  | ✅ 3/2022  | ✅ 3/2022   | ❌ 3/2022  | [Link](receiver_proofs/Realme_GT_Master_Edition) | |
| Samsung Galaxy Note 10, Note 10+                 | Exynos 9825       |  9 |            |            | ✅ 4/2021 | ✅ 1/2020  | [Link](receiver_proofs/Samsung_Galaxy_Note_10), [Link](receiver_proofs/Samsung_Galaxy_Note_10+) | |
| Samsung Galaxy Note 9 (Global)                   | Exynos 9810       |    |            |            |     ➕      |            |      | |
| Samsung Galaxy Note 9 (USA, China, Japan)        | Snapdragon 845    |    |            |            |     ➕      |            |      | |
| Samsung Galaxy Note 8 (Global)                   | Exynos 8895       |  9 |            |            | ✅ 1/2020  |            |      | |
| Samsung Galaxy Note 8 (USA, China, Japan)        | Snapdragon 835    |    |            |            |     ➕      |            |      | |
| Samsung S22+                                     | Exynos 2200       | 12 | ✅ 03/2022 | ✅ 03/2022 | ✅ 03/2022 | ✅ 03/2022 | [Link](receiver_proofs/Samsung_Galaxy_S22+) | Receives Long Range continuously |
| Samsung S21, S21+, S21 Ultra                     | Exynos 2100       | 11 | ✅ 11/2021 | ✅ 11/2021 | ✅ 11/2021 | ✅ 11/2021 |      | Receives Long Range continuously |
| Samsung S20, S20+, S20 ultra (Global)            | Exynos 990        | 10 | ✅ 1/2021  | ✅ 1/2021  | ✅ 1/2020  | ✅ 1/2020  | [Link](receiver_proofs/Samsung_Galaxy_S20_Exynos) | |
| Samsung S20, S20+, S20 ultra (USA, China, Japan) | Snapdragon 865    | 10 | ✅ 2/2021  | ✅ 2/2021  |      ➕     | ✅ 2/2021  |      | Receives Long Range continuously |
| Samsung Galaxy S10, S10e, S10+, S10 5G           | Exynos 9820       | 10 | ✅ 1/2021  | ✅ 1/2021  | ✅ 1/2020  | ✅ 1/2020  | [Link](receiver_proofs/Samsung_Galaxy_S10_Exynos) | Receives Long Range continuously |
| Samsung Galaxy S9, S9+ (Global)                  | Exynos 9810       |  9 | ❌ 1/2020  | ❌ 1/2020  | ✅ 1/2020  | ✅ 1/2020  |      | |
| Samsung Galaxy S8                                | Exynos 8895       |  9 |            |            | ✅ 1/2020  |            |      | |
| Samsung Galaxy S7                                | Exynos 8890       |    |            |            | ✅ 4/2021  |            | [Link]( receiver_proofs/Samsung_Galaxy_S7) | |
| Samsung Galaxy S6                                | Exynos 7420       |    |            |            | ✅ 4/2021  |            | [Link](receiver_proofs/Samsung_Galaxy_S6) | |
| Samsung Galaxy A5                                | Snapdragon 410    |    |            |            | ✅ 1/2020  |            |      | |
| Samsung Galaxy A71                               | Snapdragon 730    | 10 | ❌ 1/2021  | ❌ 1/2021   | ✅ 9/2021  | ✅ 1/2021  | [Link](receiver_proofs/Samsung_Galaxy_A71) | |
| Samsung Galaxy A8                                | Exynos 7885       |    |            |            | ✅ 4/2021  |            | [Link](receiver_proofs/Samsung_Galaxy_A8) | |
| Samsung Galaxy Xcover Pro                        | Exynos 9611       | 10 | ❌ 1/2020  | ❌ 1/2020   |      ➕     | ❌ 1/2020  | [Link](receiver_proofs/Samsung_Galaxy_XCover_Pro) | |
| Samsung Galaxy Xcover 5                          | Exynos 850        | 11 | ✅ 03/2022 | ❌ 03/2022 | ✅ 03/2022  | ❌ 03/2022 | [Link](receiver_proofs/Samsung_Galaxy_XCover_5) | |
| Samsung Galaxy M12                               | Exynos 850        | 11 | ✅ 03/2022 | ❌ 03/2022 | ✅ 03/2022  | ❌ 03/2022 | [Link](receiver_proofs/Samsung_Galaxy_m12) | |
| Samsung Galaxy A52s                              | Snapdragon 778G   | 11 | ✅ 03/2022 | ✅ 03/2022 | ✅ 03/2022  | ✅ 03/2022 | [Link](receiver_proofs/Samsung_Galaxy_A52s)| |
| Samsung Galaxy Xcover Pro                        | Snapdragon 865    | 10 |            |            |     ➕      | ✅ 1/2020  |      | |
| Samsung Galaxy Tab S7, S7+                       | Snapdragon 865+   |    |            |            |     ➕      | ✅ 1/2021  |      | |
| Samsung Galaxy Tab S6                            | Snapdragon 855    |    | ✅ 6/2020   |            |     ➕      |            |      | |
| Samsung Galaxy A3                                | Exynos 7870       |    | ❌ 1/2021   | ❌ 1/2021   |     ➕      | ❌ 1/2021  |      | |
| Sony XQ-AD52 Xperia L4                           | MT6762 Helio P22  |    | ✅ 1/2021   | ❌ 1/2021   |     ➕      | ❌ 1/2020  |      | |
| Sony Xperia 10 III                               | Snapdragon 690    | 11 | ❌ 3/2022   |            | ✅ 3/2022  |              | [Link](receiver_proofs/Sony_Xperia_10_III) | |
| Sony Xperia 5 III                                | Snapdragon 888    | 12 | ❌ 3/2022   |            | ✅ 3/2022  |              | [Link](receiver_proofs/Sony_Xperia_5_III) | |
| Sony Xperia 5                                    | Snapdragon 855    |    |            |             | ✅ 4/2021  |             | [Link](receiver_proofs/Sony_Xperia_5) | |
| Sony Xperia XA2                                  | Snapdragon 630    |  9 | ❌ 1/2020   | ❌ 1/2020   |     ➕     | ❌ 1/2020  |      | |
| Sony Xperia XZ1 Compact                          | Snapdragon 835    |  8 |            |            | ✅ 1/2020  |            |      | |
| Sony Xperia XZ2                                  | Snapdragon 845    | 10 |            |            | ✅ 1/2020  |            |      | |
| Vivo V21                                         | Dimensity 800U    | 11 | ✅ 3/2022  | ❌  3/2022 | ✅ 3/2022  | ❌ 3/2022  | [Link](receiver_proofs/Vivo_V21) | |
| Xiaomi Note 10                                   | Snapdragon 730G   |  9 | ✅ 1/2020  |            |      ➕     | ✅ 1/2020  | [Link](receiver_proofs/Xiaomi_Mi_Note_10) | |
| Xiaomi Mi 9T Pro                                 | Snapdragon 855    |  9 | ✅ 1/2020  |            |      ➕     | ✅ 1/2020  | [Link](receiver_proofs/Xiaomi_Mi_9T_Pro) | |
| Xiaomi Mi 9 SE                                   | Snapdragon 712    |  9 | ✅ 1/2020  |            |      ➕     | ❌ 1/2020  | [Link](receiver_proofs/Xiaomi_Mi_9_SE) | |
| Xiaomi Mi 9                                      | Snapdragon 855    |  9 | ✅ 1/2020  | ✅ 1/2020   |      ➕     | ✅ 1/2020  | [Link](receiver_proofs/Xiaomi_Mi_9) | Long Range receive is active only part of the time |
| Xiaomi Mi 8                                      | Snapdragon 845    |  9 |            |            |     ➕      | ✅ 1/2020  |      | |
| Xiaomi Redmi Note 9s                             | Snapdragon 720G   |    | ✅ 6/2020  |            |      ➕     |            |      | |
| Xiaomi Redmi note 8 Pro                          | MT Helio G90T     |  9 | ✅ 1/2020  |            |      ➕     | ❌ 1/2020  | [Link](receiver_proofs/Xiaomi_Redmi_Note_8_Pro) |  |
| Xiaomi Redmi note 7 Pro                          | Snapdragon 675    |    |            |            |     ➕      |            |      | |
| Xiaomi Redmi note 8T                             | Snapdragon 665    |  9 | ❌ 1/2020  | ❌ 1/2020   |      ➕     | ❌ 1/2020  | [Link](receiver_proofs/Xiaomi_Redmi_Note_8T) | |
| Xiaomi Redmi note 7                              | Snapdragon 660    |  9 | ❌ 1/2020  | ❌ 1/2020   |      ➕     | ❌ 1/2020  | [Link](receiver_proofs/Xiaomi_Redmi_Note_7) | |
| Xiaomi Redmi CC9 Pro/Note10 Pro                  | Snapdragon 730G   | 10 |            |            |     ➕      | ✅ 1/2020  |      | |
| Xiaomi Redmi K20 Pro                             | Snapdragon 855    |  9 |            |            |     ➕      | ✅ 1/2020  |      | |
| Xiaomi Mi Mix 3                                  | Snapdragon 845    |  9 |            |            | ✅ 1/2020  | ✅ 1/2020  |      | |
| Xiaomi Mi A2                                     | Snapdragon 660    |  9 | ❌ 1/2020  | ❌ 1/2020   |       ➕    | ❌ 1/2020  | [Link](receiver_proofs/Xiaomi_Mi_A2) | |
