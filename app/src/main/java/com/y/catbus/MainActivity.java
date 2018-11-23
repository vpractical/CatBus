package com.y.catbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.y.bus_library.EventBus;
import com.y.bus_library.ThreadMode;
import com.y.bus_library.annotation.Subscribe;
import com.y.catbus.event.Btn1EventBean;
import com.y.catbus.event.Btn2EventBean;
import com.y.catbus.event.Btn3EventBean;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"btn1发送消息线程id = " + Thread.currentThread().getId());
                EventBus.getInstance().post(new Btn1EventBean("msg:按钮1的消息"));
            }
        });
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG,"btn2发送消息线程id = " + Thread.currentThread().getId());
                        EventBus.getInstance().post(new Btn2EventBean("msg:按钮2的消息"));
                    }
                }).start();
            }
        });
        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"btn3发送消息线程id = " + Thread.currentThread().getId());
                EventBus.getInstance().post(new Btn3EventBean("msg:按钮3的消息"));
            }
        });

        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressTest();
                pressTest();
            }
        });

        EventBus.getInstance().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unRegister(this);
    }

    private void pressTest(){
        for (int i = 0; i < 1000; i++) {
            EventBus.getInstance().post(new Btn3EventBean("msg:按钮3的消息"));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void btn1Event(Btn1EventBean bean){
        Log.e(TAG,"btn1Event()接收消息线程id = " + Thread.currentThread().getId());
        show(bean.msg);
        Log.e(TAG,"btn1Event()" + bean.msg);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void btn2Event(Btn2EventBean bean){
        Log.e(TAG,"btn2Event()接收消息线程id = " + Thread.currentThread().getId());
        show(bean.msg);
        Log.e(TAG,"btn2Event()" + bean.msg);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void btn3Event1(Btn3EventBean bean){
        Log.e(TAG,"btn3Event1()接收消息线程id = " + Thread.currentThread().getId());
        show(bean.msg);
        Log.e(TAG,"btn3Event1()" + bean.msg);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void btn3Event2(final Btn3EventBean bean){
        Log.e(TAG,"btn3Event2()接收消息线程id = " + Thread.currentThread().getId());
        Log.e(TAG,"btn3Event2()" + bean.msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                show(bean.msg);
            }
        });
    }

    private void show(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}
