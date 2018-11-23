package com.y.bus_library;

import java.lang.reflect.Method;

/**
 * 保存订阅方法
 */
public class MethodInfo {
    //回调参数类型
    public Class<?> type;
    //回调线程
    public ThreadMode threadMode;
    //回调方法
    public Method method;

    public MethodInfo(Class<?> type, ThreadMode threadMode, Method method) {
        this.type = type;
        this.threadMode = threadMode;
        this.method = method;
    }

}
