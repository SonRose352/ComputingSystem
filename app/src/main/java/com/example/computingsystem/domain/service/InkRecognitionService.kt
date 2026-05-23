package com.example.computingsystem.domain.service

import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class RecognitionResult(
    val expression: String,
    val hasUncertainSymbols: Boolean
)

@Singleton
class InkRecognitionService @Inject constructor() {

    // Математическая модель ML Kit
    private val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")
    private val model = modelIdentifier?.let {
        DigitalInkRecognitionModel.builder(it).build()
    }

    suspend fun downloadModelIfNeeded(): Boolean = suspendCancellableCoroutine { cont ->
        if (model == null) { cont.resume(false); return@suspendCancellableCoroutine }
        val remoteModelManager = com.google.mlkit.common.model.RemoteModelManager.getInstance()
        remoteModelManager.isModelDownloaded(model)
            .addOnSuccessListener { isDownloaded ->
                if (isDownloaded) {
                    cont.resume(true)
                } else {
                    remoteModelManager.download(model, com.google.mlkit.common.model.DownloadConditions.Builder().build())
                        .addOnSuccessListener { cont.resume(true) }
                        .addOnFailureListener { cont.resume(false) }
                }
            }
            .addOnFailureListener { cont.resume(false) }
    }

    suspend fun recognize(
        strokes: List<List<Pair<Float, Float>>>
    ): RecognitionResult = suspendCancellableCoroutine { cont ->

        if (model == null) {
            cont.resumeWithException(IllegalStateException("Модель не доступна"))
            return@suspendCancellableCoroutine
        }

        val recognizer: DigitalInkRecognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(model).build()
        )

        // Строим Ink из штрихов DrawingNode
        val inkBuilder = Ink.builder()
        strokes.forEach { strokePoints ->
            if (strokePoints.isEmpty()) return@forEach
            val strokeBuilder = Ink.Stroke.builder()
            strokePoints.forEach { (x, y) ->
                strokeBuilder.addPoint(Ink.Point.create(x, y))
            }
            inkBuilder.addStroke(strokeBuilder.build())
        }
        val ink = inkBuilder.build()

        recognizer.recognize(ink)
            .addOnSuccessListener { result ->
                val candidates = result.candidates
                if (candidates.isEmpty()) {
                    cont.resumeWithException(IllegalStateException("Ничего не распознано"))
                    return@addOnSuccessListener
                }

                val best = candidates.first().text

                // Фильтруем только математически допустимые символы
                val allowedChars = Regex("[^0-9+\\-*/().^sincotalqreπ]")
                val hasUncertain = allowedChars.containsMatchIn(best.replace(" ", ""))

                val cleaned = best
                    .replace("−", "-")
                    .replace("x", "×")
                    .replace("X", "×")
                    .replace(" ", "")
                    .replace(",", ".")

                cont.resume(RecognitionResult(expression = cleaned, hasUncertainSymbols = hasUncertain))
                recognizer.close()
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
                recognizer.close()
            }
    }
}