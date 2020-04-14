# Android Rtsp Video Streaming Library For Telpo Device

## 使用
* **添加 JitPack 仓库到项目的根 build.gradle**
<br>Add JitPack repository in your root build.gradle:
```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

* **添加依赖到 app 的 build.gradle**
<br>Add the dependency:
```
    dependencies {
        implementation 'com.github.telpouc:rtsplive:1.0.1'
    }
```

* **配置使用 Java 8**
<br>添加 compileOptions 如下:
```
    android {
        ...
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
    }
```

* **API**
<br>具体 API 用法请参考 [Demo](https://github.com/telpouc/rtsplive/tree/master/app)
