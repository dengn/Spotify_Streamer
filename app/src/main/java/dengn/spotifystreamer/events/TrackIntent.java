package dengn.spotifystreamer.events;

/**
 * Created by OLEDCOMM on 23/06/2015.
 */
public class TrackIntent {

    public String artistId;
    public String artistName;

    public TrackIntent(String artistId, String artistName){
        this.artistId = artistId;
        this.artistName = artistName;
    }
}
