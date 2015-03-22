package za.co.lukestonehm.logicaldefence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by luke on 2015/03/21.
 */
public class LongPressReceiver extends BroadcastReceiver {
    public static final String POS_EXTRA = "position_extra";

    NavCallback navCallback;

    public LongPressReceiver(NavCallback navCallback) {
        this.navCallback = navCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        navCallback.fallacyLongPressCallback(intent.getIntExtra(POS_EXTRA, -1));
    }
}
