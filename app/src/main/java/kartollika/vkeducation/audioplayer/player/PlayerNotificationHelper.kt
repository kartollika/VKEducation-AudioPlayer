package kartollika.vkeducation.audioplayer.player

import android.content.Context
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class PlayerNotificationHelper {

    companion object {
        fun instantiateNotificationWithContent(
            context: Context, mediaSessionCompat: MediaSessionCompat): NotificationCompat.Builder {
            val metadataDescription = mediaSessionCompat.controller.metadata.description
            return NotificationCompat.Builder(context).setContentTitle(metadataDescription?.title)
                .setContentText(metadataDescription?.subtitle)
                .setLargeIcon(metadataDescription?.iconBitmap).setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_STOP
                    )
                ).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
    }
}