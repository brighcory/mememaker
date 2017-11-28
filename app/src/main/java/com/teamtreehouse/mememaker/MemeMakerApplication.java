package com.teamtreehouse.mememaker;

import android.preference.Preference;
import android.preference.PreferenceManager;
import com.teamtreehouse.mememaker.utils.FileUtilities;

public class MemeMakerApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FileUtilities.saveAssetImage(this, "dogmess.jpg");
        FileUtilities.saveAssetImage(this, "excitedcat.jpg");
        FileUtilities.saveAssetImage(this, "guiltypup.jpg");

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }
}
