package kartollika.vkeducation.audioplayer.common.utils

fun Int.parseIntToLength(): String {
    val minutes = this / 60
    val seconds = this % 60

    val lengthStringBuilder = StringBuffer()
    if (minutes < 10) {
        lengthStringBuilder.append("0").append(minutes)
    } else {
        lengthStringBuilder.append(minutes)
    }

    lengthStringBuilder.append(":")

    if (seconds < 10) {
        lengthStringBuilder.append("0").append(seconds)
    } else {
        lengthStringBuilder.append(seconds)
    }

    return lengthStringBuilder.toString()
}