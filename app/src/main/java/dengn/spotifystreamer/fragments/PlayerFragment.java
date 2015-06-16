package dengn.spotifystreamer.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.utils.DebugConfig;
import dengn.spotifystreamer.utils.PlayerUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends Fragment implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    @InjectView(R.id.player_artist_name)
    TextView artistName;
    @InjectView(R.id.player_album_name)
    TextView albumName;
    @InjectView(R.id.player_track_name)
    TextView trackName;
    @InjectView(R.id.player_album_img)
    ImageView albumImage;
    @InjectView(R.id.player_seekbar)
    SeekBar seekBar;
    @InjectView(R.id.player_ontime)
    TextView playTime;
    @InjectView(R.id.player_timelen)
    TextView totalTime;
    @InjectView(R.id.player_previous)
    ImageButton previous;
    @InjectView(R.id.player_play_pause)
    ImageButton playPause;
    @InjectView(R.id.player_next)
    ImageButton next;
    @InjectView(R.id.player_forward)
    ImageButton forward;
    @InjectView(R.id.player_backward)
    ImageButton backward;

    private String mArtistName;
    private String mAlbumName;
    private String mTrackName;
    private int mTrackDuration;
    private String mTrackPreview;
    private String mAlbumImage;

    private ArrayList<MyTrack> mTracks = new ArrayList<>();
    private int position = 0;

    private static final int seekBackwardTime = 1000;
    private static final int seekForwardTime = 1000;


    private MediaPlayer mediaPlayer;

    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();


    public static PlayerFragment newInstance(ArrayList<MyTrack> tracks, int position) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("tracks", tracks);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }


    public PlayerFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);


        if (getArguments() != null) {

            mTracks = getArguments().getParcelableArrayList("tracks");
            position = getArguments().getInt("position");

            mArtistName = mTracks.get(position).artistName;
            mAlbumName = mTracks.get(position).artistName;
            mTrackName = mTracks.get(position).name;
            mTrackDuration = MyTrack.PREVIEW_LENGTH_DEFAULT;
            mTrackPreview = mTracks.get(position).previewURL;
            mAlbumImage = mTracks.get(position).imageLargeURL;

            if (DebugConfig.DEBUG)
                Log.d(DebugConfig.TAG, "mAlbumImage " + mAlbumImage);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.inject(this, view);



        mediaPlayer = new MediaPlayer();
        // Listeners
        seekBar.setOnSeekBarChangeListener(this);
        mediaPlayer.setOnCompletionListener(this);


        artistName.setText(mArtistName);
        albumName.setText(mAlbumName);
        trackName.setText(mTrackName);
        playTime.setText("0:00");
        totalTime.setText("0:30");

        Picasso.with(getActivity()).load(mAlbumImage).into(albumImage);

        playSong();

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check for already playing
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                        // Changing button image to play button
                        playPause.setBackgroundResource(android.R.drawable.ic_media_play);
                    }
                } else {
                    // Resume song
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                        updateProgressBar();
                        // Changing button image to pause button
                        playPause.setBackgroundResource(android.R.drawable.ic_media_pause);
                    }
                }
            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        backward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mediaPlayer.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if (currentPosition - seekBackwardTime >= 0) {
                    // forward song
                    mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                } else {
                    // backward to starting position
                    mediaPlayer.seekTo(0);
                }

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        forward.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mediaPlayer.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekForwardTime <= mediaPlayer.getDuration()) {
                    // forward song
                    mediaPlayer.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                }
            }
        });


        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if (position < mTracks.size() - 1) {
                    position++;
                    refreshUI(position);
                    playSong();
                } else {
                    // play first song
                    position=0;
                    refreshUI(position);
                    playSong();
                }

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        previous.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (position > 0) {
                    position--;
                    refreshUI(position);
                    playSong();
                } else {
                    // play last song
                    position=mTracks.size()-1;
                    refreshUI(position);
                    playSong();
                }

            }
        });


        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                // Changing button image to play button
                playPause.setBackgroundResource(android.R.drawable.ic_media_play);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHandler.removeCallbacks(mUpdateTimeTask);
        mediaPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mHandler.removeCallbacks(mUpdateTimeTask);
        playPause.setBackgroundResource(android.R.drawable.ic_media_play);
        seekBar.setProgress(0);
        playTime.setText("0:00");

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);

    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = PlayerUtils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();

    }

    private void refreshUI(int newPosition){
        mArtistName = mTracks.get(newPosition).artistName;
        mAlbumName = mTracks.get(newPosition).artistName;
        mTrackName = mTracks.get(newPosition).name;
        mTrackDuration = MyTrack.PREVIEW_LENGTH_DEFAULT;
        mTrackPreview = mTracks.get(newPosition).previewURL;
        mAlbumImage = mTracks.get(newPosition).imageLargeURL;


        artistName.setText(mArtistName);
        albumName.setText(mAlbumName);
        trackName.setText(mTrackName);
        playTime.setText("0:00");
        totalTime.setText("0:30");

        Picasso.with(getActivity()).load(mAlbumImage).into(albumImage);


    }

    /**
     * Function to play a song
     */
    public void playSong() {
        // Play song
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mTrackPreview);
            mediaPlayer.prepare();
            mediaPlayer.start();
            playPause.setBackgroundResource(android.R.drawable.ic_media_pause);

            playTime.setText(PlayerUtils.milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
            totalTime.setText(PlayerUtils.milliSecondsToTimer(mediaPlayer.getDuration()));
            // set Progress bar values
            seekBar.setProgress(0);
            seekBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            playTime.setText(PlayerUtils.milliSecondsToTimer(currentDuration));
            // Displaying time completed playing
            totalTime.setText(PlayerUtils.milliSecondsToTimer(totalDuration));

            // Updating progress bar
            int progress = PlayerUtils.getProgressPercentage(currentDuration, totalDuration);

            seekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

}
