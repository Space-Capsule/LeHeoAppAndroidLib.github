package com.ethanlin.btlibrary;

import android.annotation.SuppressLint;

import com.ethanlin.config.GlobalConfig;
import com.ethanlin.utils.Utils;
import com.unity3d.player.UnityPlayer;

import java.util.HashMap;

public class ImuDataHandler {
    /**
     * 要傳給Unity的
     */
    private String mFinalDataString = "";

    private final HashMap<Integer, QuaternionDataHelper> mQuaternionMapping = new HashMap<Integer, QuaternionDataHelper>();

    private byte[] mCurrentReadData = new byte[36];

    private byte[] mDataTemp1 = new byte[4];
    private byte[] mDataTemp5 = new byte[4];
    private byte[] mDataTemp9 = new byte[4];
    private byte[] mDataTemp13 = new byte[4];

    private byte[] mDataTemp19 = new byte[4];
    private byte[] mDataTemp23 = new byte[4];
    private byte[] mDataTemp27 = new byte[4];
    private byte[] mDataTemp31 = new byte[4];

    private float mUpperFloatX = 0f;
    private float mUpperFloatY = 0f;
    private float mUpperFloatZ = 0f;
    private float mUpperFloatW = 0f;

    private float mDownFloatX = 0f;
    private float mDownFloatY = 0f;
    private float mDownFloatZ = 0f;
    private float mDownFloatW = 0f;

    private int mUpperId = 0;
    private int mDownId = 0;

    public void initQuaternionMapping() {
        for (int i = 1; i < 11; ++i) {
            mQuaternionMapping.put(i, new QuaternionDataHelper(0f, 0f, 0f, 1f));
        }
    }

    public void setByteArrayData(byte[] aData) {
        mCurrentReadData = aData;
        processData();
    }

    @SuppressLint("DefaultLocale")
    private void processData() {
        if (Utils.byteArrayToHexString(mCurrentReadData).substring(0, 1).equals("E")) {
            // android.util.Log.d(GlobalConfig.DEBUG_TAG, Utils.byteArrayToHexString(mCurrentReadData));
            /////////////////// 上半身 /////////////////////////////////////////////////////////////////
            System.arraycopy(mCurrentReadData, 1, mDataTemp1, 0, 4);
            System.arraycopy(mCurrentReadData, 5, mDataTemp5, 0, 4);
            System.arraycopy(mCurrentReadData, 9, mDataTemp9, 0, 4);
            System.arraycopy(mCurrentReadData, 13, mDataTemp13, 0, 4);

            mUpperId = Utils.getUpperBodyId(mCurrentReadData);

            mUpperFloatX = Utils.byteArray2float(mDataTemp5);
            mUpperFloatY = Utils.byteArray2float(mDataTemp1) * -1f;
            mUpperFloatZ = Utils.byteArray2float(mDataTemp9) * -1f;
            mUpperFloatW = Utils.byteArray2float(mDataTemp13);

            // 灌四元數資料 - 上半身
            mQuaternionMapping.put(mUpperId, new QuaternionDataHelper(mUpperFloatX, mUpperFloatY, mUpperFloatZ, mUpperFloatW));

            /////////////////// 下半身 /////////////////////////////////////////////////////////////////
            System.arraycopy(mCurrentReadData, 19, mDataTemp19, 0, 4);
            System.arraycopy(mCurrentReadData, 23, mDataTemp23, 0, 4);
            System.arraycopy(mCurrentReadData, 27, mDataTemp27, 0, 4);
            System.arraycopy(mCurrentReadData, 31, mDataTemp31, 0, 4);

            mDownId = Utils.getDownBodyId(mCurrentReadData);

            mDownFloatX = Utils.byteArray2float(mDataTemp23);
            mDownFloatY = Utils.byteArray2float(mDataTemp19) * -1f;
            mDownFloatZ = Utils.byteArray2float(mDataTemp27) * -1f;
            mDownFloatW = Utils.byteArray2float(mDataTemp31);

            mQuaternionMapping.put(mDownId, new QuaternionDataHelper(mDownFloatX, mDownFloatY, mDownFloatZ, mDownFloatW));

            mFinalDataString = String.format("%d#%.03f#%.03f#%.03f#%.03f#%d#%.03f#%.03f#%.03f#%.03f", mUpperId, mUpperFloatX, mUpperFloatY, mUpperFloatZ, mUpperFloatW, mDownId, mDownFloatX, mDownFloatY, mDownFloatZ, mDownFloatW);
            // UnityPlayer.UnitySendMessage(GlobalConfig.UNITY_GAME_OBJECT_NAME, "receiveDataFromNative", mFinalDataString);
        }
    }
}
