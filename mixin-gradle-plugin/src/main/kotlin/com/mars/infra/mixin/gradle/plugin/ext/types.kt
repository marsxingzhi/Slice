package com.mars.infra.mixin.gradle.plugin.ext

import org.objectweb.asm.Type


val TYPE_ANY = Type.getType(Any::class.java)

val typeMap = mutableMapOf(
    Type.SHORT_TYPE to Type.getType(Short::class.javaObjectType),
    Type.INT_TYPE to Type.getType(Int::class.javaObjectType),
    Type.LONG_TYPE to Type.getType(Long::class.javaObjectType),
    Type.DOUBLE_TYPE to Type.getType(Double::class.javaObjectType),
    Type.FLOAT_TYPE to Type.getType(Float::class.javaObjectType),
    Type.BOOLEAN_TYPE to Type.getType(Boolean::class.javaObjectType),
    Type.CHAR_TYPE to Type.getType(Char::class.javaObjectType)
)

val boxNameMap = mutableMapOf(
    Type.SHORT_TYPE to "shortValue",
    Type.INT_TYPE to "intValue",
    Type.LONG_TYPE to "longValue",
    Type.DOUBLE_TYPE to "doubleValue",
    Type.FLOAT_TYPE to "floatValue",
    Type.BOOLEAN_TYPE to "booleanValue",
    Type.CHAR_TYPE to "charValue"
)