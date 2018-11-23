package com.y.bus_library.bean;

import com.y.bus_library.ThreadMode;

import java.lang.reflect.Method;

/**
 * 保存订阅方法
 */
public class MethodInfo {
    //类对象
    public Object getter;
    //消息类型
    public Class<?> type;
    //回调线程
    public ThreadMode threadMode;
    //回调方法
    public Method method;
    //是否是粘性消息
    public boolean refuseStick;
    //方法优先级
    public int priority;

    public MethodInfo(Object getter,Class<?> type, ThreadMode threadMode, Method method,int priority,boolean refuseStick) {
        this.getter = getter;
        this.type = type;
        this.threadMode = threadMode;
        this.method = method;
        this.priority = priority;
        this.refuseStick = refuseStick;
    }
}
