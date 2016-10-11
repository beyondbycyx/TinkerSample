/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tinker.sample.android.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import tinker.sample.android.R;
import tinker.sample.android.util.Utils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Tinker.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "i am on onCreate classloader:" + MainActivity.class.getClassLoader().toString());

        //test resource change
        Log.e(TAG, "i am on onCreate string:" + getResources().getString(R.string.test_resource));
//        Log.e(TAG, "i am on patch onCreate");

        Button loadPatchButton = (Button) findViewById(R.id.loadPatch);

        loadPatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "ext路径:" + Environment.getExternalStorageDirectory().getAbsolutePath());
                TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/patch_signed_7zip.apk");


            }
        });

        Button loadLibraryButton = (Button) findViewById(R.id.loadLibrary);

        loadLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //for lib/armeabi, just use TinkerInstaller.loadLibrary
                TinkerInstaller.loadArmLibrary(getApplicationContext(), "stlport_shared");
//                TinkerInstaller.loadLibraryFromTinker(getApplicationContext(), "assets/x86", "stlport_shared");
            }
        });

        Button cleanPatchButton = (Button) findViewById(R.id.cleanPatch);

        cleanPatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tinker.with(getApplicationContext()).cleanPatch();
            }
        });

        Button killSelfButton = (Button) findViewById(R.id.killSelf);

        killSelfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        Button buildInfoButton = (Button) findViewById(R.id.showInfo);

        buildInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo(MainActivity.this);
            }
        });

        //测试补丁
        //testPatchForAddRes();
        //testPatchForAddClazz(MyNewClazz.class);
        //testPatchForInnerStaticClazz(InnerStaticMyClazz.class);
        //testPatchForInnerClazz(InnerMyClazz.class);
        testPatchForAsset("channel.txt");

    }


    /**
     * warning must use in non-ui thread for the long time read fileStream
     * @param fileName
     */
    private void  testPatchForAsset(String fileName) {

        Tinker tinker = Tinker.with(getApplicationContext());
        if (!tinker.isTinkerLoaded()) {
            return;
        }

        AssetManager assets = getAssets();
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            bufferedReader  =  new BufferedReader(new InputStreamReader(assets.open(fileName)));

            String line ;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        addCode("开始:");
        addCode(stringBuilder.toString());

        addCode("结束!");


    }

    private void testPatchForInnerClazz(Class<InnerMyClazz> innerMyClazzClass) {
        Tinker tinker = Tinker.with(getApplicationContext());
        if (!tinker.isTinkerLoaded()) {
            return;
        }

        addCode("开始:");

        InnerMyClazz obj = new InnerMyClazz("i am mary", 654321);
        addCode(obj.toString());
        addCode("结束!");

    }


    private void testPatchForInnerStaticClazz(Class innerClazz) {
        Tinker tinker = Tinker.with(getApplicationContext());
        if (!tinker.isTinkerLoaded()) {
            return;
        }



            addCode("开始:");

        InnerStaticMyClazz obj = new InnerStaticMyClazz("i am hugo", 123456);
            addCode(obj.toString());
            addCode("结束!");


    }

    private void testPatchForAddClazz(Class clazz) {


        Tinker tinker = Tinker.with(getApplicationContext());
        if (!tinker.isTinkerLoaded()) {
            return;
        }

        try {

            addCode("开始:");
/*            Constructor constructor = clazz.getConstructor(String.class, Integer.class);
            Object o = constructor.newInstance("i am hugo", 123456);*/
            MyNewClazz obj = new MyNewClazz("i am hugo", 123456);
            addCode(obj.toString());
            addCode("结束!");

        } catch ( Exception e) {
            e.printStackTrace();
        }

    }

    private void testPatchForAddRes() {

        Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            addRes(MainActivity.this, R.layout.item_main);
        }

    }

    private void testPatchForAddMethod() {
        Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            addCode("add method  code from patch" );
        }

    }

    public class InnerMyClazz{
        public String name ;
        public int id ;

        public InnerMyClazz(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return "InnerMyClazz{" +
                    "name='" + name + '\'' +
                    ", id=" + id +
                    '}';
        }
    }
    public static class InnerStaticMyClazz {
        public String name;
        public int id;
        public InnerStaticMyClazz(String name , int id){
            this.name = name;
            this.id = id;

        }

        @Override
        public String toString() {
            return "InnerStaticMyClazz{" +
                    "name='" + name + '\'' +
                    ", id=" + id +
                    '}';
        }
    }

    public boolean showInfo(Context context) {
        // add more Build Info
        final StringBuilder sb = new StringBuilder();
        Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            sb.append(String.format("[patch is loaded] \n"));
            sb.append(String.format("[buildConfig CLIENTVERSION] %s \n", BuildInfo.CLIENTVERSION));
            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName(ShareConstants.TINKER_ID)));
            sb.append(String.format("[REAL TINKER_ID] %s \n", tinker.getTinkerLoadResultIfPresent().getTinkerID()));
            sb.append(String.format("[packageConfig patchMessage] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName("patchMessage")));
            sb.append(String.format("[TINKER_ID Rom Space] %d k \n", tinker.getTinkerRomSpace()));

        } else {
            sb.append(String.format("[patch is not loaded] \n"));
            sb.append(String.format("[buildConfig CLIENTVERSION] %s \n", BuildInfo.CLIENTVERSION));
            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", ShareTinkerInternals.getManifestTinkerID(getApplicationContext())));
        }
        sb.append(String.format("[BaseBuildInfo Message] %s \n", BaseBuildInfo.TEST_MESSAGE));

        final TextView v = new TextView(context);
        v.setText(sb);
        v.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.setTextColor(0xFF000000);
        v.setTypeface(Typeface.MONOSPACE);
        final int padding = 16;
        v.setPadding(padding, padding, padding, padding);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(v);
        final AlertDialog alert = builder.create();
        alert.show();


        return true;
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "i am on onResume");
//        Log.e(TAG, "i am on patch onResume");

        super.onResume();
        Utils.setBackground(false);

    }

    public void addCode(String addMes) {
        Toast.makeText(this, addMes, Toast.LENGTH_SHORT).show();
    }

    public void addRes(Activity context, @LayoutRes int layoutId) {
        FrameLayout contentView = (FrameLayout)context.findViewById(android.R.id.content);

        View inflate = LayoutInflater.from(context).inflate(layoutId, null, false);

        ((TextView) inflate.findViewById(R.id.tv_from_patch)).setText("patch res id:"+R.id.tv_from_patch);
        contentView.addView(inflate, new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM));

       // addCode("patch for add method code ---end");
/*        TextView tv = new TextView(MainActivity.this);

        tv.setId(View.generateViewId());
        tv.setText("i am new view with id ");*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.setBackground(true);
    }


}
