package eu.kanade.tachiyomi.ui.reader.viewer

import chimahon.anki.AnkiMediaRequest
import chimahon.anki.AnkiSentenceAudioPreparation
import chimahon.anki.LazyAnkiSentenceAudioProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal const val POPUP_ANKI_MEDIA_PRELOAD_DELAY_MS = 500L

/**
 * Serializes media preparation across replacement dictionary popups. A new popup can wait for
 * cancellation cleanup of the previous native capture before starting its own capture.
 */
internal class SerializedAnkiMediaPreloadGate(
    private val mutex: Mutex = Mutex(),
) {
    suspend fun <T> run(block: suspend () -> T): T = mutex.withLock { block() }
}

internal val ankiMediaPreloadGate = SerializedAnkiMediaPreloadGate()

internal data class PopupPreparedAnkiMedia(
    val frameId: String,
    val screenshotBytes: ByteArray?,
    val sentenceAudio: AnkiSentenceAudioPreparation?,
)

internal data class PendingPopupAnkiMediaPreload(
    val frameId: String,
    val nativeCaptureStarted: CompletableDeferred<Unit>,
    val result: Deferred<PopupPreparedAnkiMedia?>,
)

internal fun AnkiMediaRequest.withSerializedSentenceAudioPreparation(
    gate: SerializedAnkiMediaPreloadGate,
): AnkiMediaRequest = copy(
    sentenceAudioProvider = sentenceAudioProvider?.let { provider ->
        LazyAnkiSentenceAudioProvider {
            gate.run { provider.prepare() }
        }
    },
)
