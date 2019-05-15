package kartollika.vkeducation.audioplayer.presentation.player.tracks_list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.player.AudioTrack

class AudioTracksAdapter(var audioTracks: List<AudioTrack> = mutableListOf()) :
    RecyclerView.Adapter<AudioTrackViewHolder>() {

    companion object {
        private const val DUMMY_VIEW = 1
        private const val DEFAULT_TRACK_VIEW = 0
    }

    private val dummyAudioTrack = AudioTrack(
        albumArt = R.drawable.ic_baseline_music_off_24px
    )

    override fun onCreateViewHolder(container: ViewGroup, p1: Int): AudioTrackViewHolder {
        val view = LayoutInflater.from(container.context).inflate(
            R.layout.audiotrack_item, container, false
        )
        return AudioTrackViewHolder(view)
    }

    override fun getItemCount(): Int = Math.max(audioTracks.size, 1)

    override fun onBindViewHolder(p0: AudioTrackViewHolder, position: Int) {
        if (getItemViewType(position) == DUMMY_VIEW) {
            p0.bind(dummyAudioTrack)
        } else {
            p0.bind(audioTracks[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (audioTracks.isEmpty()) {
            return DUMMY_VIEW
        }
        return DEFAULT_TRACK_VIEW
    }
}