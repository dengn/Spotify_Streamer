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

/**
 * Created by OLEDCOMM on 12/06/2015.
 */
public class TracksListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private Pager<Artist> mArtists;

    public TracksListAdapter(Context context, Pager<Artist> artists) {

        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mArtists = artists;
    }

    public void refresh(Pager<Artist> artists) {
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
        ((ArtistItemViewHolder) holder).artistName.setText(mArtists.items.get(position).name);
        if(mArtists.items.get(position).images!=null && mArtists.items.size()!=0) {
            Picasso.with(mContext)
                    .load(ImageUtils.getImageUrl(mArtists.items.get(position).images, ImageUtils.IMAGE_SMALL))
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(((ArtistItemViewHolder) holder).artistImg);
        }


    }

    @Override
    public int getItemCount() {
        return mArtists.items == null ? 0 : mArtists.items.size();
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
