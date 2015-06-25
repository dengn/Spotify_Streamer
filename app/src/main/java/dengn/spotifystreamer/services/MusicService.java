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

    private int position = 0;

    private IBinder mBinder = new MusicBinder();


    public MusicService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
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


    public void playSong() {
        LogHelper.i(DebugConfig.TAG, "play song called");
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mTracks.get(position).previewURL);
            mPlayer.prepare();
            mPlayer.start();
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
    }

    public void pause() {
        LogHelper.i(DebugConfig.TAG, "pause called");
        if (mPlayer.isPlaying() && mPlayer != null)
            mPlayer.pause();
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

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent) {
        mPlayer.stop();
        mPlayer.release();
        return false;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {

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
