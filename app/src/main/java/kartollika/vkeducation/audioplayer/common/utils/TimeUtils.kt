package kartollika.vkeducation.audioplayer.common.utils

const val MILLISECONDS_IN_SECOND = 1000

/**
 * Parse time in milliseconds in format like mm:ss
 * where m - minute, s - second
 */
fun Long.parseIntToLength(): String {
    val normalizedTime = this / MILLISECONDS_IN_SECOND
    val minutes = normalizedTime / 60
    val seconds = normalizedTime % 60

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

/**
 * Parse time in format mm:ss in milliseconds
 * where m - minute, s - second
 */
fun String.parseToMilliseconds(): Long {
    val timeParts = split(":")
    val seconds = timeParts[0].toLong() * 60 + timeParts[1].toLong()
    return seconds * MILLISECONDS_IN_SECOND
}