package com.ethanlin.config;

import android.Manifest;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.UUID;

public class GlobalConfig {
    /**
     * Debug 的 tag
     */
    public final static String DEBUG_TAG = "EthanLinLeHuoAppTag";

    /**
     * Unity GameObject name
     */
    public final static String UNITY_GAME_OBJECT_NAME = "Android_Native_Plugins";

    /**
     * 藍牙主機的名稱
     */
    public final static String SC_BLE_NAME = "SC-BLE5";

    /**
     * 藍牙主機的名稱
     */
    public final static String SC_BLE_NAME1 = "HC-";
    /**
     * 藍牙主機的名稱
     */
    public final static String SC_BLE_NAME2 = "SC-S0000";

    public final static String SUBSCRIBED_SERVICE = "FFE0";
    public final static String SUBSCRIBED_CHARACTERISTIC = "FFE1";

    public final static int REQUEST_ENABLE_BT = 200;
    public final static int PERMISSIONS_REQUEST_BLUETOOTH = 201;
    /**權限宣告REQUEST_CODE */
    public final static int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 100;

    public final static UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * if 未到 Android 12, 要求權限
     */
    public final static String[] BLE_PERMISSIONS = new String[] {
            // Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * if Android 12 (Level 31)以上, 要求權限
     */
    public final static String[] ANDROID_12_BLE_PERMISSIONS = new String[] {
            // Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public final static String SDCARD_ROOT_PATH = String.format("%s%s", Environment.getExternalStorageDirectory().getPath(), File.separator);
    private final static String EXTERNAL_STORAGE_PATH = "/storage/emulated/0/";


    /**
     * 下載資料夾
     */
    public final static String BASE_PUBLIC_DOWNLOAD_PATH = String.format("%s%s%s", EXTERNAL_STORAGE_PATH, Environment.DIRECTORY_DOWNLOADS, File.separator);

    /**
     * 儲存CSV檔的資料夾
     */
    private final static String BT_CSV_DATA_FOLDER  = ".SpaceCapsuleLeHuoCSV";
    /**
     * 儲存CSV檔的資料夾路徑
     */
    public final static String BT_CSV_DATA_FOLDER_PATH = String.format("%s%s%s", EXTERNAL_STORAGE_PATH, BT_CSV_DATA_FOLDER, File.separator);

    public final static String CSV_TITLe = String.format("Chest,LeftUpperArm,LeftForeArm,RightUpperArm,RightForeArm,Pelvis,LeftThigh,LeftCalf,RightThigh,RightCalf%s", System.lineSeparator());

    public final static String BT_CSV_DATA_FILE_NAME = "SpaceCapsule_";

    /**
     * CSV副檔名
     */
    public final static String CSV_DATA_FILE_Extension = ".csv";


    public static class AllPartNameIndex {
        /**
         * chest,lu,lf,ru,rf,pelvis,lt,lc,rt,rc
         *  3, 4, 5, 2, 1, 8, 9, 10, 7, 6
         */
        /**
         * 右前臂
         */
        public final static int RIGHT_FOREARM = 1;
        /**
         * 右上臂
         */
        public final static int RIGHT_UPPER_ARM = 2;
        /**
         * 胸
         */
        public final static int CHEST = 3;
        /**
         * 左上臂
         */
        public final static int LEFT_UPPER_ARM = 4;
        /**
         * 左前臂
         */
        public final static int LEFT_FOREARM = 5;
        /**
         * 左小腿
         */
        public final static int LEFT_CALF = 10;
        /**
         * 左大腿
         */
        public final static int LEFT_THIGH = 9;
        /**
         * 臀
         */
        public final static int PELVIS = 8;
        /**
         * 右大腿
         */
        public final static int RIGHT_THIGH = 7;
        /**
         * 右小腿
         */
        public final static int RIGHT_CALF = 6;
    }
}
