package kartollika.vkeducation.audioplayer.views.player_view.tracks_list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.data.models.AudioTrack

class AudioTracksAdapter(private val audioTracks: List<AudioTrack>) :
    RecyclerView.Adapter<AudioTrackViewHolder>() {

    override fun onCreateViewHolder(container: ViewGroup, p1: Int): AudioTrackViewHolder {
        val view = LayoutInflater.from(container.context).inflate(
            R.layout.audiotrack_item,
            container,
            false
        )
        return AudioTrackViewHolder(view)
    }

    override fun getItemCount(): Int = audioTracks.size

    override fun onBindViewHolder(p0: AudioTrackViewHolder, position: Int) {
        p0.bind(audioTracks[position])
    }
}