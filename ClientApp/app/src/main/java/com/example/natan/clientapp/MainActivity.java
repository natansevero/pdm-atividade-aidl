package com.example.natan.clientapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.natan.startedservicecep.IMyAidlInterface;

public class MainActivity extends AppCompatActivity {

    private TextView mResultTextView;
    private IMyAidlInterface mIMyAidlInterface;
    private ServiceConnection mServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultTextView = (TextView) findViewById(R.id.tv_result);
        onInitService();
    }

    public void onInitService() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mIMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mIMyAidlInterface = null;
            }
        };

        if(mIMyAidlInterface == null) {
            Intent intent = new Intent();
            intent.setAction("com.remote.service.REQUEST_DATA");
            bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    public void callService(View view) {
        try {
            if(mIMyAidlInterface != null) {
                mResultTextView.setText(mIMyAidlInterface.cepDatas("01001000"));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
