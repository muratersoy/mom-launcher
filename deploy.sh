#!/bin/bash
# Honor 400 Derle & Yükle Scripti

set -e

DEVICE_IP="192.168.1.18"
DEVICE_PORT="5555"
DEVICE_SERIAL="$DEVICE_IP:$DEVICE_PORT"

echo "=== 1. Honor 400 Wi-Fi ADB Bağlantısı ==="
adb connect $DEVICE_SERIAL || true

echo "=== 2. APK Derleniyor ==="
./gradlew assembleDebug

echo "=== 3. Cihaza Yükleniyor ==="
adb -s $DEVICE_SERIAL install -r app/build/outputs/apk/debug/app-debug.apk

echo "=== 4. İzinler Tanımlanıyor ==="
adb -s $DEVICE_SERIAL shell pm grant com.momlauncher android.permission.CALL_PHONE || true
adb -s $DEVICE_SERIAL shell pm grant com.momlauncher android.permission.READ_CONTACTS || true
adb -s $DEVICE_SERIAL shell pm grant com.momlauncher android.permission.WRITE_CONTACTS || true
adb -s $DEVICE_SERIAL shell pm grant com.momlauncher android.permission.READ_CALL_LOG || true
adb -s $DEVICE_SERIAL shell pm grant com.momlauncher android.permission.ANSWER_PHONE_CALLS || true
adb -s $DEVICE_SERIAL shell pm grant com.momlauncher android.permission.READ_PHONE_STATE || true
adb -s $DEVICE_SERIAL shell pm grant com.momlauncher android.permission.POST_NOTIFICATIONS || true

adb -s $DEVICE_SERIAL shell am start -a android.intent.action.MAIN -c android.intent.category.HOME -n com.momlauncher/.MainActivity

echo "✅ Tamamlandı! MomLauncher açık."
