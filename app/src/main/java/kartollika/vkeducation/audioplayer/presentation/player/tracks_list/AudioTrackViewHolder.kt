package kartollika.vkeducation.audioplayer.presentation.player.tracks_list

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kartollika.vkeducation.audioplayer.common.utils.setImageResource

import kartollika.vkeducation.audioplayer.player.AudioTrack
import kotlinx.android.synthetic.main.audiotrack_item.view.*

class AudioTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var previewImage: ImageView = itemView.albumPreviewImageView

    fun bind(audioTrack: AudioTrack) {
        val albumArt = audioTrack.albumArt
        previewImage.setImageResource(albumArt)
    }
}