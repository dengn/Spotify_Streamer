package dengn.spotifystreamer.events;

/**
 * Created by OLEDCOMM on 26/06/2015.
 */
public class FinishEvent {
    public boolean isFinish;
    public int position;
    public FinishEvent(boolean isFinish, int position){
        this.isFinish = isFinish;
        this.position = position;
    }
}
