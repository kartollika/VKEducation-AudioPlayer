package kartollika.vkeducation.audioplayer.player

class PlayerRepository {

    internal var audioTracks: Map<String, AudioTrack> = mutableMapOf()

    private var currentAudioIndex: Int = 0

    fun getTrackByTag(tag: Any): AudioTrack? = audioTracks[tag.toString()]

//    fun getCurrentTrack(tag: String): AudioTrack? = audioTracks[tag]
//
//    fun getNextTrack(): AudioTrack {
//        if (++currentAudioIndex >= audioTracks.size) {
//            currentAudioIndex = audioTracks.size - 1
//        }
//        return audioTracks[currentAudioIndex]
//
//    }
//
//    fun getPreviousTrack(): AudioTrack {
//        if (--currentAudioIndex < 0) {
//            currentAudioIndex = 0
//        }
//        return audioTracks[currentAudioIndex]
//    }

    fun getCurrentIndex() = currentAudioIndex

    fun getCurrentIndexAsLong() = currentAudioIndex.toLong()

    fun skipTo(newCurrentIndex: Int) {
        currentAudioIndex = newCurrentIndex
    }

//    fun getAudioByTag(tag: Any?): AudioTrack {
//
//    }
}