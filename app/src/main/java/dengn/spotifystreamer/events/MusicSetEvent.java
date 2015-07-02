package dengn.spotifystreamer.events;

import dengn.spotifystreamer.models.MyTrack;

/**
 * Created by OLEDCOMM on 02/07/2015.
 */
public class MusicSetEvent {
    public MyTrack myTrack;
    public MusicSetEvent(MyTrack myTrack){
        this.myTrack = myTrack;
    }
}
