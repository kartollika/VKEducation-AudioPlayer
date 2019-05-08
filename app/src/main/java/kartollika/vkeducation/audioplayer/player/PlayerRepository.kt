package kartollika.vkeducation.audioplayer.player

class PlayerRepository {

    internal var audioTracks: List<AudioTrack> = mutableListOf()

    private var currentAudioIndex: Int = 0

    fun getCurrentTrack(): AudioTrack = audioTracks[currentAudioIndex]

    fun getNextTrack(): AudioTrack {
        if (++currentAudioIndex >= audioTracks.size) {
            currentAudioIndex = audioTracks.size - 1
        }
        return audioTracks[currentAudioIndex]

    }

    fun getPreviousTrack(): AudioTrack {
        if (--currentAudioIndex < 0) {
            currentAudioIndex = 0
        }
        return audioTracks[currentAudioIndex]
    }

    fun skipTo(newCurrentIndex: Long) {
        currentAudioIndex = newCurrentIndex.toInt()
    }
}