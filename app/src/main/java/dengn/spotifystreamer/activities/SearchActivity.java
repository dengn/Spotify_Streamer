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
import dengn.spotifystreamer.events.TrackIntent;
import dengn.spotifystreamer.fragments.PlayerFragment;
import dengn.spotifystreamer.fragments.SearchFragment;
import dengn.spotifystreamer.fragments.TracksFragment;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.utils.DebugConfig;
import dengn.spotifystreamer.utils.LogHelper;

public class SearchActivity extends AppCompatActivity {


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Register EventBus
        EventBus.getDefault().register(this);

        TrackIntent trackIntent = EventBus.getDefault().removeStickyEvent(TrackIntent.class);

        if (findViewById(R.id.tracks_main) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // If is two pane, init a TracksFragment in the same activity
            if (savedInstanceState == null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                mTracksFragment = TracksFragment.newInstance("", "");
                transaction.replace(R.id.tracks_main, mTracksFragment);
                transaction.commit();
            }
            else{
                mTracksFragment = (TracksFragment) getSupportFragmentManager().getFragment(
                        savedInstanceState, "track_fragment");
            }
        }
        else{
            mTwoPane = false;
        }


        //No matter two pane or not, we need to add SearchFragment
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mSearchFragment = SearchFragment.newInstance();
            transaction.replace(R.id.search_main, mSearchFragment);
            transaction.commit();

        }
        else{
            mSearchFragment = (SearchFragment) getSupportFragmentManager().getFragment(
                    savedInstanceState, "search_fragment");
        }

        ButterKnife.inject(this);
        setSupportActionBar(searchToolbar);



    }



    @Override
    public void onDestroy(){
        super.onDestroy();
        //Unregister EventBus
        EventBus.getDefault().unregister(this);
    }


    //Receive event with TrackIntent object, from Item Click in SearchFragment
    public void onEvent(TrackIntent trackIntent){
        if(mTwoPane) {
            //Two pane, refresh TrackFragment by using new data get from SearchFragment
            mArtistId = trackIntent.artistId;
            mArtistName = trackIntent.artistName;

            mTracksFragment.onNewDataRefresh(mArtistId, mArtistName);
        }
        else{
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
    public void onEvent(PlayerIntent playerIntent){

        LogHelper.i(DebugConfig.TAG, "play intent received");
        mTracks = playerIntent.tracks;
        position = playerIntent.position;

        //Can only be from two pane situation, launch player fragment as dialog.
        PlayerFragment playerFragment = PlayerFragment.newInstance(mTracks, position);
        playerFragment.show(getSupportFragmentManager().beginTransaction(), "Player");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        // fragment instance may be null
        if (mSearchFragment != null) {
            getSupportFragmentManager().putFragment(outState, "search_fragment", mSearchFragment);
        }
        if(mTracksFragment!=null) {
            getSupportFragmentManager().putFragment(outState, "tracks_fragment", mTracksFragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
