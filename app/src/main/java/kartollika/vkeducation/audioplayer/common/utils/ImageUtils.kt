package kartollika.vkeducation.audioplayer.common.utils

import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView

fun ImageView.setImageResource(imageResource: Any) {
    when (imageResource) {
        is String -> setImageResource(
            Uri.parse(
                imageResource
            )
        )
        is Uri -> setImageURI(imageResource)
        is Bitmap -> setImageBitmap(imageResource)
        is Int -> setImageResource(imageResource)
        else -> setImageDrawable(null)
    }
}