package dengn.spotifystreamer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dengn.spotifystreamer.R;

/**
 * Created by Administrator on 2015-6-14.
 */
public class SettingUtils {
    public static String getPreferredCountry(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_country_key),
                context.getString(R.string.pref_country_default));
    }
}
