package dengn.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.events.PlayerIntent;
import dengn.spotifystreamer.events.StateEvent;
import dengn.spotifystreamer.events.TickEvent;
import dengn.spotifystreamer.events.TrackIntent;
import dengn.spotifystreamer.fragments.PlayerFragment;
import dengn.spotifystreamer.fragments.SearchFragment;
import dengn.spotifystreamer.fragments.TracksFragment;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.services.MusicService;
import dengn.spotifystreamer.utils.LogHelper;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG =
            LogHelper.makeLogTag(SearchActivity.class);


    @InjectView(R.id.toolbar)
    Toolbar searchToolbar;

    private SearchFragment mSearchFragment;

    private TracksFragment mTracksFragment;

    private String mArtistId;
    private String mArtistName;

    private ArrayList<MyTrack> mTracks = new ArrayList<>();
    private int position = 0;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private MenuItem nowPlayingItem;
    private MenuItem shareMusicItem;

    private Intent playIntent;

    private MusicService.State mState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        //Register EventBus
        EventBus.getDefault().register(this);


        if (findViewById(R.id.tracks_main) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // If is two pane, init a TracksFragment in the same activity
            //mTracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
            if (savedInstanceState == null) {
                LogHelper.i(TAG, "recreate mTracksFragment");

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                mTracksFragment = TracksFragment.newInstance("", "");
                transaction.add(R.id.tracks_main, mTracksFragment);
                transaction.commit();
            } else {
                LogHelper.i(TAG, "retrieve mTracksFragment");
                mTracksFragment = (TracksFragment) getSupportFragmentManager().getFragment(
                        savedInstanceState, "track_fragment");
            }
        } else {
            mTwoPane = false;
        }


        //No matter two pane or not, we need to add SearchFragment

        //mSearchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_search);

        if (savedInstanceState == null) {
            LogHelper.i(TAG, "recreate mSearchFragment");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mSearchFragment = SearchFragment.newInstance();
            transaction.add(R.id.search_main, mSearchFragment);
            transaction.commit();

        } else {
            LogHelper.i(TAG, "retrieve mSearchFragment");
            mSearchFragment = (SearchFragment) getSupportFragmentManager().getFragment(
                    savedInstanceState, "search_fragment");
        }

        ButterKnife.inject(this);
        setSupportActionBar(searchToolbar);


        if (savedInstanceState != null) {
            mState = (MusicService.State) savedInstanceState.getSerializable("state");
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


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unregister EventBus
        EventBus.getDefault().unregister(this);

        LogHelper.i(TAG, "Search Activity destroyed");
    }


    //Receive event with TrackIntent object, from Item Click in SearchFragment
    public void onEvent(TrackIntent trackIntent) {
        LogHelper.i(TAG, "track intent received in SearchActivity");
        if (mTwoPane) {
            //Two pane, refresh TrackFragment by using new data get from SearchFragment
            mArtistId = trackIntent.artistId;
            mArtistName = trackIntent.artistName;

            LogHelper.i(TAG, "artistId: " + mArtistId);
            LogHelper.i(TAG, "artistName: " + mArtistName);

            LogHelper.i(TAG, "mTracksFragment is null: " + (mTracksFragment == null));
            mTracksFragment.onNewDataRefresh(mArtistId, mArtistName);
        } else {
            //Not two pane, start Tracks activity
            mArtistId = trackIntent.artistId;
            mArtistName = trackIntent.artistName;

            Intent intent = new Intent(this, TracksActivity.class);
            intent.putExtra("artistId", mArtistId);
            intent.putExtra("artistName", mArtistName);
            intent.putExtra("isTwoPane", mTwoPane);
            startActivity(intent);
        }
    }

    //Receive event with PlayIntent object, from Item Click in TracksFragment
    public void onEvent(PlayerIntent playerIntent) {

        mTracks = playerIntent.tracks;
        position = playerIntent.position;

        LogHelper.i(TAG, "play intent received in SearchActivity");
        if (mTwoPane) {

            PlayerFragment player = PlayerFragment.newInstance();
            FragmentManager fm = this.getSupportFragmentManager();
            player.show(fm, PlayerFragment.PLAYER_FRAGMENT_TAG);

            playIntent = new Intent(this, MusicService.class);
            playIntent.putParcelableArrayListExtra("tracks", mTracks);
            playIntent.putExtra("position", position);
            playIntent.putExtra("artistName", mArtistName);
            playIntent.setAction(MusicService.ACTION_PLAY);

            //Use application context to avoid runtime change, and no more activity context problem
            this.startService(playIntent);


        }
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

        outState.putSerializable("state", mState);

        //Save the fragment's instance
        // fragment instance may be null
        if (mSearchFragment != null) {
            LogHelper.i(TAG, "search fragment saved");
            getSupportFragmentManager().putFragment(outState, "search_fragment", mSearchFragment);
        }
        if (mTracksFragment != null) {
            LogHelper.i(TAG, "tracks fragment saved");
            getSupportFragmentManager().putFragment(outState, "track_fragment", mTracksFragment);
        }
        super.onSaveInstanceState(outState);
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
        } else if (id == R.id.current_music) {
            if (mTwoPane) {
                //Can only be from two pane situation, launch player fragment as dialog.
                PlayerFragment player = PlayerFragment.newInstance();
                FragmentManager fm = this.getSupportFragmentManager();
                player.show(fm, PlayerFragment.PLAYER_FRAGMENT_TAG);

                playIntent = new Intent(this, MusicService.class);
                playIntent.setAction(MusicService.ACTION_RESHOWN);
                startService(playIntent);
            } else {
                Intent intent = new Intent(this, PlayerActivity.class);
                startActivity(intent);
                playIntent = new Intent(this, MusicService.class);
                playIntent.setAction(MusicService.ACTION_RESHOWN);
                startService(playIntent);
            }
            return true;
        } else if(id == R.id.share_music){

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mTracks.get(position).previewURL);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)));


        }

        return super.onOptionsItemSelected(item);
    }
}
