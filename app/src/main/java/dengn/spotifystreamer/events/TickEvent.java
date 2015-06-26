package dengn.spotifystreamer.events;

/**
 * Created by OLEDCOMM on 26/06/2015.
 */
public class TickEvent {

    public long duration;
    public long currentPostion;

    public TickEvent(long duration, long currentPostion){
        this.duration = duration;
        this.currentPostion = currentPostion;
    }
}
