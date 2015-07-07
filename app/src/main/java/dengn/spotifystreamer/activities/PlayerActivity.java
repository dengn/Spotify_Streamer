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
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.fragments.PlayerFragment;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.services.MusicService;
import dengn.spotifystreamer.utils.LogHelper;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG =
            LogHelper.makeLogTag(PlayerActivity.class);

    @InjectView(R.id.toolbar)
    Toolbar playerToolbar;



    private String mArtistName;
    private String mAlbumName;
    private String mTrackName;
    private int mTrackDuration;
    private String mTrackPreview;
    private String mAlbumImage;

    private ArrayList<MyTrack> mTracks = new ArrayList<>();
    private int position = 0;

    private PlayerFragment mPlayerFragment;


    private Intent playIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ButterKnife.inject(this);

        setSupportActionBar(playerToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //PlayerFragment.showInContext(this, false);


        Intent intent = getIntent();

        playIntent = new Intent(this, MusicService.class);
        if(intent.getStringExtra("artistName")!=null && intent.getParcelableArrayListExtra("tracks")!=null) {
            mTracks = intent.getParcelableArrayListExtra("tracks");
            position = intent.getIntExtra("position", 0);
            mArtistName = intent.getStringExtra("artistName");

            playIntent.putParcelableArrayListExtra("tracks", mTracks);
            playIntent.putExtra("position", position);
            playIntent.putExtra("artistName", mArtistName);
            playIntent.setAction(MusicService.ACTION_PLAY);
            startService(playIntent);
        }
        else{
            playIntent.setAction(MusicService.ACTION_RESHOWN);
            startService(playIntent);
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mPlayerFragment = PlayerFragment.newInstance();
            transaction.add(R.id.player_main, mPlayerFragment);
            transaction.commit();
        }
        else{
            mPlayerFragment = (PlayerFragment)getSupportFragmentManager().getFragment(
                    savedInstanceState, "player_fragment");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        // fragment instance may be null
        if (mPlayerFragment != null) {
            getSupportFragmentManager().putFragment(outState, "player_fragment", mPlayerFragment);
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
        else if(id==android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
