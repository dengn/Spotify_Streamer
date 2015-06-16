package dengn.spotifystreamer.utils;

/**
 * Created by OLEDCOMM on 16/06/2015.
 */
public class PlayerUtils {

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
    public static String milliSecondsToTimer(long milliseconds){
        int hours = 0;
        int minutes = 0;
        int seconds =0;

        String hourString;
        String minuteString;
        String secondString;

        int secondsTotal = (int) (milliseconds/1000);
        int minutesTotal = secondsTotal/60;

        hours = minutesTotal/60;
        minutes = minutesTotal%60;
        seconds = secondsTotal%60;

        if (seconds < 10) {
            secondString = "0" + String.valueOf(seconds);
        } else {
            secondString = String.valueOf(seconds);
        }

        if(hours==0) {
            minuteString = String.valueOf(minutes);

            return minuteString + ":" + secondString;

        }
        else{
            hourString = String.valueOf(hours);
            if(minutes<10){
                minuteString = "0" + String.valueOf(minutes);
            }
            else{
                minuteString = String.valueOf(minutes);
            }

            return hourString+":"+minuteString+":"+secondString;
        }
    }



    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    public static int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }
}
