package eu.kanade.tachiyomi.ui.reader.viewer

import chimahon.anki.AnkiSentenceAudioFailure
import chimahon.anki.AnkiSentenceAudioPreparation
import chimahon.anki.AnkiSentenceAudioSource
import chimahon.anki.AnkiMediaRequest
import chimahon.anki.LazyAnkiSentenceAudioProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class AnkiPopupMediaPreloadTest {

    @Test
    fun `popup media retains an unavailable sentence-audio preparation`() {
        val unavailable = AnkiSentenceAudioPreparation.Unavailable(
            AnkiSentenceAudioFailure.AUDIO_STREAMS_NOT_FOUND,
        )

        val media = PopupPreparedAnkiMedia(
            frameId = "frame",
            screenshotBytes = null,
            sentenceAudio = unavailable,
        )

        assertEquals(unavailable, media.sentenceAudio)
    }

    @Test
    fun `sentence-audio provider waits for active popup media preparation`() = runTest {
        val gate = SerializedAnkiMediaPreloadGate()
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val providerStarted = CompletableDeferred<Unit>()
        val request = AnkiMediaRequest(
            sentenceAudioProvider = LazyAnkiSentenceAudioProvider {
                providerStarted.complete(Unit)
                AnkiSentenceAudioPreparation.Ready(
                    AnkiSentenceAudioSource.fromBytes(byteArrayOf(1), "m4a"),
                )
            },
        ).withSerializedSentenceAudioPreparation(gate)

        val first = launch {
            gate.run {
                firstStarted.complete(Unit)
                releaseFirst.await()
            }
        }
        firstStarted.await()

        val preparation = async { request.sentenceAudioProvider?.prepare() }
        runCurrent()

        assertFalse(providerStarted.isCompleted)

        releaseFirst.complete(Unit)

        val prepared = preparation.await() as? AnkiSentenceAudioPreparation.Ready
        assertEquals(listOf<Byte>(1), prepared?.source?.data?.toList())
        first.join()
    }

    @Test
    fun `next preload waits until the active preload releases the shared gate`() = runTest {
        val gate = SerializedAnkiMediaPreloadGate()
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()

        val first = launch {
            gate.run {
                firstStarted.complete(Unit)
                releaseFirst.await()
            }
        }
        firstStarted.await()

        val second = async {
            gate.run {
                secondStarted.complete(Unit)
                "second"
            }
        }
        runCurrent()

        assertFalse(secondStarted.isCompleted)

        releaseFirst.complete(Unit)

        assertEquals("second", second.await())
        first.join()
    }
}
