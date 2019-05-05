package kartollika.vkeducation.audioplayer.views.player_view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearSnapHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.mocks.getAudioTracksMocks
import kartollika.vkeducation.audioplayer.common.views.AudioTracksCarouselRecyclerView
import kartollika.vkeducation.audioplayer.common.views.audio_seekbar.AudioSeekbar
import kartollika.vkeducation.audioplayer.views.player_view.tracks_list.AudioTracksAdapter
import kotlinx.android.synthetic.main.fragment_audioplayer.view.*

class PlayerFragment : Fragment() {

    private lateinit var audioSeekbar: AudioSeekbar
    private lateinit var nextTrackActionView: View
    private lateinit var previousTrackActionView: View
    private lateinit var optionsActionView: View
    private lateinit var shuffleTracksActionView: View
    private lateinit var tracksRecyclerView: AudioTracksCarouselRecyclerView
    private lateinit var tracksAdapter: AudioTracksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audioplayer, null)

        with(view) {
            audioSeekbar = audio_seekbar_view
            previousTrackActionView = previous_track_view
            nextTrackActionView = next_track_view
            tracksRecyclerView = tracks_recyclerview
        }

        tracksRecyclerView.setupAdapter(AudioTracksAdapter(getAudioTracksMocks()))
        LinearSnapHelper().apply {
            attachToRecyclerView(tracksRecyclerView)
        }
        return view
    }
}