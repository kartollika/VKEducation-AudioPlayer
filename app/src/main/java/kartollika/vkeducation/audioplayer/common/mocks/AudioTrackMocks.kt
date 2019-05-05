package kartollika.vkeducation.audioplayer.common.mocks

import kartollika.vkeducation.audioplayer.data.models.AudioTrack

fun getAudioTracksMocks(): List<AudioTrack> {
    return listOf(
        AudioTrack("Knock-knock", "Lenka", howLong = 135),
        AudioTrack("Чика", "Артур Пирожков", howLong = 240),
        AudioTrack("Чика", "Артур Пирожков", howLong = 240),
        AudioTrack("Чика", "Артур Пирожков", howLong = 240),
        AudioTrack("Чика", "Артур Пирожков", howLong = 240),
        AudioTrack("Чика", "Артур Пирожков", howLong = 240),
        AudioTrack("Чика", "Артур Пирожков", howLong = 240)
    )
}