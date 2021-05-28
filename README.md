# Compile

## Android

### 基础构建
不需要对场景材质光照模型等做出修改的情况下，使用 **Android Studio 4.2.0** 及以上版本打开项目根目录下的 android 项目即可直接编译

### 完整构建
如果需要更改材质光照模型，需要配置 Filament 的二进制依赖。

在 [Filament Release Page](https://github.com/google/filament/releases) 下载 1.9.21 版本的 host 平台的二进制文件并解压

在 android 项目的 local.properties 添加 filament.dir=bin_path，触发编译即可

```
filament.dir=/Users/abc/path/to/filament
```

## Backend

后端使用 rust 语言开发，需要配置 rust 环境。

参考 [rust install](https://www.rust-lang.org/tools/install) 安装 cargo 后
在项目根目录下执行 
```
cargo build --manifest-path backend/Cargo.toml
``` 
默认监听 12305 端口

## Unity

Unity 模块**强依赖** [NOITOM HI5 VR 手套](https://www.noitom.com.cn/hi5-vr-glove.html) 来提供手部动作捕捉能力

如果**没有手套硬件**的话无法完整的体验 Unity 模块的所有能力

项目使用 Unity-2019.4.18f

### Vive

在 Package Manager 中，添加 Vive 注册表详细信息，以便 unity 能够找到需要的依赖：

Name: Vive

URL: https://npm-registry.vive.com

Scope: com.htc.upm

添加完毕后可以在 my registry 中安装 Vive 相关依赖

### XR-SDK

在 Package Manager 中，下载 XR interaction 依赖

### SteamVR

输入模块使用了 steamVR，需要先安装 [steamVR](https://store.steampowered.com/steamvr) ，然后使用设置房间规模，并进行设备的配对

详细配置方式以及声网模块的安装可参见[官方推文](https://mp.weixin.qq.com/s/gNEBTpwPxl-7ZrhmD8yHFg  ) 

# Main Dependencies

## Android

- [Agora](https://www.agora.io/)

- [Filament](https://github.com/google/filament)

- [Resonance Audio](https://github.com/resonance-audio/resonance-audio)

- [ML Kit](https://developers.google.com/ml-kit)

## Backend

- [hyper](https://hyper.rs/)

- [serde](https://docs.rs/serde/1.0.126/serde/)

## Unity
- [Vive](https://www.vive.com)

- [XR-SDK](https://docs.unity3d.com/ru/2017.2/Manual/XR-SDK_overviews.html)

- [UniGLTF](https://github.com/ousttrue/UniGLTF.git)
