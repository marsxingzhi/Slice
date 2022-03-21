//package com.mars.infra.mixin;
//
//import com.mars.infra.mixin.annotations.Mixin;
//import com.mars.infra.mixin.annotations.Proxy;
//
///**
// * Created by Mars on 2022/3/21
// */
//@Mixin
//class TestMixin {
//
//    @Proxy(owner = "android/util/Log", name = "i")
//    public static int testHook(String tag, String msg) {
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("TestMixin--->testHook success, tag = " + tag + " msg = " + msg);
//        return -1;
//    }
//}
