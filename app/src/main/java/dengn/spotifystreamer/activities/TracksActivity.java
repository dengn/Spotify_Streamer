package dengn.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import dengn.spotifystreamer.R;
import dengn.spotifystreamer.fragments.SearchFragment;
import dengn.spotifystreamer.fragments.TracksFragment;
import dengn.spotifystreamer.utils.DebugConfig;

public class TracksActivity extends AppCompatActivity {

    private TracksFragment mTracksFragment;

    private String mArtistId;
    private String mArtistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        Intent intent = getIntent();
        mArtistId = intent.getStringExtra("artistId");
        mArtistName = intent.getStringExtra("artistName");
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mTracksFragment = TracksFragment.newInstance(mArtistId, mArtistName);
            transaction.replace(R.id.tracks_main, mTracksFragment);
            transaction.commit();
        }
        else{
            mTracksFragment = (TracksFragment)getSupportFragmentManager().getFragment(
                    savedInstanceState, "tracks_fragment");
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
        getMenuInflater().inflate(R.menu.menu_tracks, menu);
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
        else if(id==android.R.id.home){
            if(DebugConfig.DEBUG)
                Log.d(DebugConfig.TAG, "back clicked");
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
