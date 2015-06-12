package dengn.spotifystreamer.utils;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;


/**
 * Created by OLEDCOMM on 12/06/2015.
 */
public class ImageUtils {

    public static final int IMAGE_BIG = 1;
    public static final int IMAGE_MEDIUM = 2;
    public static final int IMAGE_SMALL = 3;
    public static final int IMAGE_THUMBNAIL = 4;

    public static String getImageUrl(List<Image> images, int type){
        String url = "R.drawable.no_image";

        if(images==null || images.size()==0){
            return url;
        }

        switch(type){
            case 1:
                for(Image image:images){
                    if(image.height>800){
                        url = image.url;
                        break;
                    }
                }
                break;
            case 2:
                for(Image image:images){
                    if(image.height<=800 && image.height>500){
                        url = image.url;
                        break;
                    }
                }
                break;
            case 3:
                for(Image image:images){
                    if(image.height<=500 && image.height>100){
                        url = image.url;
                        break;
                    }
                }
                break;
            case 4:
                for(Image image:images){
                    if(image.height<=100){
                        url = image.url;
                        break;
                    }
                }
                break;
            default:
                break;
        }

        return url;
    }
}
