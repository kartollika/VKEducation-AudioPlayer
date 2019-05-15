package kartollika.vkeducation.audioplayer.player

class PlayerRepository {

    internal var audioTracks: Map<String, AudioTrack> = mutableMapOf()

    private var currentAudioIndex: Int = 0

    fun getTrackByTag(tag: Any): AudioTrack? = audioTracks[tag.toString()]

    fun getCurrentIndex() = currentAudioIndex

    fun getCurrentIndexAsLong() = currentAudioIndex.toLong()

    fun skipTo(newCurrentIndex: Int) {
        currentAudioIndex = newCurrentIndex
    }
}