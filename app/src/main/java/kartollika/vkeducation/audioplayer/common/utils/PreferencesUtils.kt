package kartollika.vkeducation.audioplayer.common.utils

import android.content.Context
import android.content.SharedPreferences

private const val AUDIO_STORAGE = "audio_storage"

class PreferencesUtils(private val context: Context) {

    private lateinit var sharedPreferences: SharedPreferences

//    fun saveLastPlayedTracks(tracks: List<AudioTrack>) {
//        sharedPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE)
//        sharedPreferences.edit().apply {
//            putString("tracks", JSONArray(tracks).toString())
//        }.apply()
//    }
//
//    fun saveLastPlayedTracks(tracks: List<AudioTrack>) {
//        sharedPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE)
//        val a =
//        sharedPreferences.edit().apply {
//            putString("tracks", JSONArray(tracks).toString())
//        }.apply()
//    }
    fun saveLastPlayedDirectory(lastDirectory: String) {
        sharedPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("last_directory", lastDirectory)
        }.apply()
    }
//
    fun getLastPlayedDirectory(): String {
        sharedPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE)
        return sharedPreferences.getString("last_directory", "")!!
    }

    fun saveLastPlayedPosition(lastPosition: Int) {
        sharedPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putInt("last_position", lastPosition)
        }.apply()
    }

    fun getLastPlayedPosition(): Int {
        sharedPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE)
        return sharedPreferences.getInt("last_position", -1)
    }

}