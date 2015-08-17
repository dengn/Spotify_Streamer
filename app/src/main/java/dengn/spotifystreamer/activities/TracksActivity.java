package dengn.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.events.PlayerIntent;
import dengn.spotifystreamer.events.StateEvent;
import dengn.spotifystreamer.events.TickEvent;
import dengn.spotifystreamer.fragments.TracksFragment;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.services.MusicService;
import dengn.spotifystreamer.utils.LogHelper;

public class TracksActivity extends AppCompatActivity {

    private static final String TAG =
            LogHelper.makeLogTag(TracksActivity.class);

    private TracksFragment mTracksFragment;

    private String mArtistId;
    private String mArtistName;
    private boolean isTwoPane;

    @InjectView(R.id.toolbar)
    Toolbar tracksToolbar;

    private ArrayList<MyTrack> mTracks = new ArrayList<>();
    private int position = 0;

    private MenuItem nowPlayingItem;
    private MenuItem shareMusicItem;

    private MusicService.State mState;

    /*
    Only launched by phone, not tablets
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        EventBus.getDefault().register(this);

        ButterKnife.inject(this);

        Intent intent = getIntent();
        mArtistId = intent.getStringExtra("artistId");
        mArtistName = intent.getStringExtra("artistName");
        isTwoPane = intent.getBooleanExtra("isTwoPane", false);

        setSupportActionBar(tracksToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("Top 10 Tracks");
        getSupportActionBar().setSubtitle(mArtistName);

        if (savedInstanceState == null) {
            LogHelper.i(TAG, "recreate trackFragment");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mTracksFragment = TracksFragment.newInstance(mArtistId, mArtistName);
            transaction.replace(R.id.tracks_main, mTracksFragment);
            transaction.commit();
        } else {
            LogHelper.d(TAG, "reuse trackFragment");
            mTracksFragment = (TracksFragment) getSupportFragmentManager().getFragment(
                    savedInstanceState, "tracks_fragment");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    //Receive event with playIntent, from tracks fragment item click
    public void onEvent(PlayerIntent playerIntent) {

        LogHelper.i(TAG, "play intent received in Tracks Activity");
        mTracks = playerIntent.tracks;
        position = playerIntent.position;

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putParcelableArrayListExtra("tracks", mTracks);
        intent.putExtra("position", position);
        intent.putExtra("artistName", mArtistName);
        startActivity(intent);


    }

    public void onEvent(StateEvent event) {

        mState = event.state;
        if (nowPlayingItem != null && shareMusicItem!=null) {
            switch (mState) {
                case Playing:
                    nowPlayingItem.setVisible(true);
                    shareMusicItem.setVisible(true);
                    break;
                case Paused:
                    nowPlayingItem.setVisible(true);
                    shareMusicItem.setVisible(true);
                    break;
                case Prepared:
                    nowPlayingItem.setVisible(true);
                    shareMusicItem.setVisible(true);
                    break;
                case Retriving:
                    nowPlayingItem.setVisible(false);
                    shareMusicItem.setVisible(false);
                    break;
            }
        }

    }

    public void onEventMainThread(TickEvent event) {

        if (nowPlayingItem != null && shareMusicItem!=null) {
            nowPlayingItem.setVisible(true);
            shareMusicItem.setVisible(true);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        // fragment instance may be null
        if (mTracksFragment != null) {
            getSupportFragmentManager().putFragment(outState, "tracks_fragment", mTracksFragment);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        nowPlayingItem = menu.findItem(R.id.current_music);
        shareMusicItem = menu.findItem(R.id.share_music);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == android.R.id.home) {
            LogHelper.d(TAG, "back home clicked");

            finish();
            return true;
        } else if (id == R.id.current_music) {


            //not two pane, launch PlayFragment in a new activity
            Intent intent = new Intent(this, PlayerActivity.class);
            startActivity(intent);
            return true;

        }
        else if(id == R.id.share_music){

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mTracks.get(position).previewURL);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)));


        }


        return super.onOptionsItemSelected(item);
    }
}
