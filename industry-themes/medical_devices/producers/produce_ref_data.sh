cat ../data/medical_devices.json | kafkacat -b localhost:9092 -t MEDICAL_DEVICES_STREAM
