package dengn.spotifystreamer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import dengn.spotifystreamer.events.FinishEvent;
import dengn.spotifystreamer.events.MusicSetEvent;
import dengn.spotifystreamer.events.StateEvent;
import dengn.spotifystreamer.events.TickEvent;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.utils.LogHelper;

/**
 * Created by OLEDCOMM on 24/06/2015.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String TAG =
            LogHelper.makeLogTag(MusicService.class);

    private static final int seekBackwardTime = 1000;
    private static final int seekForwardTime = 1000;


    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_FORWARD = "ACTION_FORWARD";
    public static final String ACTION_BACKWARD = "ACTION_BACKWARD";
    public static final String ACTION_SET_POSITION = "ACTION_SET_POSITION";
    public static final String ACTION_RESHOWN = "ACTION_RESHOWN";

    public enum State {
        Retriving,
        Prepared,
        Playing,
        Paused
    }

    ;

    State mState = State.Retriving;

    // our media player
    private MediaPlayer mPlayer = null;

    private ArrayList<MyTrack> mTracks = new ArrayList<>();

    private String mArtistName = "";

    private Timer mTimer;

    private int position = 0;

    private IBinder mBinder = new MusicBinder();

    public MusicService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();


        mPlayer = new MediaPlayer();
        initMusicPlayer();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        LogHelper.i(TAG, "Received start id " + startId + ": " + intent);

        String action = intent.getAction();

        if (intent.getParcelableArrayListExtra("tracks") != null) {

            mTracks = intent.getParcelableArrayListExtra("tracks");
            if (position != intent.getIntExtra("position", 0)) {

                LogHelper.i(TAG, "old position: " + position);
                LogHelper.i(TAG, "new position: " + intent.getIntExtra("position", 0));

                mState = State.Retriving;
                position = intent.getIntExtra("position", 0);
            }
        }

        //Check if it's the same music playing


        LogHelper.i(TAG, "old artistName: " + mArtistName);
        LogHelper.i(TAG, "new artistName: " + intent.getStringExtra("artistName"));
        if (intent.getStringExtra("artistName") != null) {

            if (!mArtistName.equals(intent.getStringExtra("artistName"))) {
                mState = State.Retriving;
                mArtistName = intent.getStringExtra("artistName");
            }
        }


        if (action != null) {

            if (action.equals(ACTION_PLAY)) processPlay();
            else if (action.equals(ACTION_PAUSE)) processPause();
            else if (action.equals(ACTION_NEXT)) processNext();
            else if (action.equals(ACTION_PREVIOUS)) processPrevious();
            else if (action.equals(ACTION_FORWARD)) processForward();
            else if (action.equals(ACTION_BACKWARD)) processBackward();
            else if (action.equals(ACTION_RESHOWN)) {
                EventBus.getDefault().post(new MusicSetEvent(mTracks.get(position)));
            } else if (action.equals(ACTION_SET_POSITION)) {
                int currentPosition = intent.getIntExtra("newCurrentPosition", 0);
                processSetPosition(currentPosition);
            }

        }


        // Means we started the service, but don't want it to
        // restart in case it's killed.
        return START_NOT_STICKY;
    }

    public void initMusicPlayer() {
        //set player properties
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
    }

    private void processPlay() {
        switch (mState) {
            case Retriving:
                setMusic();
                break;
            case Prepared:
                play();
                break;
            case Playing:
                break;
            case Paused:
                play();
                break;

        }


    }

    private void processPause() {

        switch (mState) {

            case Playing:
                pause();
                break;

        }


    }

    private void processNext() {

        if (position < mTracks.size() - 1) {
            position++;

        } else {
            // play first song
            position = 0;

        }

        setMusic();


    }

    private void processPrevious() {
        if (position > 0) {
            position--;

        } else {
            // play last song
            position = mTracks.size() - 1;

        }

        setMusic();


    }

    private void processForward() {

        long currentPosition = mPlayer.getCurrentPosition();
        // check if seekForward time is lesser than song duration
        if (currentPosition + seekForwardTime <= mPlayer.getDuration()) {
            // forward song
            mPlayer.seekTo((int) currentPosition + seekForwardTime);
        } else {
            // forward to end position
            mPlayer.seekTo((int) mPlayer.getDuration());
        }

        switch (mState) {
            case Paused:
                play();
                break;
            case Playing:
                break;

        }


    }

    private void processBackward() {
        long currentPosition = mPlayer.getCurrentPosition();
        // check if seekBackward time is greater than 0 sec
        if ((int) currentPosition - seekBackwardTime >= 0) {
            // forward song
            mPlayer.seekTo((int) currentPosition - seekBackwardTime);
        } else {
            // backward to starting position
            mPlayer.seekTo(0);
        }

        switch (mState) {
            case Paused:
                play();
                break;
            case Playing:
                break;

        }

    }

    private void processSetPosition(int position) {
        mPlayer.seekTo(position);
        switch (mState) {
            case Paused:
                play();
                break;
            case Playing:
                break;

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public State getCurrentState() {
        return mState;
    }

    //release resources when unbind
    @Override
    public void onDestroy() {
        super.onDestroy();
        LogHelper.i(TAG, "on Destroy called");
        stopTicking();
        mPlayer.stop();
        mPlayer.release();
    }


    public void setSongPosition(int position) {
        this.position = position;
    }

    private void setMusic() {
        try {

            mPlayer.reset();
            mPlayer.setDataSource(mTracks.get(position).previewURL);
            EventBus.getDefault().post(new MusicSetEvent(mTracks.get(position)));
            LogHelper.i(TAG, "set music event fired ");

            //We are streaming online music, it should be asynchronous, otherwise it will take too much time on the UI thread
            mPlayer.prepareAsync();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void play() {
        LogHelper.i(TAG, "play called");

        if (mPlayer != null)
            mPlayer.start();
        startTicking();
        mState = State.Playing;
        EventBus.getDefault().post(new StateEvent(State.Playing));
    }

    private void pause() {
        LogHelper.i(TAG, "pause called");
        if (mPlayer.isPlaying() && mPlayer != null)
            mPlayer.pause();
        stopTicking();
        mState = State.Paused;
        EventBus.getDefault().post(new StateEvent(State.Paused));
    }


    private void startTicking() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mPlayer.isPlaying())
                        EventBus.getDefault().post(new TickEvent(mPlayer.getDuration(), mPlayer.getCurrentPosition()));
                }
            }, 100, 100);
        }

    }

    private void stopTicking() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        LogHelper.i(TAG, "music complete");
        mState = State.Retriving;
        EventBus.getDefault().post(new FinishEvent(true));
        processNext();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogHelper.i(TAG, "music error");
        mState = State.Retriving;
        stopTicking();
        EventBus.getDefault().post(new FinishEvent(true));
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mState = State.Prepared;
        play();
    }

    //binder
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
