# ARES – Android sensör tarama prototipi

**Paket durumu:** Kaynak kodu prototipi. Henüz derlenmiş APK değildir.

## Sürüm 0.2'de bulunanlar
- Android manyetometre, ivmeölçer ve jiroskop verilerini okuma altyapısı
- Türkçe konuşma tanıma ile temel tarama komutları
- Türkçe sesli geri bildirim (cihazdaki TTS desteğine bağlı)
- Beş tarama hattı için kullanıcı akışı
- Tarama öncesi 100 örnekli referans kalibrasyonu
- Kalibrasyon kararlılığı için basit dalgalanma uyarısı
- Sensör doğruluk durumu ve zaman damgası kaydı
- Ham sensör verilerini CSV olarak kaydetme

## Henüz bulunmayanlar
- Sürekli konuşan, genel amaçlı çevrimiçi yapay zekâ
- Kamera ile kayıt
- Otomatik zemin/boşluk tanıma
- Kanal başlangıç ve bitiş tespiti
- Gerçek derinlik hesabı
- 2D/3D yer altı modeli
- RM3100 veya başka harici sensör bağlantısı

Bu özellikler sonraki aşamalarda geliştirilebilir; ancak fiziksel ölçüm verisi ve saha doğrulaması olmadan gerçek tespit gibi sunulmayacaktır.

## APK oluşturma
1. Android Studio'nun kararlı sürümünü kurun.
2. Android Studio > Open ile bu klasörü açın.
3. Android SDK Platform 35 ve JDK 17 veya Android Studio'nun önerdiği uyumlu JDK'yı kurun.
4. Gradle senkronizasyonunun bitmesini bekleyin.
5. Build > Build Bundle(s) / APK(s) > Build APK(s) seçin.
6. APK genellikle `app/build/outputs/apk/debug/app-debug.apk` altında oluşur.
7. APK'yı telefona aktarın. Kurulum sırasında Android'in istediği bilinmeyen kaynak iznini yalnızca bu APK için verin.

## Kullanım
1. Uygulamayı açıp mikrofon iznini verin.
2. “Taramayı başlat” düğmesine basın.
3. Telefonu aynı yükseklikte, aynı yönde ve sabit hızda hareket ettirin.
4. Her hat bitince “Sonraki hatta geç” düğmesine basın.
5. Beş hat tamamlanınca “Taramayı bitir ve CSV kaydet” düğmesine basın.
6. İsterseniz “Sesli komut dinle” düğmesine basarak Türkçe temel komutları kullanın.

## Teknik not
Manyetometre manyetik alanı ölçer; boşluğu doğrudan ölçmez. Metal boru, donatı, kablo, araç ve çevredeki metal nesneler ölçümü etkileyebilir. Telefonun ivmeölçer/jiroskop verileri hareket takibine yardım eder; derinliği tek başına ölçmez. Bu prototip 70 cm derinlik iddiasında bulunmaz.

Güncel geliştirme durumu: `docs/GELISTIRME_DURUMU.md`\nAyrıntılı geliştirme planı: `docs/TEKNIK_TASARIM.md`
Saha test formu: `docs/SAHA_TEST_FORMU.md`
