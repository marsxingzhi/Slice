package com.mars.infra.mixin.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.mars.infra.mixin.gradle.plugin.core.Mixin
import com.mars.infra.mixin.gradle.plugin.ext.no
import com.mars.infra.mixin.gradle.plugin.ext.yes
import com.mars.infra.mixin.gradle.plugin.visitor.MixinClassNode
import com.mars.infra.mixin.gradle.plugin.visitor.MixinClassVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Created by Mars on 2022/3/14
 */
class MixinTransform : Transform() {

    override fun getName(): String = "MixinTransform"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental(): Boolean = false

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)

        val outputProvider = transformInvocation.outputProvider
        transformInvocation.isIncremental.no {
            outputProvider.deleteAll()
        }

        Mixin.collectHookInfo(transformInvocation)

        transformInvocation.inputs.forEach {
            it.directoryInputs.forEach { directoryInput ->
                val output = outputProvider!!.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                processDir(directoryInput.file, output)
            }
            it.jarInputs.forEach { jarInput ->
                var destName = jarInput.file.name
                // jar包重命名，因为可能存在同名文件，同名文件会覆盖
                val hash = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length - 4)
                }
                val output = outputProvider!!.getContentLocation(
                    "${destName}_${hash}",
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                processJar(jarInput, output)
            }
        }
    }

    private fun processJar(jarInput: JarInput, output: File) {
        ZipOutputStream(output.outputStream()).use { zos ->
            zos.setMethod(ZipOutputStream.STORED)  // 设置压缩方法，这里仅打包归档存储
            ZipFile(jarInput.file).use { originJar ->
//                println("MixinTransform---processJar---originJar name = ${originJar.name}")
                originJar.entries().iterator().forEach { zipEntry ->
                    if (!zipEntry.isDirectory && zipEntry.name.endsWith(".class")) {
//                        println("MixinTransform---processJar---zipEntry: ${zipEntry.name}")
                        val inputStream = originJar.getInputStream(zipEntry).readBytes()
                        doTransform(inputStream)?.let {
                            zos.writeEntry(zipEntry.name, it)
                        }
                    }
                }
                println("=== ===")
            }
        }
    }



    private fun processDir(input: File, output: File) {
        output.exists().yes {
            FileUtils.forceDelete(output)
        }
        com.android.utils.FileUtils.mkdirs(output)

        val srcDirPath = input.absolutePath
        val destDirPath = output.absolutePath

        input.listFiles()?.forEach {
            val destFilePath = it.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            if (it.isDirectory) {
                processDir(it, destFile)
            } else if (it.isFile) {
                if (it.absolutePath.endsWith(".class")) {
//                    println("MixinTransform---processDir---inputPath: ${it.absolutePath}")
//                    println("MixinTransform---processDir---outputPath: ${destFile.absolutePath}")
//                    println("=== ===")
                    weave(FileInputStream(it.absolutePath), destFile)
                } else {
                    FileUtils.copyFile(it, destFile)
                }
            }
        }
    }

    private fun weave(fileInputStream: FileInputStream, destFile: File) {
        fileInputStream.use {
            doTransform(it.readBytes())?.let { bytes ->
                val fileOutputStream = FileOutputStream(destFile)
                bytes.write(fileOutputStream)
            }
        }
    }

    private fun ByteArray.write(outputStream: FileOutputStream) {
        outputStream.use {
            it.write(this)
        }
    }

    /**
     * 一般地，flag设置为ClassWriter.COMPUTE_FRAMES、ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
     * 但是这里如上设置，在编译期出现Type androidx/transition/TransitionSet not present的错误...
     * TODO 未找到原因，暂时将ClassWriter.COMPUTE_FRAMES改成0
     */
    private fun doTransform(inputStream: ByteArray): ByteArray? {
        val cr = ClassReader(inputStream)
//        val cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES)
        val cw = ClassWriter(cr, 0)

        val mixinClassVisitor = MixinClassVisitor(cw)

        val mixinClassNode = MixinClassNode(mixinClassVisitor)
//        cr.accept(mixinClassVisitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        cr.accept(mixinClassNode, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    private fun ZipOutputStream.writeEntry(entryName: String, byteArray: ByteArray) {
        val crc32 = CRC32()
        crc32.apply {
            reset()
            update(byteArray)
        }
        val zipEntry = ZipEntry(entryName)
        zipEntry.also {
            it.size = byteArray.size.toLong()
            it.crc = crc32.value
        }
        putNextEntry(zipEntry)
        write(byteArray)
    }

}