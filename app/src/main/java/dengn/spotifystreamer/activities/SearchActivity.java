package dengn.spotifystreamer.activities;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import dengn.spotifystreamer.R;
import dengn.spotifystreamer.fragments.SearchFragment;
import dengn.spotifystreamer.utils.DebugConfig;

public class SearchActivity extends AppCompatActivity {


    private SearchFragment mSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mSearchFragment = SearchFragment.newInstance();
            transaction.replace(R.id.search_main, mSearchFragment);
            transaction.commit();
        }
        else{
            mSearchFragment = (SearchFragment)getSupportFragmentManager().getFragment(
                    savedInstanceState, "search_fragment");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        // fragment instance may be null
        if (mSearchFragment != null) {
            getSupportFragmentManager().putFragment(outState, "search_fragment", mSearchFragment);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
