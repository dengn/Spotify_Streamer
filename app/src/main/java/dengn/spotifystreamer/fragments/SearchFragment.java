package dengn.spotifystreamer.fragments;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.activities.TracksActivity;
import dengn.spotifystreamer.adapters.ArtistListAdapter;
import dengn.spotifystreamer.listener.RecyclerItemClickListener;
import dengn.spotifystreamer.models.MyArtist;
import dengn.spotifystreamer.utils.DebugConfig;
import dengn.spotifystreamer.utils.ImageUtils;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {


    //UI components
    @InjectView(R.id.search_toolbar)
    Toolbar searchToolbar;

    @InjectView(R.id.search_text)
    TextInputLayout searchText;

    @InjectView(R.id.artist_list)
    RecyclerView artistList;

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


    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the fragment's state here

        if (DebugConfig.DEBUG)
            Log.d(DebugConfig.TAG, "save data to bundle");
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            // Restore the fragment's state here
            mArtists = savedInstanceState.getParcelableArrayList("artists");

            if (DebugConfig.DEBUG)
                Log.d(DebugConfig.TAG, "get data from saved bundle");
        }

        //Ui init
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.inject(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(searchToolbar);
        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        artistList.setLayoutManager(new LinearLayoutManager(getActivity()));
        artistListAdapter = new ArtistListAdapter(getActivity(), mArtists);
        artistList.setAdapter(artistListAdapter);

        artistList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {


            @Override
            public void onItemClick(View view, int position) {

                if (mArtists.get(position).id != null) {

                    Intent intent = new Intent(getActivity(), TracksActivity.class);
                    intent.putExtra("artistId", mArtists.get(position).id);
                    intent.putExtra("artistName", mArtists.get(position).name);
                    startActivity(intent);
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
                if (artistName.length() > 1)
                    spotifySearchArtists(artistName);

            }
        });
    }

    private void showToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        artistListAdapter.refresh(mArtists);

                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

                mArtists.clear();
                //Error happens, Clear the list
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        artistListAdapter.refresh(mArtists);

                    }
                });

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
