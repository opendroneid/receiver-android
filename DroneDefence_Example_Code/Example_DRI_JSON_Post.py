def postDetection(logger, dri_detection, endpoint):
    try:
        r = requests.post(
            endpoint,
            json={
                "time": int(time.time() * 1000),
                "sensor-id": DRI_SENSOR_ID, # 1796 + F + 010 + MACADDRESS
                "position": {
                    "latitude": float(dri_detection.get('drone',{}).get('latitude',0)),
                    "longitude": float(dri_detection.get('drone',{}).get('longitude',0)),
                    "altitude": float(dri_detection.get('drone',{}).get('height',0)), #In DRI, the barometric altitude is "height".
                    "accuracy": 1.0, # +/- meters
                    "speed-horizontal": float(dri_detection.get('drone',{}).get('speed',0)),
                    "bearing": float(dri_detection.get('drone',{}).get('bearing',0)) % 360
                },
                "metadata": [
                    {
                        "key": "type",
                        "val": "drone"
                    },{
                        "key": "mac_address",
                        "val": str(dri_detection.get('drone',{}).get('mac_address',"PLACEHOLDER_MAC_ADDRESS")),
                        "type": "primary"
                    },{
                        "key": "source",
                        "val": str(dri_detection.get('drone',{}).get('mac_address',"PLACEHOLDER_MAC_ADDRESS")),
                        "type": "primary"
                    },{
                        "key": "runtime",
                        "val": str(dri_detection.get('sensor',{}).get('runtime',0)),
                        "type": "primary"
                    },{
                        "key": "registration",
                        "val": str(dri_detection.get('drone',{}).get('drone_id',"PLACEHOLDER_DRONE_ID")),
                        "type": "primary"
                    },{
                        "key": "icao",
                        "val": str(dri_detection.get('drone',{}).get('drone_id',"PLACEHOLDER_DRONE_ID")),
                        "type": "primary"
                    },{
                        "key": "sensor_latitude",
                        "val": str(gps.lat),
                        "type": "primary"
                    },{
                        "key": "sensor_longitude",
                        "val": str(gps.lon),
                        "type": "primary"
                    },{
                        "key": "Operator Location",
                        "val": f"%d, %d" % (float(dri_detection.get('base',{}).get('latitude',0)), float(dri_detection.get('base',{}).get('longitude',0))),
                        "type": "volatile"
                    },{
                        "key": "alt",
                        "val": str(dri_detection.get('drone',{}).get('altitude',0)),
                        "type": "volatile"
                    }
                ]
            },
            timeout=1.0
        )
        logger.debug(f"[postDetection] - response from {endpoint}: {r.text}")
    except Exception as e:
        logger.critical(f"[postDetection] - Error when forwarding dri packet to server: {e}")