package kartollika.vkeducation.audioplayer.presentation.player.tracks_list

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kartollika.vkeducation.audioplayer.common.utils.setImageResource
import kartollika.vkeducation.audioplayer.player.AudioTrack
import kotlinx.android.synthetic.main.audiotrack_item.view.*

class AudioTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var previewImage: ImageView = itemView.albumPreviewImageView
    private var artistNameTextView: TextView = itemView.artistNameTextView
    private var songNameTextView: TextView = itemView.songNameTextView

    fun bind(audioTrack: AudioTrack) {
        artistNameTextView.text = audioTrack.artist
        songNameTextView.text = audioTrack.title
        val albumArt = audioTrack.albumArt
        previewImage.setImageResource(albumArt)
    }
}