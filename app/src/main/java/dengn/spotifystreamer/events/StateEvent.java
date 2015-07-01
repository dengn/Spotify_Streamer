package dengn.spotifystreamer.events;

import dengn.spotifystreamer.services.MusicService;

/**
 * Created by OLEDCOMM on 01/07/2015.
 */
public class StateEvent {

    public MusicService.State state;
    public StateEvent(MusicService.State state){
        this.state = state;
    }
}
