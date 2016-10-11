package com.xhuabu.huangquan.mypluginapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void log() {
        ClassLoader classLoader = getClassLoader();
        if (classLoader != null){
            Log.e(TAG, "[onCreate] classLoader "  + " : " + classLoader.toString());
            while (classLoader.getParent()!=null){
                classLoader = classLoader.getParent();
                Log.e(TAG,"[onCreate] classLoader "   + " : " + classLoader.toString());
            }
        }
    }
}
