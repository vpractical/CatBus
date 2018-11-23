package com.y.catbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.y.bus_library.EventBus;
import com.y.bus_library.annotation.Subscribe;
import com.y.catbus.event.Btn1EventBean;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.unRegister(this);
    }

    @Subscribe(refuseStick = true)
    public void btn1EventStick1(Btn1EventBean bean){
        Log.e(TAG,bean.msg);
        T.show(bean.msg);
    }

    @Subscribe
    public void btn1EventStick2(Btn1EventBean bean){
        Log.e(TAG,bean.msg);
        T.show(bean.msg);
    }
}
