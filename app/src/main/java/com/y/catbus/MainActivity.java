package com.y.catbus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

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
        T.init(this);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "btn1发送消息线程id = " + Thread.currentThread().getId());
                EventBus.post(new Btn1EventBean("msg:按钮1的消息"));
            }
        });
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "btn2发送消息线程id = " + Thread.currentThread().getId());
                        EventBus.post(new Btn2EventBean("msg:按钮2的消息"));
                    }
                }).start();
            }
        });
        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.post(new Btn3EventBean("msg:按钮3的消息"));
            }
        });

        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressTest();
            }
        });

        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, SecondActivity.class));
                    }
                }, 2500);

                Btn1EventBean event = new Btn1EventBean("msg:按钮1的粘性消息");
                //粘性测试
                EventBus.postStick(event);
                //移除粘性消息测试
//                EventBus.removeStick(event);

            }
        });

        EventBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.unRegister(this);
    }

    private void pressTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 200; i++) {
                        Thread.sleep(100);
                        Log.e("压力测试", "次数： " + i);
                        EventBus.post(new Btn3EventBean("msg:按钮3的消息"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void btn1Event(Btn1EventBean bean) {
        Log.e(TAG, "btn1Event()接收消息线程id = " + Thread.currentThread().getId());
        show(bean.msg);
        Log.e(TAG, "btn1Event()" + bean.msg);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void btn2Event(Btn2EventBean bean) {
        Log.e(TAG, "btn2Event()接收消息线程id = " + Thread.currentThread().getId());
        show(bean.msg);
        Log.e(TAG, "btn2Event()" + bean.msg);
    }

    @Subscribe(priority = 1)
    public void btn3Event1(Btn3EventBean bean) {
        Log.e(TAG, "btn3Event1()" + bean.msg);
        show(bean.msg);
    }

    @Subscribe(priority = 2)
    public void btn3Event2(final Btn3EventBean bean) {
        Log.e(TAG, "btn3Event2()" + bean.msg);
        show(bean.msg);
        EventBus.cancelLowerPriority(bean);
    }

    @Subscribe
    public void btn3Event3(final Btn3EventBean bean) {
        Log.e(TAG, "btn3Event3()" + bean.msg);
        show(bean.msg);
    }

    private void show(String msg) {
        T.show(msg);
    }
}
