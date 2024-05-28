package com.ethanlin.btlibrary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import com.ethanlin.config.GlobalConfig;
import com.unity3d.player.UnityPlayer;

public class PermissionHelper {

    public static void requestBluetoothPermissions() {
        UnityPlayer.currentActivity.requestPermissions(GlobalConfig.ANDROID_12_BLE_PERMISSIONS, GlobalConfig.PERMISSIONS_REQUEST_BLUETOOTH);
    }

    /**
     * 檢查是否有藍牙權限
     */
    public static boolean checkBluetoothPermissions() {
        return UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED && UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * android 11+ check external storage
     * not use for now
     */
    public static void checkManagerExternalStoragePermission() {
        if (!Environment.isExternalStorageManager()) {
            UnityPlayer.currentActivity.startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
        } else {
            android.util.Log.d(GlobalConfig.DEBUG_TAG, "already got manage external storage permission");
        }
    }

    public static void requestManageExternalStoragePermission() { UnityPlayer.currentActivity.startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)); }
}
