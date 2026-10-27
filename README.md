# MomLauncher 📱👵🧓

Yaşlılar ve büyüklerimiz için özel olarak tasarlanmış; karmaşık menülerden, küçük yazılardan ve yanlış dokunma kazalarından arındırılmış, sade ve yüksek kontrastlı modern **Android Başlatıcı (Launcher)** uygulaması.

---

## ✨ Öne Çıkan Özellikler

* **🎨 Gözü Yormayan Türk Zevkine Uygun Arayüz:** Sıcak fildişi zemin, huzur veren zümrüt yeşili, çini mavisi ve kehribar sarısı yüksek kontrastlı okunaklı renkler.
* **📞 6 Büyük Hızlı Arama Yuvası:** En sık aranan yakınlar için büyük fotoğraf ve isimli kartlar. Boş yuvaya basılı tutarak rehberden kolayca kişi atama.
* **🕌 Keçiören Namaz Vakitleri & Büyük Saat:** Güncel saat, tarih ve en yakın namaz vakti tek bakışta görünür.
* **🔔 Akıllı Cevapsız Arama Bildirimi:** Arayan kişinin ismi ve ne zaman aradığı net 2 satır halinde gösterilir. 24 saat sonra ana ekrandan otomatik olarak kaybolur. Tek dokunuşla geri arama veya boşverme imkanı sunar.
* **📖 Büyük Butonlu Temel Özellikler:**
  * **Rehber:** Büyük harf indeksli, hızlı arama destekli kolay rehber.
  * **Tuşlarla Ara:** Yaşlıların rahatça basabileceği dev rakamlı arama ekranı.
  * **Son Aramalar:** Renkli oklarla gelen, giden ve cevapsız çağrı geçmişi.
  * **Fotoğraflar:** Sadeleştirilmiş, dev kareli dahili aile fotoğraf albümü.
  * **Haberler:** Büyük puntolarla rahatça okunabilen güncel TRT Haber akışı.
* **👶 Torun Modu (Gizli Kısayol):** Sağ üstteki Namaz kartına **çift tıklandığında** cihazdaki tüm oyun ve uygulamaların listelendiği, hızlı arama çubuklu gizli "Torun Modu" açılır.

## 📥 Hazır APK İndir (Doğrudan Telefona Kur)

Kod derlemekle veya Android Studio ile uğraşmak istemiyorsanız, hazır derlenmiş en güncel APK'yı doğrudan telefonunuza indirebilirsiniz:

👉 **[MomLauncher.apk İndir (v1.0.0)](https://github.com/muratersoy/mom-launcher/raw/main/MomLauncher.apk)** *(~16 MB)*

> 💡 **Nasıl Kurulur?**
> 1. Yukarıdaki bağlantıya Android telefonunuzdan dokunup dosyayı indirin.
> 2. İndirilen dosyayı açın (varsa *"Bilinmeyen kaynaklardan yüklemeye izin ver"* onayını verin).
> 3. **"Yükle"** butonuna basarak kurulumu saniyeler içinde tamamlayın.

---

## 🚀 Kendiniz APK Üretmek İsterseniz (Alternatif Yollar)

Uygulamanın kodlarını kendiniz derleyip APK üretmek isterseniz aşağıdaki pratik yöntemleri kullanabilirsiniz:

### Yöntem 1: Terminal / Komut Satırından Tek Komutla (En Kolay Yol)

Bilgisayarınızda **Java (JDK 17)** kurulu olması yeterlidir. Android Studio açmanıza gerek yoktur.

#### 1. Proje dizininde terminali açın ve çalıştırın:
* **macOS / Linux:**
  ```bash
  ./gradlew assembleDebug
  ```
* **Windows (PowerShell / CMD):**
  ```cmd
  gradlew.bat assembleDebug
  ```

#### 2. APK Dosyanız Hazır!
Derleme 15-30 saniye içinde biter. APK dosyanız şu konumda oluşturulur:
```text
app/build/outputs/apk/debug/app-debug.apk
```

#### 3. Telefona Yükleme:
1. Bu `app-debug.apk` dosyasını telefonunuza gönderin (WhatsApp, Telegram, Google Drive, AirDrop, Bluetooth veya USB kablosuyla).
2. Telefonda dosyaya dokunun.
3. Çıkan ekranda *"Bilinmeyen kaynaklardan yüklemeye izin ver"* onayını verip **Yükle** butonuna basın.

---

### Yöntem 2: Android Studio ile Görsel Olarak APK Çıkarma

1. **Android Studio**'yu açın ve `momlauncher` klasörünü seçin.
2. Gradle senkronizasyonunun bitmesini bekleyin.
3. Üst menüden sırasıyla:
   `Build` > `Build Bundle(s) / APK(s)` > **`Build APK(s)`** seçeneğine tıklayın.
4. Derleme bitince sağ altta çıkan bildirimdeki mavi **`locate`** yazısına tıklayın. Açılan klasördeki APK yüklemeye hazırdır.

---

### Yöntem 3: Kablosuz / Kablolu ADB ile Doğrudan Telefona Yükleme (Geliştiriciler İçin)

Eğer telefonunuz bilgisayara USB kablosuyla veya Wi-Fi ADB ile bağlıysa:

```bash
# Cihaz IP'nizi deploy.sh içine yazdıktan sonra:
./deploy.sh
```
Bu script APK'yı derler, cihaza kablosuz yükler, gerekli arama/rehber izinlerini otomatik tanımlar ve launcher'ı hemen başlatır.

---

## ⚙️ Telefonda İlk Ayarlar & Tavsiyeler

1. **Varsayılan Başlatıcı (Default Launcher) Yapma:**
   * Telefonun *Ayarlar > Uygulamalar > Varsayılan Uygulamalar > Ana Ekran* menüsünden **MomLauncher**'ı seçin.
   * Böylece telefonun ana ekran tuşuna basıldığında her zaman MomLauncher açılır.
2. **Gerekli İzinleri Onaylama:**
   * İlk açılışta ana ekranda çıkan izin kartına dokunarak Rehber, Arama ve Bildirim izinlerini verin.
3. **Varsayılan Arama Uygulaması (İsteğe Bağlı):**
   * MomLauncher'ın dahili arama ekranını kullanmak için Ayarlar sayfasından *"Varsayılan Telefon Yap"* seçeneğini etkinleştirebilirsiniz.

---

## 🛠️ Teknik Altyapı

* **Platform:** Android (minSdk: 26 - Android 8.0 Oreo, targetSdk: 34 - Android 14)
* **Dil:** Kotlin 1.9.22
* **Arayüz:** Jetpack Compose (Material3, Modern Temiz Mimari)
* **Görsel Yükleme:** Coil Compose
* **Asenkron / Coroutine:** Kotlin Coroutines & StateFlow

---

## 📄 Lisans
Bu proje açık kaynaklı ve kişisel/ailevi kullanım için özgürce geliştirilebilir şekilde hazırlanmıştır.
