package com.example.natan.startedservicecep;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

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
        public String cepDatas(final String cep) throws RemoteException {
            new Thread() {
                @Override
                public void run() {
                    mCepDatas = doRequest(cep);
                }
            }.start();

            while (mCepDatas == null) {
                Log.d("WHILE", "...");
            }

            return mCepDatas;
        }

    };

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String cep = intent.getStringExtra(Intent.EXTRA_TEXT);
                    String anwser = doRequest(cep);

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

    public String doRequest(String cep) {
        HttpURLConnection urlConnection = null;

        try {
            String url = "https://viacep.com.br/ws/"+ cep +"/json/";

            URL u = new URL(url);
            urlConnection = (HttpURLConnection) u.openConnection();

            InputStream in = urlConnection.getInputStream();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while((inputStr = streamReader.readLine()) != null){
                responseStrBuilder.append(inputStr);
            }

            JSONObject json = new JSONObject(responseStrBuilder.toString());

            return json.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return "";

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


}
