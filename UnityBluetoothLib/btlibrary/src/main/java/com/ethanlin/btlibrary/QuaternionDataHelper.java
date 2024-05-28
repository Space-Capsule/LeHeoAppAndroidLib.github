package com.ethanlin.btlibrary;

import android.annotation.SuppressLint;

public class QuaternionDataHelper {

    private String mQuaternionSet;

    @SuppressLint("DefaultLocale")
    public QuaternionDataHelper(float aQuaternionX, float aQuaternionY, float aQuaternionZ, float aQuaternionW) {
        mQuaternionSet = String.format("%.03f#%.03f#%.03f#%.03f", aQuaternionX, aQuaternionY, aQuaternionZ, aQuaternionW);
    }

    public String GetQuaternionSet() { return mQuaternionSet; }
}
