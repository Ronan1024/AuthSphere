package com.authsphere.server.common.utils;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.function.Supplier;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/23
 */
public class Assert {



    public static void isEmpty(Object obj, Supplier<RuntimeException> messageSupplier){
        if (ObjectUtils.isEmpty(obj)) {
            throw messageSupplier.get();
        }
    }

    public static void notEmpty(Object obj, Supplier<RuntimeException> messageSupplier){
        if (!ObjectUtils.isEmpty(obj)) {
            throw messageSupplier.get();
        }
    }


    public static void  isTrue(boolean expression, Supplier<RuntimeException> messageSupplier){
        if (expression) {
            throw messageSupplier.get();
        }
    }

    public static void  isFalse(boolean expression, Supplier<RuntimeException> messageSupplier){
        if (!expression) {
            throw messageSupplier.get();
        }
    }
}
