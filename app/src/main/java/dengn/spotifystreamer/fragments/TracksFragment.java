package dengn.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;

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

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Top 10 Tracks");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(mArtistName);

        return view;
    }
}
