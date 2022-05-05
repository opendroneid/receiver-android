# Transmitter Devices

This page provides a list of transmitter devices that are capable of broadcasting Direct Remote ID signals according to the ASD-STAN prEN4709-02 (EU version) and ASTM F3411 (US version) standards.

For more information about Remote ID and links to additional resources, please see the [opendroneid-core-c project](https://github.com/opendroneid/opendroneid-core-c#opendroneid-core-c).

A [list of smart phones](supported-smartphones.md) that have been tested to receive Remote ID signals is available and information about various open source receiver implementations is available [here](https://github.com/opendroneid/opendroneid-core-c#receiver-examples).

This list is work in progress and can contain wrong or misleading data.
If you want any data added, removed, corrected etc., please raise an issue or provide a pull request with the changes.

The intention of this page is not to promote or exclude any specific commercial or non-commercial devices or implementations.
It is merely meant as a helpful source of information for people interested in Remote ID.
If you have information about devices not on the list, input and updates are very welcome.

The list is divided into three sections: drones, add-on devices and other implementations.

## Drones

The rules in the US and EU are not yet mandating that drones must broadcast Remote ID signals.
Some information about the timelines can be found [here](https://github.com/opendroneid/opendroneid-core-c#timelines).

It is expected that this list will grow when getting closer to the cut-off dates.

The list is presented in alphabetical order.

| Device        | BT 4 | BT 5 | Wi-Fi Beacon | Wi-Fi NAN | Link                                   | Notes                       |
| ------------- | ---- | ---- | ------------ | --------- | -------------------------------------- | --------------------------- |
| Parrot Anafi  | ❌   | ❌   | ✅            | ❌        | https://www.parrot.com/en/drones/anafi | FW version >= 1.8.0 required |

## Add-on Devices

Add-on devices are stand-alone implementations of remote ID, intended for retro-fitting on existing drones, for those cases where it is not practical/possible to modify the HW/SW of the drone itself.

The list is presented in alphabetical order.

| Device        | BT 4 | BT 5 | Wi-Fi Beacon | Wi-Fi NAN | Link                                                |
| ------------- | ---- | ---- | ------------ | --------- | --------------------------------------------------- |
| Aerobits idME | ✅   | ✅   | ❌            | ❌        | https://www.aerobits.pl/product/idme/               |
| DroneTag Mini | ✅   | ✅   | ❌            | ❌        | https://dronetag.cz/en/products/mini/               |
| INVOLI LEMAN  | ❌   | ❌   |              | ✅         | https://www.involi.com/products/leman-drone-tracker |

## Other Transmitter Implementations

For open source transmitter example implementations, please see [here](https://github.com/opendroneid/opendroneid-core-c#transmitter-examples).
