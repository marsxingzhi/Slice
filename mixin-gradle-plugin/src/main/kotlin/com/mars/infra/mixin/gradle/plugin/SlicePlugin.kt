package com.mars.infra.mixin.gradle.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by Mars on 2022/3/14
 */
class SlicePlugin: Plugin<Project> {

    override fun apply(project: Project) {
        println("MixinPlugin---😄")
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(SliceTransform())
    }
}