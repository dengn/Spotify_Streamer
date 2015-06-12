package dengn.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.adapters.ArtistListAdapter;
import dengn.spotifystreamer.adapters.TracksListAdapter;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class TracksFragment extends Fragment {

    //Ui components
    @InjectView(R.id.track_list)
    RecyclerView trackList;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private String mArtistId;
    private String mArtistName;

    private Tracks mTracks;

    private TracksListAdapter mTracksListAdapter;

    //Spotify
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();

    public static TracksFragment newInstance(String artistId, String artistName) {
        TracksFragment fragment = new TracksFragment();
        Bundle args = new Bundle();
        args.putString("artistId", artistId);
        args.putString("artistName", artistName);
        fragment.setArguments(args);
        return fragment;
    }

    public TracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mArtistId = getArguments().getString("artistId");
            mArtistName = getArguments().getString("artistName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        ButterKnife.inject(this, view);

        mTracks = new Tracks();
        List<Track> tracks = new ArrayList<Track>();
        mTracks.tracks = tracks;

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Top 10 Tracks");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(mArtistName);

        trackList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTracksListAdapter = new TracksListAdapter(getActivity(), mTracks);
        trackList.setAdapter(mTracksListAdapter);

        Map<String, Object> options = new HashMap<>();
        options.put("country", "se");
        spotify.getArtistTopTrack(mArtistId, options, new Callback<Tracks>(){

            @Override
            public void success(Tracks tracks, Response response) {
                mTracks = tracks;
                //Bind data to list and show
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mTracksListAdapter.refresh(mTracks);

                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

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


        return view;
    }

    private void showToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
            }
        });
    }
}
