package kartollika.vkeducation.audioplayer.presentation.player.tracks_list

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.player.AudioTrack

class AudioTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private lateinit var previewImage: ImageView
    private var artistNameTextView: TextView = itemView.findViewById(R.id.artist_name_textview)
    private var songNameTextView: TextView = itemView.findViewById(R.id.song_name_textview)

    fun bind(audioTrack: AudioTrack) {
        artistNameTextView.text = audioTrack.artist
        songNameTextView.text = audioTrack.title
    }
}