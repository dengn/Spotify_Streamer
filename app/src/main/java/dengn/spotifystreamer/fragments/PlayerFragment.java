package dengn.spotifystreamer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.events.FinishEvent;
import dengn.spotifystreamer.events.TickEvent;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.services.MusicService;
import dengn.spotifystreamer.utils.DebugConfig;
import dengn.spotifystreamer.utils.LogHelper;
import dengn.spotifystreamer.utils.PlayerUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

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

    //service
    private MusicService musicService;
    private Intent playIntent;

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

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;

            LogHelper.i(DebugConfig.TAG, "service bound");
            //get service
            musicService = binder.getService();
            musicService.playSong();
//            Intent intent = new Intent(getActivity(), MusicService.class);
//            intent.setAction(MusicService.ACTION_PLAY);
//            getActivity().getApplicationContext().startService(intent);

            if (musicService.isPlaying()) {
                playPause.setBackgroundResource(android.R.drawable.ic_media_pause);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        EventBus.getDefault().register(this);

        if (getArguments() != null) {

            mTracks = getArguments().getParcelableArrayList("tracks");
            position = getArguments().getInt("position");

            mArtistName = mTracks.get(position).artistName;
            mAlbumName = mTracks.get(position).albumName;
            mTrackName = mTracks.get(position).name;
            mTrackDuration = MyTrack.PREVIEW_LENGTH_DEFAULT;
            mTrackPreview = mTracks.get(position).previewURL;
            mAlbumImage = mTracks.get(position).imageLargeURL;

            LogHelper.d(DebugConfig.TAG, "mAlbumImage " + mAlbumImage);
        }

        if (playIntent == null) {

            playIntent = new Intent(getActivity(), MusicService.class);
            playIntent.putParcelableArrayListExtra("tracks", mTracks);
            playIntent.putExtra("position", position);

            //Use application context to avoid runtime change, and no more activity context problem
            getActivity().getApplicationContext().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);


        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        LogHelper.i(DebugConfig.TAG, "playFragment on dismiss called");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogHelper.i(DebugConfig.TAG, "playFragment on stop called");
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();

        LogHelper.i(DebugConfig.TAG, "playFragment on destory view called");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogHelper.i(DebugConfig.TAG, "playFragment on destory called");

        EventBus.getDefault().unregister(this);

        getActivity().getApplicationContext().unbindService(musicConnection);

    }


    public void onEventMainThread(TickEvent event) {

        LogHelper.i(DebugConfig.TAG, "TickEvent received");

        playPause.setBackgroundResource(android.R.drawable.ic_media_pause);

        long totalDuration = event.duration;
        long currentDuration = event.currentPostion;

        // Displaying Total Duration time
        playTime.setText(PlayerUtils.milliSecondsToTimer(currentDuration));
        // Displaying time completed playing
        totalTime.setText(PlayerUtils.milliSecondsToTimer(totalDuration));

        // Updating progress bar
        int progress = PlayerUtils.getProgressPercentage(currentDuration, totalDuration);

        seekBar.setProgress(progress);


    }

    public void onEventMainThread(FinishEvent event) {
        LogHelper.i(DebugConfig.TAG, "FinishEvent received");
        if (event.isFinish) {
            LogHelper.i(DebugConfig.TAG, "set play icon2");

            playPause.setBackgroundResource(android.R.drawable.ic_media_play);
            seekBar.setProgress(0);
            playTime.setText("0:00");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.inject(this, view);

        // Listeners
        seekBar.setOnSeekBarChangeListener(this);


        artistName.setText(mArtistName);
        albumName.setText(mAlbumName);
        trackName.setText(mTrackName);
        playTime.setText("0:00");
        totalTime.setText("0:30");

        Picasso.with(getActivity()).load(mAlbumImage).into(albumImage);


        playPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check for already playing
                if (musicService.isPlaying()) {
                    musicService.pause();
                    LogHelper.i(DebugConfig.TAG, "set play icon1");
                    playPause.setBackgroundResource(android.R.drawable.ic_media_play);
                } else {
                    musicService.play();
                    playPause.setBackgroundResource(android.R.drawable.ic_media_pause);
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
                long currentPosition = musicService.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if ((int) currentPosition - seekBackwardTime >= 0) {
                    // forward song
                    musicService.seekTo((int) currentPosition - seekBackwardTime);
                } else {
                    // backward to starting position
                    musicService.seekTo(0);
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
                long currentPosition = musicService.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekForwardTime <= musicService.getDuration()) {
                    // forward song
                    musicService.seekTo((int) currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    musicService.seekTo((int) musicService.getDuration());
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
                    musicService.setSongPosition(position);
                    musicService.playSong();
                } else {
                    // play first song
                    position = 0;
                    refreshUI(position);
                    musicService.setSongPosition(position);
                    musicService.playSong();
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
                    musicService.setSongPosition(position);
                    musicService.playSong();
                } else {
                    // play last song
                    position = mTracks.size() - 1;
                    refreshUI(position);
                    musicService.setSongPosition(position);
                    musicService.playSong();
                }

            }
        });


        return view;
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
        //mHandler.removeCallbacks(mUpdateTimeTask);

    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        long totalDuration = musicService.getDuration();
        int currentPosition = PlayerUtils.progressToTimer(seekBar.getProgress(), totalDuration);

        //update UI to the current scrolled position
        seekBar.setProgress(seekBar.getProgress());
        playTime.setText(PlayerUtils.milliSecondsToTimer(currentPosition));
        // forward or backward to certain seconds
        musicService.seekTo(currentPosition);

    }

    private void refreshUI(int newPosition) {
        mArtistName = mTracks.get(newPosition).artistName;
        mAlbumName = mTracks.get(newPosition).albumName;
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


}
