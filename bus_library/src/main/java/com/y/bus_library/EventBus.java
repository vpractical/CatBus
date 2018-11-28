package com.y.bus_library;

import com.y.bus_library.bean.MethodInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仿EventBus
 * 1.事件传递
 * 2.线程调度
 * 3.粘性事件，可以有多个，但需要自己移除
 * 4.顺序事件
 */
public class EventBus {

    private static volatile EventBus instance = new EventBus();
    private static final EventHelper helper = EventHelper.getInstance();

    /**
     * 保存带注解的回调方法,key是类对象
     */
    private Map<Object, List<MethodInfo>> cacheMap = new HashMap<>();

    /**
     * 粘性事件的最大数量
     */
    private static final int NUM_STICK_MAX = 10;
    /**
     * 保存粘性事件,key是事件，value是事件封装
     */
    private List<Object> cacheStickList = new ArrayList<>(NUM_STICK_MAX);

    private EventBus() {
    }

    /**
     * 注册类时，与粘性消息匹配，然后在存入映射map
     *
     * @param getter 类对象
     */
    public static void register(Object getter) {
        List<MethodInfo> list = instance.cacheMap.get(getter);
        if (list == null) {
            list = helper.findAnnotationMethod(getter);
            //注册查找完一个类的注解方法后，匹配粘性消息
            Map<Object, List<MethodInfo>> map = new HashMap<>();
            map.put(getter, list);

            for (Object setter : instance.cacheStickList) {
                helper.post(map, setter);
            }
            //将查找到的带注解方法放入map
            instance.cacheMap.putAll(map);
        }
    }

    /**
     * 注销一个类
     *
     * @param getter
     */
    public static void unRegister(Object getter) {
        if (instance.cacheMap.containsKey(getter)) {
            instance.cacheMap.remove(getter);
        }
    }

    /**
     * 发送消息
     *
     * @param setter 消息对象
     */
    public static void post(Object setter) {
        helper.post(instance.cacheMap, setter);
    }

    /**
     * 发送粘性消息，可以用于跳转activity传值
     *
     * @param setter
     */
    public static void postStick(Object setter) {
        helper.post(instance.cacheMap, setter);
        if (!instance.cacheStickList.contains(setter)) {
            if (instance.cacheStickList.size() == NUM_STICK_MAX) {
                instance.cacheStickList.remove(NUM_STICK_MAX - 1);
            }
            instance.cacheStickList.add(setter);
        }
    }

    /**
     * 移除粘性消息
     *
     * @param setter
     */
    public static void removeStick(Object setter) {
        if (instance.cacheStickList.contains(setter)) {
            instance.cacheStickList.remove(setter);
        }
    }

    public static void removeAllStick() {
        instance.cacheStickList.clear();
    }

    /**
     * 取消事件在不同优先级方法中的传递
     *
     * @param setter 事件对象
     */
    public static void cancelLowerPriority(Object setter) {
        helper.cancelLowerPriority(setter);
    }

    /**
     * 是否粘性消息
     *
     * @param setter
     * @return
     */
    public static boolean isStick(Object setter) {
        return instance.cacheStickList.contains(setter);
    }

}
