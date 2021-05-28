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

## 后端

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

SDKs

# Main Dependencies

## Android

[Agora](https://www.agora.io/)

[Filament](https://github.com/google/filament)

[Resonance Audio](https://github.com/resonance-audio/resonance-audio)

[ML Kit](https://developers.google.com/ml-kit)

[ARCore](https://developers.google.com/ar)

## backend

## Unity
