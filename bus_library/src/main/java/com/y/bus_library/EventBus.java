package com.y.bus_library;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.y.bus_library.annotation.Subscribe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBus {
    private static final String TAG = "EventBus";

    private static volatile EventBus instance = new EventBus();
    public static EventBus getInstance() {
        return instance;
    }

    //保存带注解的回调方法
    private Map<Object, List<MethodInfo>> cacheMap;
    private Handler handler;
    private ExecutorService executorService;

    private EventBus() {
        cacheMap = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());
        executorService = Executors.newCachedThreadPool();
    }

    public void register(Object getter) {
        List<MethodInfo> list = cacheMap.get(getter);
        if (list == null) {
            list = findAnnotationMethod(getter);
            cacheMap.put(getter, list);
        }
    }

    public void unRegister(Object getter){
        if(cacheMap.containsKey(getter)){
            cacheMap.remove(getter);
        }
    }

    private List<MethodInfo> findAnnotationMethod(Object getter) {
        Log.e(TAG,"寻找带注解方法: " + getter.getClass().getName());
        List<MethodInfo> list = new ArrayList<>();
        Class<?> clazz = getter.getClass();

            //系统类，跳出
//            String clazzName = clazz.getName();
//            if (clazzName.startsWith("java.") || clazzName.startsWith("javax.") || clazzName.startsWith("android.")) {
//                break;
//            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                if (subscribe == null) {
                    continue;
                }

                //方法必须是返回void
                if (!"void".equals(method.getGenericReturnType().toString())) {
                    throw new RuntimeException("method must return void");
                }

                //方法参数校验
                Class<?>[] paramsTypes = method.getParameterTypes();
                if (paramsTypes.length != 1) {
                    throw new RuntimeException("method must has only one params");
                }

                //方法符合规则，加入缓存
                MethodInfo methodInfo = new MethodInfo(paramsTypes[0], subscribe.threadMode(), method);
                list.add(methodInfo);
            }
        return list;
    }

    public void post(final Object setter) {
        Log.e(TAG,"发送消息: " + setter.getClass().getName());
        Set<Object> set = cacheMap.keySet();
        for (final Object getter:set) {
            List<MethodInfo> list = cacheMap.get(getter);
            if(list != null){
                for (final MethodInfo info:list) {
                    //判断是否这个类的对象
                    if(info.type.isAssignableFrom(setter.getClass())){
                        switch (info.threadMode){
                            case MAIN:
                                if(Looper.getMainLooper() == Looper.myLooper()){
                                    invoke(info, getter, setter);
                                }else{
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            invoke(info,getter,setter);
                                        }
                                    });
                                }
                                break;
                            case POSTING:
                                invoke(info,getter,setter);
                                break;
                            case BACKGROUND:
                                if(Looper.getMainLooper() == Looper.myLooper()){
                                    executorService.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            invoke(info, getter, setter);
                                        }
                                    });
                                }else{
                                    invoke(info, getter, setter);
                                }
                                break;
                            case ASYNC:
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(info, getter, setter);
                                    }
                                });
                                break;
                        }
                    }
                }
            }
        }
    }

    private void invoke(MethodInfo info, Object getter, Object setter) {
        try {
            Log.e(TAG,"执行注解方法: " + info.method.getName());
            info.method.invoke(getter,setter);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

}
