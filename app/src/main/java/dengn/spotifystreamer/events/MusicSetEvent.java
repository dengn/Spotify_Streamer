package dengn.spotifystreamer.events;

import dengn.spotifystreamer.models.MyTrack;
import dengn.spotifystreamer.services.MusicService;

/**
 * Created by OLEDCOMM on 02/07/2015.
 */
public class MusicSetEvent {
    public MyTrack myTrack;
    public MusicService.State mState;
    public MusicSetEvent(MyTrack myTrack, MusicService.State mState){
        this.myTrack = myTrack;
        this.mState = mState;
    }
}
