package dengn.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.utils.DebugConfig;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends Fragment {

    @InjectView(R.id.player_artist_name)
    TextView artistName;
    @InjectView(R.id.player_album_name)
    TextView albumName;
    @InjectView(R.id.player_track_name)
    TextView trackName;
    @InjectView(R.id.player_album_img)
    ImageView albumImage;
    @InjectView(R.id.player_seekbar)
    SeekBar seekBar;
    @InjectView(R.id.player_ontime)
    TextView playTime;
    @InjectView(R.id.player_timelen)
    TextView totalTime;
    @InjectView(R.id.player_previous)
    ImageButton previous;
    @InjectView(R.id.player_play_pause)
    ImageButton playPause;
    @InjectView(R.id.player_next)
    ImageButton next;

    private String mArtistName;
    private String mAlbumName;
    private String mTrackName;
    private int mTrackDuration;
    private String mTrackPreview;
    private String mAlbumImage;

    private boolean isPlaying = false;



    public static PlayerFragment newInstance(String artistName, String albumName, String trackName, int trackDuration, String trackPreview, String albumImage) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString("artist_name", artistName);
        args.putString("album_name", albumName);
        args.putString("track_name", trackName);
        args.putInt("track_duration", trackDuration);
        args.putString("track_preview", trackPreview);
        args.putString("album_image", albumImage);
        fragment.setArguments(args);
        return fragment;
    }


    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (getArguments() != null) {
            mArtistName = getArguments().getString("artist_name");
            mAlbumName = getArguments().getString("album_name");
            mTrackName = getArguments().getString("track_name");
            mTrackDuration = getArguments().getInt("track_duration");
            mTrackPreview = getArguments().getString("track_preview");
            mAlbumImage = getArguments().getString("album_image");

            if(DebugConfig.DEBUG)
                Log.d(DebugConfig.TAG, "mAlbumImage "+mAlbumImage);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.inject(this, view);

        artistName.setText(mArtistName);
        albumName.setText(mAlbumName);
        trackName.setText(mTrackName);
        playTime.setText("0:00");
        totalTime.setText("0:30");
        Picasso.with(getActivity()).load(mAlbumImage).into(albumImage);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    playPause.setBackgroundResource(android.R.drawable.ic_media_play);
                    isPlaying = false;
                }
                else{
                    playPause.setBackgroundResource(android.R.drawable.ic_media_pause);
                    isPlaying = true;
                }
            }
        });

        return view;
    }
}
