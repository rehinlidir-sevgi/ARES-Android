# ARES APK derleme

Bu proje klasöründe `.github/workflows/android-apk.yml` dosyası, GitHub Actions üzerinde APK derlemek için hazırlanmıştır.

## APK'yı almak için
1. ZIP'i bilgisayara çıkarın.
2. GitHub'da yeni, boş bir repository oluşturun.
3. ZIP içindeki bütün dosyaları (gizli `.github` klasörü dahil) repository'ye yükleyip `main` dalına gönderin.
4. Repository'nin **Actions** sekmesini açın.
5. **ARES Android APK** iş akışını seçip **Run workflow** düğmesine basın. `main` dalına yükleme yapıldığında da otomatik başlar.
6. İşlem başarılı olursa çalışmanın **Artifacts** bölümünden `ARES-debug-APK` dosyasını indirin.
7. ZIP içinden `app-debug.apk` dosyasını Android telefona aktarın ve kurun. Telefon, dış kaynaktan uygulama kurma izni isteyebilir.

## Önemli
- Bu iş akışı debug/test APK'sı üretir; Play Store'a yüklemeye hazır imzalı release APK değildir.
- Bu çalışma ortamında Android SDK ve Gradle bulunmadığı için APK burada derlenip test edilemedi.
- Derleme başarısız olursa GitHub Actions ekranındaki hata günlüğü gerekir; hata günlüğü görülmeden APK'nın başarılı olduğu söylenemez.
- APK'nın telefonda kurulması, sensör ölçümlerinin bilimsel olarak doğrulandığı anlamına gelmez.
