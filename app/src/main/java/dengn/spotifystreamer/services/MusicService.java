package dengn.spotifystreamer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.activities.SearchActivity;
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


    public static final int NOTIFICATION_ID = 3000;

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

        if (intent.getParcelableArrayListExtra("tracks") != null){

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
        return null;
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

        //showNotificationUsingCustomLayout();

        if (mPlayer != null)
            mPlayer.start();
        startTicking();
        mState = State.Playing;
        EventBus.getDefault().post(new StateEvent(State.Playing));
    }

    private void pause() {
        LogHelper.i(TAG, "pause called");

        //showNotificationUsingCustomLayout();

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


    /**
     * Notifications
     */
    private void showNotificationUsingCustomLayout() {

        //New Remote View
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notification_playback);
        remoteView.setTextViewText(R.id.track_name, mTracks.get(position).name);
        remoteView.setTextViewText(R.id.artist_name, mTracks.get(position).artistName);

        //Playback controls
        //Previous Track Intent

        Intent playPreviousTrackIntent = new Intent(this, MusicService.class);
        playPreviousTrackIntent.setAction(ACTION_PREVIOUS);

        remoteView.setOnClickPendingIntent(
                R.id.play_previous_track,
                PendingIntent.getService(this, 0, playPreviousTrackIntent, 0)
        );


        Intent pauseTrackIntent = new Intent(this, MusicService.class);
        pauseTrackIntent.setAction(ACTION_PAUSE);
        Intent resumeTrackIntent = new Intent(this, MusicService.class);
        resumeTrackIntent.setAction(ACTION_PLAY);
        //Resume/Pause
        remoteView.setViewVisibility(R.id.pause_track, View.VISIBLE);
        remoteView.setViewVisibility(R.id.resume_track, View.VISIBLE);
        if(mPlayer != null && mPlayer.isPlaying()) {
            remoteView.setViewVisibility(R.id.resume_track, View.GONE);
            remoteView.setOnClickPendingIntent(
                    R.id.pause_track,
                    PendingIntent.getService(this, 0, pauseTrackIntent, 0)
            );
        }
        else {
            remoteView.setViewVisibility(R.id.pause_track, View.GONE);
            remoteView.setOnClickPendingIntent(
                    R.id.resume_track,
                    PendingIntent.getService(this, 0, resumeTrackIntent, 0)
            );
        }


        Intent nextTrackIntent = new Intent(this, MusicService.class);
        nextTrackIntent.setAction(ACTION_NEXT);
        //Next Track Intent
        remoteView.setOnClickPendingIntent(
                R.id.play_next_track,
                PendingIntent.getService(this, 0, nextTrackIntent, 0)
        );

        //Content action
        //Show App Intent
        Intent showAppIntent = new Intent(this, SearchActivity.class);
        showAppIntent.setAction(Intent.ACTION_MAIN);
        showAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent showAppPendingIntent = PendingIntent.getActivity(this, 0, showAppIntent, 0);

        //Prepare notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContent(remoteView)
                .setContentIntent(showAppPendingIntent);

        //Check if ongoing notification
        notificationBuilder.setOngoing(mPlayer != null && mPlayer.isPlaying());

        //Show playback controls in lockscreen
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean showPlaybackControlsInLockScreen = sharedPreferences.getBoolean(PREF_SHOW_PLAYBACK_CONTROLS_IN_LOCKSCREEN, true);
        boolean showPlaybackControlsInLockScreen = true;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH && showPlaybackControlsInLockScreen) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        //Display notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);

        //Thumbnail
//        String thumbnailUrl = Utils.getThumbnailUrl(mCurrentTrack.album.images, 0);
//        if(thumbnailUrl != null)
//            Picasso.with(this).load(thumbnailUrl).into(remoteView, R.id.album_thumbnail, NOTIFICATION_ID, notification);
        Picasso.with(this)
                .load(mTracks.get(position).imageSmallURL)
                .into(remoteView, R.id.album_thumbnail, NOTIFICATION_ID, notification);

    }
}
