package dengn.spotifystreamer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.activities.TracksActivity;
import dengn.spotifystreamer.adapters.ArtistListAdapter;
import dengn.spotifystreamer.listener.RecyclerItemClickListener;
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

    private ArtistsPager mResults = new ArtistsPager();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Ui init
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.inject(this, view);

        ((AppCompatActivity)getActivity()).setSupportActionBar(searchToolbar);
        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Pager<Artist> artists = new Pager<>();
        mResults.artists = artists;

        artistList.setLayoutManager(new LinearLayoutManager(getActivity()));
        artistListAdapter = new ArtistListAdapter(getActivity(), mResults.artists);
        artistList.setAdapter(artistListAdapter);

        artistList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), TracksActivity.class);
                intent.putExtra("artistId", mResults.artists.items.get(position).id);
                intent.putExtra("artistName", mResults.artists.items.get(position).name);
                startActivity(intent);
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
                spotifySearchArtists(artistName);
            }
        });
    }

    private void showToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
            }
        });
    }

    private void spotifySearchArtists(String artistName) {
        //Sportify calbacks are in worker thread
        spotify.searchArtists(artistName, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                mResults = artistsPager;

                //Bind data to list and show
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        artistListAdapter.refresh(mResults.artists);

                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {


                //Error happens, Clear the list
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Pager<Artist> artists = new Pager<>();
                        mResults.artists = artists;
                        artistListAdapter.refresh(mResults.artists);

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
