package dengn.spotifystreamer.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
import dengn.spotifystreamer.events.StateEvent;
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

    private long totalDuration = 0;
    private long currentDuration = 0;


    private MusicService.State mState = MusicService.State.Retriving;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        EventBus.getDefault().register(this);

        LogHelper.i(DebugConfig.TAG, "playFragment on Create called");

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
            playIntent.putExtra("artistName", mArtistName);
            playIntent.setAction(MusicService.ACTION_PLAY);

            //Use application context to avoid runtime change, and no more activity context problem
            getActivity().getApplicationContext().startService(playIntent);


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


    }


    public void onEventMainThread(TickEvent event) {

        LogHelper.d(DebugConfig.TAG, "TickEvent received");

        playPause.setBackgroundResource(android.R.drawable.ic_media_pause);

        totalDuration = event.duration;
        currentDuration = event.currentPostion;

        // Displaying Total Duration time
        playTime.setText(PlayerUtils.milliSecondsToTimer(currentDuration));
        // Displaying time completed playing
        totalTime.setText(PlayerUtils.milliSecondsToTimer(totalDuration));

        // Updating progress bar
        int progress = PlayerUtils.getProgressPercentage(currentDuration, totalDuration);

        seekBar.setProgress(progress);


    }

    public void onEvent(StateEvent event) {

        mState = event.state;

    }

    public void onEvent(FinishEvent event) {
        LogHelper.i(DebugConfig.TAG, "FinishEvent received");
        if (event.isFinish) {
            LogHelper.i(DebugConfig.TAG, "set play icon2");

            //Play Next song
            //playPause.setBackgroundResource(android.R.drawable.ic_media_play);
            seekBar.setProgress(0);
            playTime.setText("0:00");
            mState = MusicService.State.Retriving;


            refreshUI(event.position);
            position = event.position;
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
                if (mState == MusicService.State.Playing) {
                    playIntent.setAction(MusicService.ACTION_PAUSE);
                    getActivity().getApplicationContext().startService(playIntent);

                    LogHelper.i(DebugConfig.TAG, "set play icon1");
                    playPause.setBackgroundResource(android.R.drawable.ic_media_play);
                }else if(mState == MusicService.State.Retriving){
                    playIntent.setAction(MusicService.ACTION_PLAY);
                    getActivity().getApplicationContext().startService(playIntent);

                    playPause.setBackgroundResource(android.R.drawable.ic_media_pause);
                }
                else{
                    playIntent.setAction(MusicService.ACTION_PLAY);
                    getActivity().getApplicationContext().startService(playIntent);

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
                playIntent.setAction(MusicService.ACTION_BACKWARD);
                getActivity().getApplicationContext().startService(playIntent);

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        forward.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View arg0) {
                playIntent.setAction(MusicService.ACTION_FORWARD);
                getActivity().getApplicationContext().startService(playIntent);
            }
        });


        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        next.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View arg0) {

                playIntent.setAction(MusicService.ACTION_NEXT);
                // check if next song is there or not
                if (position < mTracks.size() - 1) {
                    position++;
                    refreshUI(position);

                } else {
                    // play first song
                    position = 0;
                    refreshUI(position);

                }
                playIntent.putExtra("position", position);
                getActivity().getApplicationContext().startService(playIntent);

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        previous.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                playIntent.setAction(MusicService.ACTION_PREVIOUS);
                if (position > 0) {
                    position--;
                    refreshUI(position);

                } else {
                    // play last song
                    position = mTracks.size() - 1;
                    refreshUI(position);

                }
                playIntent.putExtra("position", position);
                getActivity().getApplicationContext().startService(playIntent);
            }
        });


        return view;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        int newCurrentDuration = PlayerUtils.progressToTimer(progress, totalDuration);
        seekBar.setProgress(progress);
        playTime.setText(PlayerUtils.milliSecondsToTimer(newCurrentDuration));
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {


    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {


        int newCurrentDuration = PlayerUtils.progressToTimer(seekBar.getProgress(), totalDuration);
        LogHelper.i(DebugConfig.TAG, "newCurrentDuration: "+newCurrentDuration);

        //update UI to the current scrolled position
        seekBar.setProgress(seekBar.getProgress());
        playTime.setText(PlayerUtils.milliSecondsToTimer(newCurrentDuration));
        // forward or backward to certain seconds
        playIntent.setAction(MusicService.ACTION_SET_POSITION);
        playIntent.putExtra("newCurrentPosition", newCurrentDuration);
        getActivity().getApplicationContext().startService(playIntent);

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
