package com.y.bus_library;

import android.os.Handler;
import android.os.Looper;

import com.y.bus_library.annotation.Subscribe;
import com.y.bus_library.bean.MethodInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 事件总线代理类
 */
public class EventHelper {

    private static EventHelper instance = new EventHelper();
    private Handler handler;
    private ExecutorService executorService;

    private EventHelper() {
        handler = new Handler(Looper.getMainLooper());
        executorService = Executors.newCachedThreadPool();
    }

    public static EventHelper getInstance() {
        return instance;
    }

    /**
     * 中断传递的事件缓存
     */
    private List<Object> cacheCancelList = new ArrayList<>(3);

    /**
     * 中断事件在不同优先级方法中的传递
     * @param setter 事件对象
     */
    public void cancelLowerPriority(Object setter){
        if(!cacheCancelList.contains(setter)){

            if(cacheCancelList.size() == 3){
                cacheCancelList.remove(2);
            }
            cacheCancelList.add(setter);
        }
    }

    /**
     * 寻找带注解方法
     *
     * @param getter
     * @return
     */
    public List<MethodInfo> findAnnotationMethod(Object getter) {
        List<MethodInfo> list = new ArrayList<>();
        Class<?> clazz = getter.getClass();
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
            MethodInfo methodInfo = new MethodInfo(getter,paramsTypes[0], subscribe.threadMode(), method, subscribe.priority(), subscribe.refuseStick());
            list.add(methodInfo);
        }
        return list;
    }

    /**
     * 发送消息
     * @param setter 消息对象
     */
    public void post(Map<Object, List<MethodInfo>> cacheMap, final Object setter) {
        List<MethodInfo> matchedMethods = new ArrayList<>();
        Set<Object> set = cacheMap.keySet();
        for (final Object getter : set) {
            List<MethodInfo> list = cacheMap.get(getter);
            if (list != null) {
                for (final MethodInfo info : list) {
                    //判断是否这个类的对象
                    if (info.type.isAssignableFrom(setter.getClass())) {
                        matchedMethods.add(info);
                    }
                }
            }
        }

        execute(matchedMethods,setter);
    }

    /**
     * 找到所有匹配的注解方法，执行
     * 如果是顺序事件，判断是否被取消传递
     * @param matchedMethods
     * @param setter
     */
    private void execute(List<MethodInfo> matchedMethods,Object setter){
        sortByPriority(matchedMethods);
        for (int i = 0; i < matchedMethods.size(); i++) {
            if(cacheCancelList.contains(setter)){
                break;
            }
            execute(matchedMethods.get(i),setter);
        }
        cacheCancelList.remove(setter);
    }

    /**
     * 线程调度
     */
    private void execute(final MethodInfo info,final Object setter){
        switch (info.threadMode) {
            case MAIN:
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    invoke(info, setter);
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            invoke(info, setter);
                        }
                    });
                }
                break;
            case POSTING:
                invoke(info, setter);
                break;
            case BACKGROUND:
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            invoke(info, setter);
                        }
                    });
                } else {
                    invoke(info, setter);
                }
                break;
            case ASYNC:
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        invoke(info, setter);
                    }
                });
                break;
        }
    }

    /**
     * 执行注解方法
     * @param info   方法封装对象
     * @param setter 消息对象的封装
     */
    private void invoke(MethodInfo info, Object setter) {
        try {
            if (EventBus.isStick(setter) && info.refuseStick) {
                //是粘性消息，方法拒绝粘性消息，则不执行
                return;
            }
            info.method.setAccessible(true);
            info.method.invoke(info.getter, setter);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将注解方法按优先级排序
     * @param matchedMethods
     */
    private void sortByPriority(List<MethodInfo> matchedMethods) {
        Collections.sort(matchedMethods,comparator);
    }

    private final Comparator<MethodInfo> comparator = new Comparator<MethodInfo>() {
        @Override
        public int compare(MethodInfo methodInfo, MethodInfo t1) {
            if(methodInfo.priority > t1.priority){
                return -1;
            }else if(methodInfo.priority < t1.priority){
                return 1;
            }
            return 0;
        }
    };

}
