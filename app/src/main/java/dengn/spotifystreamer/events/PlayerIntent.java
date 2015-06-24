package dengn.spotifystreamer.events;

import java.util.ArrayList;

import dengn.spotifystreamer.models.MyTrack;

/**
 * Created by OLEDCOMM on 24/06/2015.
 */
public class PlayerIntent {

    public ArrayList<MyTrack> tracks;
    public int position;

    public PlayerIntent(ArrayList<MyTrack> tracks, int position){
        this.tracks = tracks;
        this.position = position;
    }
}
