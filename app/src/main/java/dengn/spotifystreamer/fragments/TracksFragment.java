package dengn.spotifystreamer.fragments;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import dengn.spotifystreamer.models.MyArtist;
import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.utils.DebugConfig;
import dengn.spotifystreamer.utils.ImageUtils;
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

    private ArrayList<MyTrack> mTracks = new ArrayList<MyTrack>();

    private TracksListAdapter mTracksListAdapter;

    private boolean reload = true;

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

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the fragment's state here

        if (DebugConfig.DEBUG)
            Log.d(DebugConfig.TAG, "save data to bundle");
        if (mTracks != null) {
            outState.putParcelableArrayList("tracks", mTracks);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (getArguments() != null) {
            mArtistId = getArguments().getString("artistId");
            mArtistName = getArguments().getString("artistName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            // Restore the fragment's state here
            mTracks = savedInstanceState.getParcelableArrayList("tracks");
            reload = false;

            if (DebugConfig.DEBUG)
                Log.d(DebugConfig.TAG, "get data from saved bundle");
        } else {
            reload = true;
        }

        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        ButterKnife.inject(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Top 10 Tracks");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(mArtistName);

        trackList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTracksListAdapter = new TracksListAdapter(getActivity(), mTracks);
        trackList.setAdapter(mTracksListAdapter);

        if (reload) {
            Map<String, Object> options = new HashMap<>();
            options.put("country", "se");
            spotify.getArtistTopTrack(mArtistId, options, new Callback<Tracks>() {

                @Override
                public void success(Tracks tracks, Response response) {

                    if (tracks == null || tracks.tracks.size() == 0 || tracks.tracks == null) {
                        MyTrack track = new MyTrack(getString(R.string.tracks_not_found), null, null, null, null);
                        mTracks.clear();
                        mTracks.add(track);

                    } else {
                        mTracks.clear();
                        for (Track item : tracks.tracks) {
                            MyTrack track = new MyTrack(item.name, item.album.name, ImageUtils.getImageUrl(item.album.images, ImageUtils.IMAGE_BIG), ImageUtils.getImageUrl(item.album.images, ImageUtils.IMAGE_SMALL), item.preview_url);
                            mTracks.add(track);
                        }
                    }
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

                    mTracks.clear();
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
