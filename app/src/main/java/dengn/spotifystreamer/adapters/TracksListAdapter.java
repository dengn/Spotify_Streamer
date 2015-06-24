package dengn.spotifystreamer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dengn.spotifystreamer.R;
import dengn.spotifystreamer.models.MyTrack;

/**
 * Created by OLEDCOMM on 12/06/2015.
 */
public class TracksListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ArrayList<MyTrack> mTracks;

    public TracksListAdapter(Context context, ArrayList<MyTrack> tracks) {

        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mTracks = tracks;
    }

    public void refresh(ArrayList<MyTrack> tracks) {
        mTracks = tracks;
        notifyDataSetChanged();
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new TracksItemViewHolder(mLayoutInflater.inflate(R.layout.track_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        //Bind data to view
        if (mTracks.get(position).name != null)
            ((TracksItemViewHolder) holder).trackName.setText(mTracks.get(position).name);
        if(mTracks.get(position).albumName!=null){
            ((TracksItemViewHolder) holder).trackAlbumName.setText(mTracks.get(position).albumName);
        }
        if (mTracks.get(position).imageSmallURL != null && mTracks.size() != 0) {
            Picasso.with(mContext)
                    .load(mTracks.get(position).imageSmallURL)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(((TracksItemViewHolder) holder).trackImg);
        }
        else{
            Picasso.with(mContext)
                    .load(R.drawable.no_image)
                    .into(((TracksItemViewHolder)holder).trackImg);
        }


    }

    @Override
    public int getItemCount() {
        return mTracks == null ? 0 : mTracks.size();
    }

    public static class TracksItemViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.track_img)
        ImageView trackImg;

        @InjectView(R.id.track_name)
        TextView trackName;

        @InjectView(R.id.track_album_name)
        TextView trackAlbumName;

        TracksItemViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }


}
