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

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.utils.DebugConfig;
import dengn.spotifystreamer.utils.PlayerUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends Fragment implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener{

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

    private String mArtistName;
    private String mAlbumName;
    private String mTrackName;
    private int mTrackDuration;
    private String mTrackPreview;
    private String mAlbumImage;


    private int playPosition = 0;

    private MediaPlayer mediaPlayer;

    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    public static PlayerFragment newInstance(String artistName, String albumName, String trackName, int trackDuration, String trackPreview, String albumImage) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString("artist_name", artistName);
        args.putString("album_name", albumName);
        args.putString("track_name", trackName);
        args.putInt("track_duration", trackDuration);
        args.putString("track_preview", trackPreview);
        args.putString("album_image", albumImage);
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
            mArtistName = getArguments().getString("artist_name");
            mAlbumName = getArguments().getString("album_name");
            mTrackName = getArguments().getString("track_name");
            mTrackDuration = getArguments().getInt("track_duration");
            mTrackPreview = getArguments().getString("track_preview");
            mAlbumImage = getArguments().getString("album_image");

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
                if(mediaPlayer.isPlaying()){
                    if(mediaPlayer!=null){
                        mediaPlayer.pause();
                        // Changing button image to play button
                        playPause.setBackgroundResource(android.R.drawable.ic_media_play);
                    }
                }else{
                    // Resume song
                    if(mediaPlayer!=null){
                        mediaPlayer.start();
                        updateProgressBar();
                        // Changing button image to pause button
                        playPause.setBackgroundResource(android.R.drawable.ic_media_pause);
                    }
                }
            }
        });


        return view;
    }

    @Override
    public void onDetach(){
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
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);

    }

    /**
     * When user stops moving the progress hanlder
     * */
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


    /**
     * Function to play a song
     * */
    public void  playSong(){
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
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
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
