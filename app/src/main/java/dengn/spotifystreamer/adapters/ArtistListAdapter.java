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
import dengn.spotifystreamer.models.MyArtist;

/**
 * Created by OLEDCOMM on 12/06/2015.
 */
public class ArtistListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ArrayList<MyArtist> mArtists;

    public ArtistListAdapter(Context context, ArrayList<MyArtist> artists) {

        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mArtists = artists;
    }

    public void refresh(ArrayList<MyArtist> artists) {
        mArtists = artists;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ArtistItemViewHolder(mLayoutInflater.inflate(R.layout.artist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        //Bind data to view
        if (mArtists.get(position).name != null)
            ((ArtistItemViewHolder) holder).artistName.setText(mArtists.get(position).name);
        if(mArtists.get(position).imageURL!=null && mArtists.size()!=0) {
            Picasso.with(mContext)
                    .load(mArtists.get(position).imageURL)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(((ArtistItemViewHolder) holder).artistImg);
        } else{
            Picasso.with(mContext)
                    .load(R.drawable.no_image)
                    .into(((ArtistItemViewHolder) holder).artistImg);
        }


    }

    @Override
    public int getItemCount() {
        return mArtists == null ? 0 : mArtists.size();
    }

    public static class ArtistItemViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.artist_img)
        ImageView artistImg;

        @InjectView(R.id.artist_name)
        TextView artistName;


        ArtistItemViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }


}
