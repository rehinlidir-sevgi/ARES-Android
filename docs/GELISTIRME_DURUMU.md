# ARES geliştirme durumu – Sürüm 0.2

## Bu sürümde kaynak koda eklenenler
- Tarama başlamadan önce 100 manyetik alan örneğiyle başlangıç referansı kalibrasyonu.
- Kalibrasyon örneklerinin standart sapması üzerinden basit kararlılık uyarısı.
- Kalibrasyon tamamlanmadan taramayı başlatmayı engelleme.
- Beş hatlı tarama sırasında manyetik alan, referans farkı, ivmeölçer, jiroskop ve sensör doğruluk durumunu CSV'ye yazma.
- Sensörün monotonik zaman damgasını ve ISO tarih-saatini kaydetme.
- Türkçe sesli komutlara kalibrasyon komutunu ekleme.

## Bilinen eksikler / sıradaki teknik işler
1. CSV dosyası Android Sharesheet üzerinden FileProvider ile güvenli biçimde paylaşılabilir.
2. Kamera kaydı ve oturumla ilişkilendirme eklenmeli.
3. Hatların gerçek mesafesi, telefon yüksekliği ve yönü kullanıcıdan alınmalı veya uygun donanımla ölçülmeli.
4. Ölçüm kalitesi grafikleri ve 2D ölçüm haritası hazırlanmalı.
5. 3D yüzey görselleştirmesi, ölçüm koordinatları tanımlandıktan sonra eklenmeli.
6. Derinlik tahmini ancak bilinen derinlikte referans hedeflerle yeterli saha verisi toplanırsa ele alınmalı.

## Derleme / doğrulama
Bu ortamda Android SDK/Gradle olmadığı için APK üretilemedi ve kod Android derleyicisinde doğrulanamadı. Android Studio'da Gradle senkronizasyonu ve derleme yapılmalıdır. Derleme hataları çıkarsa tam hata metni üzerinden düzeltilmelidir.

## Kritik bilimsel sınır
Telefonun dahili manyetometresi manyetik alanı ölçer; su kanalını, hava boşluğunu veya bunların derinliğini doğrudan ölçmez. Bu sürüm bir saha veri toplama prototipidir; yer altı tespit cihazı olarak doğrulanmış değildir.
