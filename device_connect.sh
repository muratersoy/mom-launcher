#!/bin/bash
# Honor 400 Wi-Fi ADB Bağlantı Scripti

DEVICE_IP="192.168.1.18"
DEVICE_PORT="5555"
DEVICE_SERIAL="$DEVICE_IP:$DEVICE_PORT"

echo "Honor 400 cihazına bağlanılıyor ($DEVICE_SERIAL)..."
adb connect $DEVICE_SERIAL

echo "Bağlı cihazlar:"
adb devices
