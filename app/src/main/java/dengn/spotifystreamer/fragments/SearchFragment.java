package dengn.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.adapters.ArtistListAdapter;
import dengn.spotifystreamer.events.TrackIntent;
import dengn.spotifystreamer.listener.RecyclerItemClickListener;
import dengn.spotifystreamer.models.MyArtist;
import dengn.spotifystreamer.utils.DebugConfig;
import dengn.spotifystreamer.utils.ImageUtils;
import dengn.spotifystreamer.utils.LogHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {

    private static final String TAG =
            LogHelper.makeLogTag(SearchFragment.class);


    //UI components


    @InjectView(R.id.search_text)
    TextInputLayout searchText;

    @InjectView(R.id.artist_list)
    RecyclerView artistList;

    @InjectView(R.id.search_progressbar)
    ProgressBar progressBar;


    //Adapter
    private ArtistListAdapter artistListAdapter;

    private ArrayList<MyArtist> mArtists = new ArrayList<>();
    //Spotify
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the fragment's state here

        LogHelper.d(DebugConfig.TAG, "save data to bundle");
        if (mArtists != null) {
            outState.putParcelableArrayList("artists", mArtists);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            // Restore the fragment's state here
            mArtists = savedInstanceState.getParcelableArrayList("artists");

            LogHelper.d(DebugConfig.TAG, "get data from saved bundle");
        }

        //Ui init
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.inject(this, view);

        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.INVISIBLE);


        artistList.setLayoutManager(new LinearLayoutManager(getActivity()));
        artistListAdapter = new ArtistListAdapter(getActivity(), mArtists);
        artistList.setAdapter(artistListAdapter);


        artistList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {


            @Override
            public void onItemClick(View view, int position) {

                if (mArtists.get(position).id != null) {

                    //Post event with TrackIntent back to SearchActivity
                    EventBus.getDefault().post(new TrackIntent(mArtists.get(position).id, mArtists.get(position).name));
                }
            }
        }));

        setUpSearchText();


        return view;
    }


    private void setUpSearchText() {
        EditText editText = searchText.getEditText();
        searchText.setHint("Type the Artist Name.");

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String artistName = s.toString();
                if (artistName.length() > 1) {
                    spotifySearchArtists(artistName);
                    progressBar.setVisibility(View.VISIBLE);
                }

            }
        });
    }


    private void showToast(final String msg) {
        //Makes sure that fragment is attached to Activity already, and getActivity will not return null
        if (isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void spotifySearchArtists(String artistName) {
        //Sportify calbacks are in worker thread
        spotify.searchArtists(artistName, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {


                if (artistsPager.artists.items == null || artistsPager.artists.items.size() == 0) {

                    if (DebugConfig.DEBUG)
                        Log.d(DebugConfig.TAG, "no artists found");

                    MyArtist artist = new MyArtist(getString(R.string.artists_not_found), null, null);
                    mArtists.clear();
                    mArtists.add(artist);
                } else {
                    mArtists.clear();
                    for (Artist item : artistsPager.artists.items) {
                        MyArtist artist = new MyArtist(item.name, ImageUtils.getImageUrl(item.images, ImageUtils.IMAGE_SMALL), item.id);
                        mArtists.add(artist);
                    }
                }

                //Bind data to list and show
                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            artistListAdapter.refresh(mArtists);

                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {


                mArtists.clear();
                //Error happens, Clear the list
                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressBar.setVisibility(View.INVISIBLE);
                            artistListAdapter.refresh(mArtists);

                        }
                    });
                }

                switch (error.getKind()) {
                    case NETWORK:
                        showToast("Nwtwork error");
                        break;
                    case CONVERSION:
                        showToast("Please retry");
                        break;
                    case HTTP:
                        showToast("Error Code:"
                                + String.valueOf(error.getResponse().getStatus())
                                + "Error Raison:" + error.getResponse().getReason());
                        break;
                    case UNEXPECTED:
                        showToast("Unknown Error");
                        break;
                }
                showToast("failure:" + error.getKind());

            }
        });
    }

}
