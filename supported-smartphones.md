## Introduction

This repository contains a list of compatible smartphones that have been tested for receiving the Direct Remote ID signals from the UA or add-on device following the European and US standards. 

The current specification of both ASD-STAN prEN4709-02 (EU version) and ASTM F3411-19 (US version) standards rely on wireless protocols in the unlicensed spectrum to get the desired identification and telemetry data from UAVs to the ground observers. Particularly, they define transport methods over Bluetooth 4 Legacy Advertising, Bluetooth 5 Long Range (Advertising Extensions over Coded PHY S8), Wi-Fi Beacon, and Wi-Fi Neighbor Awareness Network (NAN). The main reason for choosing these wireless technologies is that they are currently the only ones that meet the requirement of being available in ordinary mobile devices.

Unfortunately, both of the above standards do not cover the receiver side of the Direct Remote ID. Thus, this repository provides a quick overview of community tested smartphones compatible with the specified transport Remote ID technologies. 

The current stage of the prEN4709-02 is a finished draft at CEN Enquiry, and the final version will be published in the upcoming months. On the other hand, to obtain a copy of the ASTM Remote ID standard, please visit this [link](https://www.astm.org/Standards/F3411.htm).

**Disclaimer:** The list of tested devices is not exhaustive and comes without any guarantees. It might contain some errors and misleading information. There might even be some changes via SW updates from the smartphone manufacturers that can either improve or degrade the receiving capabilities. If you find any incomplete, inconsistent, or wrong information, please feel free to open a GitHub issue.

## Testing Methodology

To determine if your smartphone can receive the Direct Remote ID messages, a simple test methodology is described below. Testing is now defined only for Android phones because it is generally known that iOS devices currently do not support Bluetooth 5 Long Range, WiFi NAN, and the situation about Wi-Fi Beacon support is unclear. Testing the Bluetooth 4 Legacy Advertising support is irrelevant since all existing Android models support it and no devices have been found to behave unexpectedly.

Please note that there should also be an advanced testing methodology, covering some hidden pitfalls of receiver implementations. One of the examples is the Bluetooth Long Range feature (Extended Advertisements on Coded PHY S8). Some Android phones claim to support the Long Range feature by reading OS flags, but in reality, they only support advertising from the phone to the others, not reading the Remote ID information (scanning). Another known pitfall for Bluetooth is that even if the phone supports scanning the Long Range advertisements, some weird power-saving feature will cause the loss of the majority of Remote ID messages. The work on the advanced testing methodology is now in progress. 

### Bluetooth 5 Long Range Advertising - Elimination Criteria

This simple test is a prerequisite for Bluetooth 5 Long Range functionality support as it can easily discard phones that don’t have sufficient capabilities to support the Long Range feature. However, it doesn’t say all the necessary information to prove the Long Range support. 

1. Install [nRF Connect for Mobile](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp).
2. Open the side menu and go to Device information.
3. Check if the rows for the Long Range (PHY Coded) and Extended advertisement are labeled YES.

### Bluetooth 5 Long Range Support - the Ability to Receive Data

Suppose the previous test did not eliminate the device from Long Range support. In that case, it is now necessary to test the target device’s basic receiving capability. At the moment, this information cannot be read from the OS information and must be verified by using another device that supports the Long Range.

1. Prepare the device tested (DUT) for the Long Range receiving support by running [nRF Connect for Mobile](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp).
2. Gather another device (Master) that passed the Elimination Criteria and run [nRF Connect for Mobile](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp). 
	* Alternatively: gather any other device that supports Long Range advertisements (might be Remote ID add-on or Bluetooth development kit - e.g., [nRF52840 DK](https://www.nordicsemi.com/Software-and-Tools/Development-Kits/nRF52840-DK)).
3. On the Master, go to the Advertiser tab and create a New advertising packet. 
	* In options, select Advertising Extensions and in Primary and Secondary PHY, select LE Coded (Long Range). Leave the other fields default.
4. On the Master, run the Advertisement by toggling the switch. Select Until manually turned off and No maximum options.
	* Alternatively: Start the add-on device or run the [Bluetooth Long Range sample](https://developer.nordicsemi.com/nRF_Connect_SDK/doc/latest/nrf/samples/bluetooth/central_hr_coded/README.html).
5. On the DUT, run the scanning in the Scanner tab and try to find the Master device.
	* If the Master is close to the DUT, it should have significantly better RSSI (around -40 dBm).
6. If the Master device can be seen, verify in detail that the Advertising type has value LE Coded. 

Please note that this test says nothing about the possible power-saving features of the phone or Bluetooth Long Range scanning duty cycle. If the test passes, we can only be sure that the DUT somehow receives the Long Range messages, but we don’t know how reliable. Some advanced testing should further verify reliability.

### Wi-Fi Beacon Broadcast

TBD

### Wi-Fi NAN Broadcast

There is currently a lack of information about the Wi-Fi NAN implementation in mobile phones. So far, the easiest way to verify basic support is to read OS flags.

1. Install [AIDA64](https://play.google.com/store/apps/details?id=com.finalwire.aida64) diagnostic app.
2. Open Network menu.
3. Scroll down to Wi-Fi logs and check Wi-Fi Aware flag.

### Contributing to the Repository

#### I have a phone that isn’t listed and I want to add it.

That’s great! Please follow the methodology to verify its capabilities and create a Pull Request with additional information and screenshots from the tests proving the support.

#### I found misleading information in the list.

Well, that might happen. Please create an Issue for that and we will do our best to inspect it.

#### Found a better way to test smartphones for Direct Remote ID capabilities?

Any contribution is welcome! Feel free to open the Issue so we can discuss it further.

### List of Devices and Their Capabilities

TBD