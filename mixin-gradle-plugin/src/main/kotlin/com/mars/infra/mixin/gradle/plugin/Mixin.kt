package com.mars.infra.mixin.gradle.plugin

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInvocation
import com.mars.infra.mixin.gradle.plugin.visitor.MixinCollectClassVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

/**
 * Created by Mars on 2022/3/15
 */
object Mixin {

    val mixinDataList by lazy {
        arrayListOf<MixinData>()
    }

    fun clear() {
        mixinDataList.clear()
    }


    /**
     * 在进行transform之前，需要先获取到Proxy注解标识的内容
     * TODO 将Transform拆分，拆成三个步骤，beforeTransform、transform以及afterTransform
     */
    fun collectHookInfo(transformInvocation: TransformInvocation) {
        clear()
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
                // 不需要遍历所有的类
                // 这里的注释不要删
//                if (!zipEntry.isDirectory && zipEntry.name.endsWith(".class")) {
//                    collectInternal(originJar.getInputStream(zipEntry))
//                }

                if (!zipEntry.isDirectory
                    && zipEntry.name.endsWith(".class")
                    && zipEntry.name.contains(TEMP_HOOK_CLASS)) {
                    collectInternalV2(originJar.getInputStream(zipEntry))
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
                // 不需要遍历所有的类
                // 这里的注释不要删
//                if (it.absolutePath.endsWith(".class")) {
//                    collectInternal(it.inputStream())
//                }

                if (it.absolutePath.equals(".class")
                    && it.absolutePath.contains(TEMP_HOOK_CLASS)) {
                    collectInternalV2(it.inputStream())
                }
            }
        }
    }

    private fun collectInternalV2(inputStream: InputStream) {
        inputStream.use {
            val classReader = ClassReader(it.readBytes())
            val classNode = ClassNode()
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES)

            classNode.handleNode()
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

private fun ClassNode.handleNode() {
    methods.asIterable().filter {
        it.access and Opcodes.ACC_ABSTRACT == 0
                && it.access and Opcodes.ACC_NATIVE == 0
                && it.name != "<init>"
    }.forEach { methodNode ->
//        println("handleNode---methodNode = ${methodNode.name}")
        val mixinData = MixinData(name, methodNode.name, methodNode.desc, methodNode = methodNode)

        methodNode.invisibleAnnotations.forEach { annotationNode ->
//            println("handleNode---annotationNode = ${annotationNode.desc}")
            if (annotationNode.desc == ANNOTATION_PROXY) {
                var index = 0
                val owner = annotationNode.values[++index] as String
                index++
                val name = annotationNode.values[++index] as String
                mixinData.proxyData = ProxyData(owner, name)

                println("handleNode---mixinData = $mixinData")
                Mixin.mixinDataList.add(mixinData)
            }
        }
    }
}