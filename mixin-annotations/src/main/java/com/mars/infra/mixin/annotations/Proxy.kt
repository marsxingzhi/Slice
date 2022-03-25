package com.mars.infra.mixin.annotations

/**
 * Created by Mars on 2022/3/15
 *
 * 本来想Proxy新增一个desc的参数，但是作为sdk，业务方hook方法时，如果需要传入描述符，是不是太扯了？
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class Proxy(
    val owner: String,
    val name: String,
    val isStatic: Boolean  // 目标方法是否是静态方法
)
