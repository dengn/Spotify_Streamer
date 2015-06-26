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
import dengn.spotifystreamer.events.TickEvent;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.utils.DebugConfig;
import dengn.spotifystreamer.utils.LogHelper;

/**
 * Created by OLEDCOMM on 24/06/2015.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {


    // our media player
    private MediaPlayer mPlayer = null;

    private ArrayList<MyTrack> mTracks = new ArrayList<>();

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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mTracks = intent.getParcelableArrayListExtra("tracks");
        position = intent.getIntExtra("position", 0);
        return mBinder;
    }

    public void setSongPosition(int position){
        this.position = position;
    }

    public void playSong() {
        LogHelper.i(DebugConfig.TAG, "play song called");
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mTracks.get(position).previewURL);
            mPlayer.prepare();
            mPlayer.start();
            startTicking();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play(){
        LogHelper.i(DebugConfig.TAG, "play called");
        if(mPlayer!=null)
            mPlayer.start();
        startTicking();
    }

    public void pause() {
        LogHelper.i(DebugConfig.TAG, "pause called");
        if (mPlayer.isPlaying() && mPlayer != null)
            mPlayer.pause();
        stopTicking();
    }

    public void seekTo(int position){
        mPlayer.seekTo(position);
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public long getDuration(){
        return mPlayer.getDuration();
    }

    public long getCurrentPosition(){
        return mPlayer.getCurrentPosition();
    }

    private void startTicking(){
        if(mTimer==null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(mPlayer.isPlaying())
                        EventBus.getDefault().post(new TickEvent(getDuration(), getCurrentPosition()));
                }
            }, 0, 100);
        }

    }

    private void stopTicking() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent) {
        stopTicking();
        mPlayer.stop();
        mPlayer.release();
        return false;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        LogHelper.i(DebugConfig.TAG, "music complete");
        stopTicking();
        EventBus.getDefault().post(new FinishEvent(true));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    //binder
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
