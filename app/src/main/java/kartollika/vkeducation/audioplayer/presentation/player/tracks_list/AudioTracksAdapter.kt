package kartollika.vkeducation.audioplayer.presentation.player.tracks_list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.player.AudioTrack

class AudioTracksAdapter(audioTracks: List<AudioTrack>) :
    RecyclerView.Adapter<AudioTrackViewHolder>() {

    private val dummyView = 1
    private val dummyAudioTrack =
        AudioTrack(artist = "Треков нет", title = "Выберите треки и приходите обратно :)")
    private val defaultTrackView = 0

    interface OnSetTracksListener {
        fun onSet(isEmpty: Boolean)
    }

    var onSetAudioTracksListener: OnSetTracksListener? = null

    var audioTracks: List<AudioTrack> = audioTracks
        set(value) {
            onSetAudioTracksListener?.onSet(audioTracks.isEmpty())
            field = value
        }

    override fun onCreateViewHolder(container: ViewGroup, p1: Int): AudioTrackViewHolder {
        val view = LayoutInflater.from(container.context).inflate(
            R.layout.audiotrack_item, container, false
        )
        return AudioTrackViewHolder(view)
    }

    override fun getItemCount(): Int = Math.max(audioTracks.size, 1)

    override fun onBindViewHolder(p0: AudioTrackViewHolder, position: Int) {
        if (getItemViewType(position) == dummyView) {
            p0.bind(dummyAudioTrack)
        } else {
            p0.bind(audioTracks[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (audioTracks.isEmpty()) {
            return dummyView
        }
        return defaultTrackView
    }
}