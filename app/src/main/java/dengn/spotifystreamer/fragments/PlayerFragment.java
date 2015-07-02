package dengn.spotifystreamer.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import dengn.spotifystreamer.events.MusicSetEvent;
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

    public static final String PLAYER_FRAGMENT_TAG = "player-fragment";

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

//    private String mArtistName;
//    private String mAlbumName;
//    private String mTrackName;
//    private int mTrackDuration;
//    private String mTrackPreview;
//    private String mAlbumImage;

    private ArrayList<MyTrack> mTracks = new ArrayList<>();
    private int position = 0;

    private long totalDuration = 0;
    private long currentDuration = 0;


    private MusicService.State mState = MusicService.State.Retriving;

    //service
    private Intent playIntent;



    public static PlayerFragment newInstance() {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static void showInContext(FragmentActivity context, boolean asDialog) {
        PlayerFragment player = PlayerFragment.newInstance();
        FragmentManager fm = context.getSupportFragmentManager();

        // Don't show more than one.
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(PLAYER_FRAGMENT_TAG);
        if (prev != null) ft.remove(prev);
        ft.commit();

        if (asDialog) {
            player.show(fm, PLAYER_FRAGMENT_TAG);
        } else {
            // http://developer.android.com/guide/topics/ui/dialogs.html#FullscreenDialog
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.replace(R.id.player_main, player, PLAYER_FRAGMENT_TAG).commit();
        }
    }

    public PlayerFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        EventBus.getDefault().register(this);

        LogHelper.i(DebugConfig.TAG, "playFragment on Create called");



    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        LogHelper.i(DebugConfig.TAG, "playFragment on Activity created called");


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

    public void onEvent(MusicSetEvent event){

        LogHelper.i(DebugConfig.TAG, "MusicSetEvent received in PlayerFragment");
        artistName.setText(event.myTrack.artistName);
        albumName.setText(event.myTrack.albumName);
        trackName.setText(event.myTrack.name);
        Picasso.with(getActivity()).load(event.myTrack.imageLargeURL).into(albumImage);
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

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.inject(this, view);

        // Listeners
        seekBar.setOnSeekBarChangeListener(this);



        playTime.setText("0:00");
        totalTime.setText("0:30");

        playIntent = new Intent(getActivity().getApplicationContext(), MusicService.class);


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



}
