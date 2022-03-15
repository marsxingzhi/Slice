package com.mars.infra.mixin.gradle.plugin

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInvocation
import com.mars.infra.mixin.gradle.plugin.visitor.MixinCollectClassVisitor
import org.objectweb.asm.ClassReader
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

/**
 * Created by Mars on 2022/3/15
 */
object Mixin {

    /**
     * 在进行transform之前，需要先获取到Proxy注解标识的内容
     * TODO 将Transform拆分，拆成三个步骤，beforeTransform、transform以及afterTransform
     */
    fun collectHookInfo(transformInvocation: TransformInvocation) {
        transformInvocation.inputs.forEach {
            it.directoryInputs.forEach { directoryInput ->
                forEachDir(directoryInput.file)
            }
            it.jarInputs.forEach { jarInput ->
                forEachJar(jarInput)
            }
        }
    }

    // 一定需要过滤，只处理.class
    private fun forEachJar(jarInput: JarInput) {
        ZipFile(jarInput.file).use { originJar ->
            originJar.entries().iterator().forEach { zipEntry ->
                if (!zipEntry.isDirectory && zipEntry.name.endsWith(".class")) {
                    collectInternal(originJar.getInputStream(zipEntry))
                }
            }
        }
    }

    // 一定需要过滤，只处理.class
    private fun forEachDir(input: File) {
        input.listFiles()?.forEach {
            if (it.isDirectory) {
                forEachDir(it)
            } else if (it.isFile) {
                if (it.absolutePath.endsWith(".class")) {
                    collectInternal(it.inputStream())
                }
            }
        }
    }

    private fun collectInternal(inputStream: InputStream) {
        inputStream.use {
            val cr = ClassReader(it.readBytes())
            val cv = MixinCollectClassVisitor()
            cr.accept(cv, ClassReader.EXPAND_FRAMES)
        }
    }

    private fun collectInternal(file: File) {
        file.inputStream().use {
            val cr = ClassReader(it.readBytes())
            val cv = MixinCollectClassVisitor()
            cr.accept(cv, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        }
    }

}