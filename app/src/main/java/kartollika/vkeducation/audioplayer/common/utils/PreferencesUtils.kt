package kartollika.vkeducation.audioplayer.common.utils

import android.content.Context
import android.content.SharedPreferences

private const val AUDIO_STORAGE = "audio_storage"

class PreferencesUtils(private val context: Context) {

    private lateinit var sharedPreferences: SharedPreferences

    fun saveLastPlayedDirectory(lastDirectory: String) {
        sharedPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("last_directory", lastDirectory)
        }.apply()
    }

    fun getLastPlayedDirectory(): String {
        sharedPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE)
        return sharedPreferences.getString("last_directory", "")!!
    }
}