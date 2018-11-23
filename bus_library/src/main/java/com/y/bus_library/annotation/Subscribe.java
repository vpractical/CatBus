package com.y.bus_library.annotation;

import com.y.bus_library.ThreadMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
    /**
     * 在哪个线程执行注解方法
     */
    ThreadMode threadMode() default ThreadMode.POSTING;

    /**
     * 是否拒绝接受粘性消息
     */
    boolean refuseStick() default false;

    /**
     * 注解方法优先级,数字越大优先级越高
     */
    int priority() default 0;
}
