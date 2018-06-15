package com.example.natan.startedservicecep;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyService extends Service {

    private final Object lock = new Object();
    private String mCepDatas;

    private final IMyAidlInterface.Stub mBinder = new IMyAidlInterface.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            // does nothing, dude
        }

        @Override
        public String cepDatas(String cep) throws RemoteException {
            Intent intent = new Intent(getApplicationContext(), MyService.class);
            startService(intent);

            if(mCepDatas != null) {
                return mCepDatas;
            }

            return "";
        }

    };

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String cep = intent.getStringExtra(Intent.EXTRA_TEXT);

                    String url = "https://viacep.com.br/ws/"+ cep +"/json/";

                    URL u = new URL(url);
                    HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();

                    InputStream in = urlConnection.getInputStream();
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                    StringBuilder responseStrBuilder = new StringBuilder();

                    String inputStr;
                    while((inputStr = streamReader.readLine()) != null){
                        responseStrBuilder.append(inputStr);
                    }

                    JSONObject json = new JSONObject(responseStrBuilder.toString());

                    String anwser = json.toString();

                    synchronized (lock) {
                        mCepDatas = anwser;
                    }

                    Message msg = new Message();
                    msg.obj = anwser;

                    MainActivity.myHandler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


}
