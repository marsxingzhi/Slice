package com.mars.infra.mixin.annotations

/**
 * Created by Mars on 2022/3/15
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class Proxy(
    val owner: String,
    val name: String
)
