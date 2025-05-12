import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.zt.signature"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        // exe免安装打包
        // Issues：https://github.com/JetBrains/compose-multiplatform/issues/3910
        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "automatic-signature"
            packageVersion = "1.0.0"

            windows {
                // 设置图标文件
                iconFile.set(project.file("android_icon.ico"))
                // 设置输出目录
                outputBaseDir.set(project.layout.buildDirectory.dir("release"))
            }
        }
    }
}

/**
 * 注册distribution任务分组
 *
 * 注册打包为 zip 文件的任务
 *
 * 打印日志的时候不要出现中文
 */
tasks.register("zipReleaseDistributable") {
    group = "distribution"
    dependsOn("createReleaseDistributable")

    doLast {
        val version = project.version.toString()
        val outputDir = File(buildDir, "release/main-release/app")
        val sourceDir = File(outputDir, "automatic-signature")
        val zipFile = File(project.file("release"), "automatic-signature-$version.zip")

        // 删除旧 zip 文件
        if (zipFile.exists()) {
            println("Delete the old zip file ${zipFile.absolutePath}")
            zipFile.delete()
        }

        // 执行压缩命令（调用 PowerShell）
        val command = listOf(
            "powershell", "-Command",
            "Compress-Archive -Path '${sourceDir.absolutePath}\\*' -DestinationPath '${zipFile.absolutePath}'"
        )

        println("Start compressing to zip...")

        val result = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
            .apply {
                inputStream.bufferedReader().useLines { it.forEach(::println) }
            }
            .waitFor()

        if (result == 0 && zipFile.exists()) {
            println("Success in creation ${zipFile.absolutePath}")
        } else {
            throw GradleException("Error ZIP 压缩失败，退出码 $result")
        }
    }
}

/**
 * 注册distribution任务分组
 * 注册绿色软件打包任务
 *
 * 完成时间：2025-5-8 15:38
 */
tasks.register("packageReleaseGreenSoftware") {
    group = "distribution"
    dependsOn("createReleaseDistributable", "zipReleaseDistributable")
}