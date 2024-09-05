package com.ethanlin.utils;

import android.icu.text.SimpleDateFormat;

import com.ethanlin.config.GlobalConfig;
import com.unity3d.player.UnityPlayer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Utils {

    /*public static void unitySendDataString(String aDataString) {
        UnityPlayer.UnitySendMessage(GlobalConfig.UNITY_GAME_OBJECT_NAME, "receiveDataFromAndroidNative", aDataString);
    }
    public static void unitySendMessage(String aMessage) {
        UnityPlayer.UnitySendMessage(GlobalConfig.UNITY_GAME_OBJECT_NAME, "receiveMessageFromAndroidNative", aMessage);
    }*/

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String byteArrayToHexString(byte[] aBytes) {
        StringBuilder sb = new StringBuilder(aBytes.length * 2);
        for (byte b : aBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }

    public static byte[] hexStringToByteArray(String hex) {
        hex = hex.length() % 2 != 0 ? "0" + hex  :hex;

        byte[] b = new byte[hex.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static float byteArray2float(byte[] aBytes) {
        if (aBytes.length != 4) {
            throw new IllegalArgumentException("The byte array must have a length of 4.");
        }

        ByteBuffer buf = ByteBuffer.wrap(aBytes);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getFloat();
    }

    /**
     * 取得上半身ID return Int
     */
    public static int getUpperBodyId(byte[] aBytes) {
        if (byteArrayToHexString(aBytes).substring(0, 1).equals("E"))
        {
            return Integer.parseInt(byteArrayToHexString(aBytes).substring(1, 2));
        }
        else
        {
            return 0;
        }
    }

    /**
     * 取得下半身ID return Int
     */
    public static int getDownBodyId(byte[] aBytes) {
        if (byteArrayToHexString(aBytes).substring(36, 37).equals("E"))
        {
            return byteArrayToHexString(aBytes).charAt(37) == 'A' ? 10 : Integer.parseInt(byteArrayToHexString(aBytes).substring(37, 38));
        }
        else
        {
            return 0;
        }
    }

    public static UUID getUUID(String aUuidString) {
        UUID uuid = null;
        if (aUuidString.length() == 36) {
            uuid = UUID.fromString(aUuidString);
        } else if (aUuidString.length() <= 8) {
            StringBuilder newString = new StringBuilder();
            newString.append("00000000", 0, 8 - aUuidString.length());
            newString.append(aUuidString);
            newString.append("-0000-1000-8000-00805f9b34fb");
            uuid = UUID.fromString(newString.toString());
        }
        return uuid;
    }

    public static void sortArrayList(ArrayList<String> aList) {
        if (aList != null && aList.size() > 0) {
            Collections.sort(aList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                    // return 0;
                }
            });
        } else {
            android.util.Log.e(GlobalConfig.DEBUG_TAG, "sortArrayList List is null or aList size = 0");
        }
    }

    public static void convertStringArrayToString1(ArrayList<String> aList) {
        if (aList != null && aList.size() > 0) {
            // aList.add("FBFBFBFBFBFBFBFBFBFBFBFBFBFBFBFBFBFB");
            String str = java.lang.String.join(System.lineSeparator(), aList);
            android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("轉換後:%s%s", System.lineSeparator(), str));
        } else {
            android.util.Log.e(GlobalConfig.DEBUG_TAG, "convertStringArrayToString List is null or aList size = 0");
        }
    }

    public static void convertStringArrayToString2(ArrayList<String> aList) {
        if (aList != null && aList.size() > 0) {
            // aList.add("FBFBFBFBFBFBFBFBFBFBFBFBFBFBFBFBFBFB");
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : aList) {
                stringBuilder.append(s);
                stringBuilder.append(System.lineSeparator());
            }
        } else {
            android.util.Log.e(GlobalConfig.DEBUG_TAG, "convertStringArrayToString List is null or aList size = 0");
        }
    }
}
