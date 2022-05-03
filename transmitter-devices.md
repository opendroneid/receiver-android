# Transmitter Devices

This page provides a list of transmitter devices that are capable of broadcasting Direct Remote ID signals according to ASD-STAN prEN4709-02 (EU version) and ASTM F3411-19 (US version).

The list is divided into three sections: drones, add-on devices and other implementations.

## Drones / UAV

| Device        | BT 4 | BT 5 | Wi-Fi Beacon | Wi-Fi NAN | Link                                   | Notes                       |
| ------------- | ---- | ---- | ------------ | --------- | -------------------------------------- | --------------------------- |
| Parrot Anafi  | ❌   | ❌   | ✅            | ❌        | https://www.parrot.com/en/drones/anafi | FW version >= 1.8.0 required |

## Add-on Devices

| Device        | BT 4 | BT 5 | Wi-Fi Beacon | Wi-Fi NAN | Link                                                |
| ------------- | ---- | ---- | ------------ | --------- | --------------------------------------------------- |
| Aerobits idME | ✅   | ✅   | ❌            | ❌        | https://www.aerobits.pl/product/idme/               |
| DroneTag Mini | ✅   | ✅   | ❌            | ❌        | https://dronetag.cz/en/products/mini/               |
| INVOLI LEMAN  | ❌   | ❌   |              | ✅         | https://www.involi.com/products/leman-drone-tracker |

## Other Transmitter Implementations

* Linux: https://github.com/opendroneid/transmitter-linux
* Arduino / ESP32: https://github.com/sxjack/uav_electronic_ids
