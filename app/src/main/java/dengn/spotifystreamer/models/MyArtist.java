package dengn.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015-6-13.
 */
public class MyArtist implements Parcelable {

    public String name;
    public String imageURL;
    public String id;

    public MyArtist(String name, String imageURL, String id) {
        this.name = name;
        this.imageURL = imageURL;
        this.id = id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.imageURL);
        dest.writeString(this.id);
    }

    private MyArtist(Parcel in) {
        this.name = in.readString();
        this.imageURL = in.readString();
        this.id = in.readString();
    }

    public static final Parcelable.Creator<MyArtist> CREATOR = new Parcelable.Creator<MyArtist>() {
        public MyArtist createFromParcel(Parcel source) {
            return new MyArtist(source);
        }

        public MyArtist[] newArray(int size) {
            return new MyArtist[size];
        }
    };
}
