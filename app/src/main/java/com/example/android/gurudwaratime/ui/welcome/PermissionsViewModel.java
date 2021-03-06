package com.example.android.gurudwaratime.ui.welcome;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

public class PermissionsViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> mLocationGrantedStatus, mDNDGrantedStatus;


    public PermissionsViewModel(@NonNull Application application) {
        super(application);

        mLocationGrantedStatus = new MutableLiveData<>();
        mDNDGrantedStatus = new MutableLiveData<>();

        checkLocationPermission();
        checkDNDPermission();

    }

    /**
     * static method to access checkLocationAndDndPermissions
     * from PermissionsHelper
     */
    public static boolean checkLocationAndDndPermissions(Context context) {
        return PermissionsHelper.checkLocationAndDndPermissions(context);
    }

    MutableLiveData<Boolean> getLocationGrantedStatus() {
        return mLocationGrantedStatus;
    }

    MutableLiveData<Boolean> getDNDGrantedStatus() {
        return mDNDGrantedStatus;
    }

    /**
     * to set location permission granted status
     *
     * @param b granted status
     */
    void setLocationPermissionGranted(boolean b) {
        mLocationGrantedStatus.setValue(b);
    }

    /**
     * to set dnd permission granted status
     *
     * @param b granted status
     */
    private void setDNDPermissionGranted(boolean b) {
        mDNDGrantedStatus.setValue(b);
    }

    /**
     * check location permission and save
     */
    void checkLocationPermission() {
        setLocationPermissionGranted(PermissionsHelper.checkLocationPermission(getApplication()));
    }

    /**
     * check dnd permission and save
     */
    void checkDNDPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            setDNDPermissionGranted(PermissionsHelper.checkDNDPermission(getApplication()));
        } else {
            setDNDPermissionGranted(true);
        }
    }

    /**
     * Request appropriate permissions (first location, then dnd(if needed)
     */
    void requestPermissions(Activity activity) {
        checkLocationPermission();
        Boolean locationGranted = mLocationGrantedStatus.getValue();
        if (locationGranted != null) {
            if (locationGranted) {
                checkDNDPermission();
                Boolean dndGranted = mDNDGrantedStatus.getValue();
                if (dndGranted != null && !dndGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionsHelper.requestDNDPermission(activity);
                    }
                }
            } else {
                PermissionsHelper.requestLocationPermission(activity);
            }
        }
    }
}
