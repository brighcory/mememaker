package com.teamtreehouse.mememaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.teamtreehouse.mememaker.utils.StorageType;

public class MemeMakerApplicationSettings {
    SharedPreferences mSharedPreferences;

    public MemeMakerApplicationSettings(Context context){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getStoragePreference(){
        return mSharedPreferences.getString("Storage", StorageType.INTERNAL);
    }

    public void setSharedPreference(String storageType){
        mSharedPreferences.edit()
                .putString("Storage", storageType)
                .apply();
    }

}
