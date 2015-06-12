package dengn.spotifystreamer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.utils.ImageUtils;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by OLEDCOMM on 12/06/2015.
 */
public class TracksListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private Tracks mTracks;

    public TracksListAdapter(Context context, Tracks tracks) {

        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mTracks = tracks;
    }

    public void refresh(Tracks tracks) {
        mTracks = tracks;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new TracksItemViewHolder(mLayoutInflater.inflate(R.layout.artist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        //Bind data to view
        if (mTracks.tracks.get(position).name != null)
            ((TracksItemViewHolder) holder).trackName.setText(mTracks.tracks.get(position).name);
        if (mTracks.tracks.get(position).album.images != null && mTracks.tracks.get(position).album.images.size() != 0) {
            Picasso.with(mContext)
                    .load(ImageUtils.getImageUrl(mTracks.tracks.get(position).album.images, ImageUtils.IMAGE_SMALL))
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(((TracksItemViewHolder) holder).trackImg);
        }


    }

    @Override
    public int getItemCount() {
        return mTracks.tracks == null ? 0 : mTracks.tracks.size();
    }

    public static class TracksItemViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.artist_img)
        ImageView trackImg;

        @InjectView(R.id.artist_name)
        TextView trackName;


        TracksItemViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }


}
