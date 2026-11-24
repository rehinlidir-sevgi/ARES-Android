package com.ares.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.FileProvider
import kotlin.math.sqrt

class MainActivity : Activity(), SensorEventListener, TextToSpeech.OnInitListener {
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private lateinit var statusText: TextView
    private lateinit var readingText: TextView
    private lateinit var laneText: TextView
    private lateinit var calibrationText: TextView
    private lateinit var logText: TextView

    private var scanning = false
    private var calibrating = false
    private var currentLane = 1
    private var magneticX = 0f
    private var magneticY = 0f
    private var magneticZ = 0f
    private var magneticMagnitude = 0f
    private var baseline = 0f
    private var baselineSamples = 0
    private var sampleCount = 0
    private var sensorAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
    private var lastMagneticTimestamp = 0L
    private var lastSavedFile: File? = null
    private val baselineValues = mutableListOf<Float>()
    private val lastValues = mutableMapOf<Int, FloatArray>()
    private val csv = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        buildUi()
        updateSensorAvailability()
        requestAudioPermission()
    }

    private fun buildUi() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 18, 22, 18)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = TextView(this).apply {
            text = "ARES\nSaha Ölçüm Sistemi"
            textSize = 26f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 18)
        }
        statusText = TextView(this).apply { textSize = 16f; text = "Hazırlanıyor..." }
        readingText = TextView(this).apply { textSize = 17f; text = "Manyetik alan: -- µT\nReferans farkı: -- µT" }
        laneText = TextView(this).apply { textSize = 19f; text = "Tarama hattı: 1 / 5" }
        calibrationText = TextView(this).apply { textSize = 15f; text = "Kalibrasyon: yapılmadı" }
        logText = TextView(this).apply { textSize = 14f; text = "Ölçüm kayıtları burada görünecek." }

        content.addView(title)
        content.addView(statusText)
        content.addView(readingText)
        content.addView(calibrationText)
        content.addView(laneText)
        content.addView(makeButton("1. Başlangıç referansı al (telefonu sabit tutun)") { startCalibration() })
        content.addView(makeButton("2. Beş hatlı taramayı başlat") { startScan() })
        content.addView(makeButton("Sonraki hatta geç") { nextLane() })
        content.addView(makeButton("Taramayı bitir ve CSV kaydet") { stopScanAndSave() })
        content.addView(makeButton("Son CSV dosyasını paylaş") { shareLastCsv() })
        content.addView(makeButton("Sesli komut dinle") { listenForCommand() })
        content.addView(makeButton("Kalibrasyonu temizle") { resetCalibration() })
        content.addView(logText)
        scroll.addView(content)
        outer.addView(scroll)
        setContentView(outer)
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply { text = label; textSize = 14f; setOnClickListener { action() } }

    private fun updateSensorAvailability() {
        val missing = mutableListOf<String>()
        if (magnetometer == null) missing.add("manyetometre")
        if (accelerometer == null) missing.add("ivmeölçer")
        if (gyroscope == null) missing.add("jiroskop")
        statusText.text = if (missing.isEmpty()) {
            "Sensörler bulundu. Ölçüm öncesi kalibrasyon yapın."
        } else {
            "Eksik sensör: ${missing.joinToString()}. Bazı veriler sıfır kaydedilebilir."
        }
    }

    override fun onResume() {
        super.onResume()
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        lastValues[event.sensor.type] = event.values.clone()
        if (event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return

        magneticX = event.values[0]
        magneticY = event.values[1]
        magneticZ = event.values[2]
        magneticMagnitude = sqrt(magneticX * magneticX + magneticY * magneticY + magneticZ * magneticZ)
        lastMagneticTimestamp = event.timestamp

        if (calibrating) {
            baselineValues.add(magneticMagnitude)
            baselineSamples = baselineValues.size
            calibrationText.text = "Kalibrasyon örneği: $baselineSamples / 100"
            if (baselineValues.size >= 100) finishCalibration()
        }

        if (scanning) {
            sampleCount++
            appendCsvRow(event.timestamp)
        }

        val delta = if (baselineSamples > 0) magneticMagnitude - baseline else 0f
        readingText.text = "Manyetik alan: %.2f µT\nReferans farkı: %+.2f µT\nÖrnek: %d\nSensör doğruluğu: %s".format(
            magneticMagnitude, delta, sampleCount, accuracyLabel(sensorAccuracy)
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            sensorAccuracy = accuracy
            calibrationText.text = "Kalibrasyon: ${if (baselineSamples >= 100) "tamamlandı" else "bekliyor"} | Sensör: ${accuracyLabel(accuracy)}"
        }
    }

    private fun accuracyLabel(value: Int): String = when (value) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "yüksek"
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "orta"
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "düşük"
        else -> "güvenilmez/bilinmiyor"
    }

    private fun startCalibration() {
        if (scanning) {
            speak("Önce taramayı bitirin.")
            return
        }
        if (magnetometer == null) {
            speak("Bu telefonda manyetometre bulunmuyor.")
            return
        }
        calibrating = true
        baselineValues.clear()
        baselineSamples = 0
        statusText.text = "Kalibrasyon: telefonu metal eşyalardan uzakta sabit tutun."
        calibrationText.text = "Kalibrasyon örneği: 0 / 100"
        speak("Kalibrasyon başladı. Telefonu hareketsiz ve metal nesnelerden uzakta tutun.")
    }

    private fun finishCalibration() {
        calibrating = false
        baseline = baselineValues.average().toFloat()
        val variance = baselineValues.map { (it - baseline) * (it - baseline) }.average()
        val stdDev = sqrt(variance).toFloat()
        calibrationText.text = "Referans: %.2f µT | Dalgalanma: %.3f µT".format(baseline, stdDev)
        statusText.text = if (stdDev > 2.0f) {
            "Uyarı: referans çok dalgalı. Metal etkisi/hareket olabilir; yeniden kalibre edin."
        } else {
            "Kalibrasyon alındı. Ölçüm koşullarını değiştirmeyin."
        }
        speak("Kalibrasyon tamamlandı. Referans %.1f mikrotesla.".format(baseline))
    }

    private fun resetCalibration() {
        if (scanning) {
            speak("Tarama sürerken kalibrasyon temizlenemez.")
            return
        }
        calibrating = false
        baseline = 0f
        baselineSamples = 0
        baselineValues.clear()
        calibrationText.text = "Kalibrasyon: yapılmadı"
        statusText.text = "Yeni kalibrasyon bekleniyor."
        speak("Kalibrasyon temizlendi.")
    }

    private fun startScan() {
        if (magnetometer == null) {
            speak("Bu telefonda manyetometre bulunamadı.")
            return
        }
        if (calibrating) {
            speak("Kalibrasyonun bitmesini bekleyin.")
            return
        }
        if (baselineSamples < 100) {
            speak("Önce başlangıç referansı alın.")
            statusText.text = "Tarama başlatılmadı: önce 100 örnekli kalibrasyon yapın."
            return
        }
        scanning = true
        currentLane = 1
        sampleCount = 0
        csv.setLength(0)
        csv.append("timestamp_iso,elapsed_realtime_ns,lane,mag_x_uT,mag_y_uT,mag_z_uT,mag_total_uT,delta_from_baseline_uT,acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z,sensor_accuracy\n")
        laneText.text = "Tarama hattı: 1 / 5"
        statusText.text = "Tarama başladı. Telefonu aynı yükseklik, yön ve hızda tutun."
        speak("Tarama başladı. Birinci hat.")
    }

    private fun nextLane() {
        if (!scanning) { speak("Önce taramayı başlatın."); return }
        if (currentLane < 5) {
            currentLane++
            laneText.text = "Tarama hattı: $currentLane / 5"
            speak("$currentLane numaralı hatta geçin.")
        } else speak("Beşinci hattasınız. Taramayı bitirebilirsiniz.")
    }

    private fun appendCsvRow(sensorTimestamp: Long) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date())
        val acc = lastValues[Sensor.TYPE_ACCELEROMETER] ?: floatArrayOf(Float.NaN, Float.NaN, Float.NaN)
        val gyro = lastValues[Sensor.TYPE_GYROSCOPE] ?: floatArrayOf(Float.NaN, Float.NaN, Float.NaN)
        val delta = magneticMagnitude - baseline
        csv.append("$now,$sensorTimestamp,$currentLane,$magneticX,$magneticY,$magneticZ,$magneticMagnitude,$delta,")
            .append("${acc[0]},${acc[1]},${acc[2]},${gyro[0]},${gyro[1]},${gyro[2]},$sensorAccuracy\n")
    }

    private fun stopScanAndSave() {
        if (!scanning) { speak("Aktif tarama yok."); return }
        scanning = false
        try {
            val dir = getExternalFilesDir(null) ?: filesDir
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val out = File(dir, "ARES_tarama_$stamp.csv")
            out.writeText(csv.toString())
            lastSavedFile = out
            statusText.text = "Tarama kaydedildi: ${out.name}\nÖrnek sayısı: $sampleCount"
            logText.text = "Kayıt: ${out.absolutePath}\nÖlçüm sayısı: $sampleCount\nReferans: %.2f µT".format(baseline)
            speak("Tarama verileri kaydedildi. $sampleCount ölçüm.")
        } catch (e: Exception) {
            statusText.text = "Kayıt hatası: ${e.localizedMessage}"
            speak("Kayıt sırasında hata oluştu.")
        }
    }

    private fun shareLastCsv() {
        val file = lastSavedFile
        if (file == null || !file.exists()) {
            speak("Önce bir tarama kaydedin.")
            return
        }
        try {
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(send, "ARES ölçüm CSV dosyasını paylaş"))
            statusText.text = "CSV paylaşım menüsü açıldı: ${file.name}"
        } catch (e: Exception) {
            statusText.text = "Paylaşım hatası: ${e.localizedMessage}"
            speak("CSV paylaşımı açılamadı.")
        }
    }

    private fun requestAudioPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
        }
    }

    private fun listenForCommand() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission(); return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Bu cihazda konuşma tanıma hizmeti bulunamadı."); return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { statusText.text = "Dinliyorum..." }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { statusText.text = "Komut işleniyor..." }
                override fun onError(error: Int) { statusText.text = "Ses algılanamadı. Tekrar deneyin." }
                override fun onResults(results: Bundle?) {
                    val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                        ?.lowercase(Locale("tr", "TR")) ?: return
                    logText.text = "Algılanan komut: $phrase"
                    when {
                        phrase.contains("kalibrasyon") || phrase.contains("referans al") -> startCalibration()
                        phrase.contains("başlat") || phrase.contains("tara") -> startScan()
                        phrase.contains("sonraki") || phrase.contains("hat") -> nextLane()
                        phrase.contains("bitir") || phrase.contains("kaydet") -> stopScanAndSave()
                        phrase.contains("sıfırla") -> resetCalibration()
                        else -> speak("Komutu anlayamadım. Kalibrasyon yap, taramayı başlat, sonraki hat veya bitir diyebilirsiniz.")
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "ARES komutunu söyleyin")
            }
            startListening(intent)
        }
    }

    private fun speak(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "ARES")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale("tr", "TR")
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}
