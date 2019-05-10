package kartollika.vkeducation.audioplayer.presentation.player.tracks_list

import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kartollika.vkeducation.audioplayer.player.AudioTrack
import kotlinx.android.synthetic.main.audiotrack_item.view.*

class AudioTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var previewImage: ImageView = itemView.albumPreviewImageView

    fun bind(audioTrack: AudioTrack) {
        val albumArt = audioTrack.albumArt
        when (albumArt) {
            is Uri -> previewImage.setImageURI(albumArt)
            is Bitmap -> previewImage.setImageBitmap(albumArt)
            is Int -> previewImage.setImageResource(albumArt)
            else -> previewImage.setImageDrawable(null)
        }

    }
}