package dengn.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015-6-13.
 */
public class MyTrack implements Parcelable {

    public static final int PREVIEW_LENGTH_DEFAULT = 30;

    public String name;
    public String albumName;
    public String imageLargeURL;
    public String imageSmallURL;
    public String previewURL;

    public MyTrack(String name, String albumName, String imageLargeURL, String imageSmallURL, String previewURL) {
        this.name = name;
        this.albumName = albumName;
        this.imageLargeURL = imageLargeURL;
        this.imageSmallURL = imageSmallURL;
        this.previewURL = previewURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.albumName);
        dest.writeString(this.imageLargeURL);
        dest.writeString(this.imageSmallURL);
        dest.writeString(this.previewURL);
    }

    private MyTrack(Parcel in) {
        this.name = in.readString();
        this.albumName = in.readString();
        this.imageLargeURL = in.readString();
        this.imageSmallURL = in.readString();
        this.previewURL = in.readString();
    }

    public static final Creator<MyTrack> CREATOR = new Creator<MyTrack>() {
        public MyTrack createFromParcel(Parcel source) {
            return new MyTrack(source);
        }

        public MyTrack[] newArray(int size) {
            return new MyTrack[size];
        }
    };
}
