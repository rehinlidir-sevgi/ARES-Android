# ARES – Teknik Tasarım ve Geliştirme Planı

## 1. Amaç
ARES, Android telefondan ölçüm toplayan, sesli komutla tarama akışını yöneten ve ölçüm verilerini incelemeye hazırlayan bir saha uygulaması prototipidir.

Bu belge bir ürün doğrulama raporu değildir. Telefon sensörlerinin yer altındaki boşlukları veya su kanallarını tek başına tespit ettiği kanıtlanmış değildir.

## 2. Modüller

### A. Sensör toplama
- Manyetometre: x/y/z manyetik alan bileşenleri (µT).
- İvmeölçer: hareket ve yerçekimi bileşenleri (m/s²).
- Jiroskop: açısal hız (rad/s).
- Her örnek zaman damgası ve tarama hattı numarasıyla kaydedilir.
- Sensör bulunmuyorsa uygulama bunu açıkça bildirir.

### B. Tarama yöneticisi
- Beş hatlı başlangıç iş akışı.
- Kullanıcı her hat değişiminde uygulamaya komut verir.
- Bu sürüm hatlar arası fiziksel mesafeyi ve telefonun zemine göre konumunu otomatik ölçmez.
- Tekrarlanabilir ölçüm için sabit yükseklik, yön, hız ve hat aralığı gerekir.

### C. Sesli arayüz
İlk prototipte Android'in konuşma tanıma ve metin okuma servisleri kullanılır.
Desteklenen temel komutlar:
- “ARES taramayı başlat”
- “Sonraki hatta geç”
- “Taramayı bitir”
- “Referansı sıfırla”

Bu sürüm sürekli dinleyen, bağımsız bir sohbet yapay zekâsı değildir. Komut düğmesiyle dinleme başlatılır.

### D. Veri dışa aktarma
- Ham sensör ölçümleri CSV olarak kaydedilir.
- CSV, sonraki analiz ve grafik aşamasının temel girdisidir.
- CSV'deki manyetik alan değişimi doğrudan “boşluk” veya “derinlik” anlamına gelmez.

## 3. Bilimsel ve teknik sınırlar
- Manyetometre manyetik alanı ölçer; hava boşluğunu doğrudan ölçmez.
- Beton, asfalt, demir donatı, borular, kablolar, araçlar, telefon kılıfındaki mıknatıslar ve çevredeki metal nesneler ölçümü etkileyebilir.
- İvmeölçer ve jiroskop telefon hareketini izlemeye yardım eder; tek başlarına yer altı geometrisini ölçmez.
- GPS, kısa tarama hatlarında santimetre hassasiyeti sağlamaz.
- Fotoğraf, yüzeyin görsel kaydıdır; yer altı boşluğunun derinliğini tek başına belirleyemez.
- Bir yapay zekâ modeli sensörlerde bulunmayan fiziksel bilgiyi üretemez. Ölçümle desteklenmeyen derinlik değeri gösterilmemelidir.

## 4. Gerçek derinlik ve 3D için gerekenler
1. Ölçüm düzeneğinin sabitlenmesi ve sensör kalibrasyonu.
2. Hat aralığı, hat uzunluğu, örnekleme hızı ve telefon yüksekliğinin tanımlanması.
3. Aynı zeminde tekrarlı taramalar.
4. Bilinen konum ve derinlikte referans hedeflerle test.
5. Metal etkileri ve zemin türlerinin ayrı kaydı.
6. Verinin koordinatlandırılması ve gürültü filtreleme.
7. Ancak yeterli doğrulama verisi varsa anomali sınıflandırma ve derinlik kestirimi.
8. 3D görselleştirme: gerçek ölçüm noktaları, belirsizlik aralığı ve tahmini bölgeler ayrı gösterilmeli.

## 5. Saha test protokolü
- Test alanının ölçülerini belirleyin.
- Bilinen derinlikte, güvenli ve izinli bir test hedefi kullanın.
- Telefonu metal içermeyen bir taşıyıcıya sabitleyin.
- Başlangıç yönünü ve yüksekliğini işaretleyin.
- Beş hattı aynı hız ve aralıkla tarayın.
- Her taramayı en az üç kez tekrarlayın.
- Hedef olmayan kontrol alanında da tarama yapın.
- Gerçek hedef konumu ile yazılımın bulduğu anomaliyi karşılaştırın.
- Sonuçları başarı/başarısızlık ve hata payıyla raporlayın.

## 6. Aşamalar
- Aşama 1: Android sensörlerinden ham veri toplama ve CSV kaydı (bu kaynak kod paketinin kapsamı).
- Aşama 2: Sensör kalibrasyonu, örnekleme kalitesi ve tarama güzergâhı kaydı.
- Aşama 3: CSV analiz ekranı, grafikler ve ölçüm kalitesi göstergesi.
- Aşama 4: Kamera ile yüzey fotoğrafı/video kaydı ve ölçüm oturumuna bağlama.
- Aşama 5: 2D/3D veri görselleştirme.
- Aşama 6: Referans hedeflerle doğrulanmışsa sınıflandırma/derinlik kestirimi.
- Aşama 7: İsteğe bağlı harici sensör entegrasyonu.

## 7. Bu paketin mevcut durumu
Bu paket Aşama 1 için başlangıç kaynak kodudur. Derlenmiş APK değildir. Android Studio ve Android SDK ile derlenmesi gerekir. APK oluşturulmadan önce gerçek Android cihazlarda derleme, izin, sensör ve kayıt testleri yapılmalıdır.
