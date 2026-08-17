# KiraAddon

StarRailExpress（残月列车）的 JOJO 角色附属包，添加了**吉良吉影**和**东方仗助**两个角色。

## 非官方角色附属模组声明

本模组是 StarRailExpress（残月列车 / The Harpy Express）的非官方角色附属模组。

- 非残月团队官方作品，未经官方认可，与残月团队无任何从属或合作关系；
- 原模组（StarRailExpress）开源仓库：[https://github.com/catmoon-train/StarRailExpress](https://github.com/catmoon-train/StarRailExpress)；
- 本模组采用 GPL-3.0 协议开源（见 LICENSE），与上游 StarRailExpress 保持一致。

## 简介

残月哈比快车服务器的整合模组，为 StarRailExpress 提供：

- **吉良吉影（杀手阵营）**：标记、引爆、败者食尘、枯萎穿心攻击等技能；
- **东方仗助（警长阵营）**：疯狂钻石连打、技能清除、复活等技能；
- 角色商店、CCA 组件绑定、网络数据包同步等完整角色系统。

## 构建

### 前提

构建前需要将 StarRailExpress 及其依赖wathe的jar 放入 `libs/` 目录，详见 `build.gradle`。

### 构建命令

```bash
# JDK 21
./gradlew build        # Windows / Linux / macOS
```

产物：`build/libs/KiraAddon-1.0.0.jar`

注意：替换 mods 目录中的 jar 前请先退出游戏（运行中替换 jar/配置会因类加载错乱而崩溃）。

## 许可

GPL-3.0（与上游 StarRailExpress 相同；上游 LICENSE 原样保留于本仓库）