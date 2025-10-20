plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.fashion.devbrowser"
version = "1.1.0"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // IntelliJ Platform Gradle Plugin 2.x 依赖配置
    intellijPlatform {
        intellijIdeaCommunity("2025.2.3")

        // 插件依赖
        bundledPlugin("com.intellij.java")

        // 插件签名和验证
        pluginVerifier()
        zipSigner()
    }
}

// 配置 IntelliJ Platform
intellijPlatform {
    pluginConfiguration {
        id = "com.fashion.devbrowser"
        name = "DevBrowser"
        version = "1.1.0"

        ideaVersion {
            sinceBuild = "243"
            untilBuild = "252.*"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    // 禁用 buildSearchableOptions 任务以避免 kotlinx-coroutines-debug 问题
    buildSearchableOptions = false
}

tasks {
    // 设置 JVM 兼容性版本（JDK 17，但推荐 JDK 21）
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
