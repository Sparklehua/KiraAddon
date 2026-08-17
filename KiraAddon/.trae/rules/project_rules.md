# KiraAddon 模组维护规则

> **优先级：最高** — 任何 AI 在修改此项目代码前，必须先完整阅读本文件。

---

## 项目身份

- **名称**：KiraAddon（吉良吉影附属包）
- **类型**：StarRailExpress（星穹铁道杀手模组）的 Fabric 附属 Addon
- **Minecraft**：1.21.1
- **构建**：Gradle + Fabric Loom
- **Java**：JDK 21
- **映射**：Mojang 官方映射（officialMojangMappings）
- **语言文件**：仅维护 `zh_cn.json` 和 `en_us.json`，无 `zh_tw.json`

## 依赖

| 依赖 | 版本 | 位置 |
|------|------|------|
| Fabric Loader | 0.18.2 | gradle.properties |
| Fabric API | 0.116.13+1.21.1 | gradle.properties |
| star_rail_express | 4.3.0 | libs/star_rail_express-4.3.0.jar |
| wathe | 1.3.2-1.21.1 | libs/wathe-1.3.2-1.21.1.jar (compileOnly) |
| cardinal-components-api | 6.1.1 | gradle.properties |

## 构建命令

```bash
./gradlew build          # 完整构建，输出在 build/libs/
./gradlew compileJava    # 仅编译检查
./gradlew clean          # 清理
```

---

## 源码结构速查

```
src/main/java/org/agmas/kiraaddon/
├── KiraAddon.java              # 主入口：注册所有组件
├── KiraAddonClient.java        # 客户端入口：渲染器、按键、HUD
├── cca/                        # CCA 数据组件
│   ├── KiraComponents.java     # 组件 KEY 注册
│   └── KiraPlayerComponent.java# Kira 状态（标记/冷却/败者食尘次数）
├── client/                     # 客户端渲染
│   ├── KiraHudRenderer.java
│   ├── JosukeHudRenderer.java
│   ├── KeyInputHandler.java    # V键：蹲下收回枯萎穿心
│   └── widget/KiraPlayerWidget.java
├── content/
│   ├── entity/SheerHeartEntity.java       # 枯萎穿心 AI 实体
│   └── item/
│       ├── SheerHeartAttackItem.java      # 枯萎穿心物品
│       └── DetonateButtonItem.java        # 引爆按钮
├── events/KiraEvents.java      # 事件：标记、引爆、败者食尘回溯
├── game/roles/
│   ├── killer/kira/
│   │   ├── KiraBitesTheDustHandler.java   # 败者食尘死亡拦截
│   │   ├── KiraShopHandler.java           # 商店
│   │   └── SheerHeartShopEntry.java
│   └── vigilante/josuke/
│       ├── JosukeFistPunchHandler.java    # 连击逻辑
│       ├── JosukePlayerComponent.java
│       └── JosukeSkillHandler.java
├── init/
│   ├── ModEntities.java / ModItems.java / ModRoles.java / ModSounds.java
├── input/KeyBindings.java      # V键绑定
├── mixin/                      # Mixin 注入
│   ├── ShopContentMixin.java
│   ├── KiraScreenMixin.java
│   └── client/PlayerBodyEntityClientMixin.java
└── network/
    ├── KiraC2SPacket.java / JosukeSkillC2SPacket.java / RecallSheerHeartPacket.java
    └── PacketHandler.java      # 服务端包处理
```

---

## 必须遵守的编码规范

### 1. 网络包（最高优先级）
```
所有 CustomPacketPayload 的 StreamCodec 必须使用 RegistryFriendlyByteBuf ！！！
严禁使用 FriendlyByteBuf，否则编译通过但运行时报错。
```

空包标准写法：
```java
public record XxxPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<XxxPacket> ID = 
        new CustomPacketPayload.Type<>(KiraAddon.id("xxx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, XxxPacket> CODEC = 
        StreamCodec.of((packet, buf) -> {}, buf -> new XxxPacket());
    @Override public CustomPacketPayload.Type<?> type() { return ID; }
}
```

### 2. CCA 组件操作
- 获取组件后 **必须判空**：`if (component != null)`
- 修改组件数据后调用 `sync()`，**只能一次**，不能重复

### 3. 按键系统
- **G 键**：已被 `GKeyRoleSkill` 占用（Kira：标记败者食尘位置 / Josuke：技能）
- **V 键**：收回枯萎穿心攻击（蹲下时触发）
- 新增按键必须避开 G 键
- 改键后必须同步更新 `zh_cn.json` 和 `en_us.json` 中所有提示文本

### 4. Mixin 配置
- `kiraaddon.mixins.json` 中 `"required"` 必须为 `true`
- 新增 Mixin 类必须在 json 对应的数组中注册

### 5. 角色系统
- 角色 ID 使用 `KiraAddon.id("kira")` 和 `KiraAddon.id("josuke")` 命名空间
- 东方仗助通过 `RoleAssignmentManager.addOccupationRole(KIRA, JOSUKE)` 绑定到吉良
- 12 人以上游戏才开放吉良（在 `KiraAddon.java` 的 tick 中控制）

### 6. 语言文件
- 只维护 `zh_cn.json` 和 `en_us.json`
- 翻译键前缀：`key.kiraaddon.` / `message.kiraaddon.` / `hud.kiraaddon.` / `star.role.kiraaddon.`

### 7. 导入语句
- 不允许有重复的 import
- 主模组类路径：`io.wifi.starrailexpress.*`
- 其他依赖类路径：`org.agmas.noellesroles.*`、`org.agmas.harpymodloader.*`

---

## 修改后必须检查的清单

每次修改代码后，AI 必须自查以下项目：

- [ ] `build.gradle` 依赖版本未变
- [ ] `fabric.mod.json` entrypoint 和 mixin 路径正确
- [ ] `kiraaddon.mixins.json` 中 `required: true`，所有 Mixin 类已注册
- [ ] `zh_cn.json` 和 `en_us.json` 翻译键一致
- [ ] 所有网络包使用 `RegistryFriendlyByteBuf`（不是 FriendlyByteBuf）
- [ ] CCA 组件获取后有 `!= null` 判空
- [ ] 无重复 import
- [ ] 按键未与 G 键冲突
- [ ] `sync()` 调用无重复
- [ ] 不做多语言文件时，只改 zh_cn.json 和 en_us.json，不创建 zh_tw.json

---

## 常见陷阱

| 陷阱 | 正确做法 |
|------|----------|
| 网络包用 `FriendlyByteBuf` | 必须用 `RegistryFriendlyByteBuf` |
| 组件不判空直接调用方法 | 先 `if (comp != null)` |
| `sync()` 调用两次 | 只调用一次 |
| 新增按键用 G 键 | 避开 G 键，改用其他键 |
| mixins.json 的 required 为 false | 改为 true |
| 导入重复 | 删除多余 import |
| 创建 zh_tw.json | 不要创建，只维护 zh_cn 和 en_us |
| 修改按键不更新语言文件 | 同步更新所有相关提示文本 |

---

## StarRailExpress 主模组 API 参考

### 角色注册 API

```java
// 1. 创建角色 — 使用 NormalRole 构造函数
// NormalRole(identifier, color, isInnocent, canUseKiller, moodType, maxSprintTime, canSeeTime)
SRERole role = TMMRoles.registerRole(new NormalRole(
    KiraAddon.id("role_name"),     // ResourceLocation — 角色 ID
    new Color(255, 215, 0).getRGB(),// int — 角色颜色
    false,                          // isInnocent — 是否平民阵营
    true,                           // canUseKiller — 是否杀手阵营
    SRERole.MoodType.FAKE,          // moodType — 心情类型（FAKE=杀手/REAL=平民/NONE=中立）
    Integer.MAX_VALUE,              // maxSprintTime — 最大冲刺时间（tick），-1 无限制
    true                            // canSeeTime — 能否看到时间
));

// 2. 链式配置
role.setCanSeeCoin(true)              // 能否看到金币
    .setCanBeRandomedByOtherRoles(false) // 是否允许被其他角色随机分配
    .setOccupiedRoleCount(2)           // 占用杀手名额数
    .setComponentKey(componentKey)     // 绑定 CCA 组件
    .setVigilanteTeam(true);           // 设为警长阵营

// 3. 重写商店
new NormalRole(...) {
    @Override
    public List<ShopEntry> getShopEntries() {
        return customShopList;
    }
}
```

### 职业绑定 API（东方仗助随吉良吉影绑定）

```java
// 将 companionRole 与 mainRole 绑定，mainRole 出现时 companionRole 自动加入
Harpymodloader.addOccupationRole(mainRole, companionRole);
// 等价写法：
RoleAssignmentManager.addOccupationRole(mainRole, companionRole);
```

### 职业数量控制 API

```java
// 设置职业最大数量（-1 表示不限制）
Harpymodloader.setRoleMaximum(roleId, maxCount);
// 例如 12 人以上开放 Kira：
Harpymodloader.setRoleMaximum(ModRoles.KIRA_ID, players >= 12 ? 1 : 0);
```

### G 键技能注册 API

```java
// 在 KiraAddonClient 中注册 G 键技能
// register(role, beforeRhapsody, handler)
GKeyRoleSkill.register(ModRoles.KIRA, true, (client, gameWorld) -> {
    // 发送网络包
    ClientPlayNetworking.send(new KiraC2SPacket(new UUID(0, 0), KiraC2SPacket.ACTION_ANCHOR_MARK));
    return true; // 返回 true 表示已处理
});
```

### 事件系统 API

所有事件都是 Fabric 的 `Event<T>` 类型，通过 `EVENT.register(callback)` 注册。

| 事件类 | 位置 | 用途 | 返回值 |
|--------|------|------|--------|
| `AllowPlayerDeath.EVENT` | `io.wifi.starrailexpress.event` | 判断玩家是否允许死亡（无杀手） | `boolean`：false 阻止死亡 |
| `AllowPlayerDeathWithKiller.EVENT` | `io.wifi.starrailexpress.event` | 判断玩家是否允许死亡（有杀手） | `boolean`：false 阻止死亡 |
| `AllowGameEnd.EVENT` | `io.wifi.starrailexpress.event` | 决定游戏是否结束及胜利状态 | `WinStatus`：NOT_MODIFY 不修改 |
| `OnPlayerKilledPlayer.EVENT` | `io.wifi.starrailexpress.event` | 玩家击杀玩家后触发 | void（非拦截） |
| `OnGameEnd.EVENT` | `io.wifi.starrailexpress.event` | 游戏结束时触发 | void |
| `OnGameTrueStarted.EVENT` | `io.wifi.starrailexpress.event` | 游戏真正开始时触发 | void |

### 击杀玩家 API

```java
// 标准击杀（走死亡事件流程）
GameUtils.killPlayer(victim, spawnBody, killer, deathReason);
// 参数：victim=受害者, spawnBody=是否生成尸体, killer=杀手(可为null), deathReason=死亡原因

// 强杀（跳过死亡事件检查，慎用）
GameUtils.forceKillPlayer(victim, spawnBody, killer, deathReason);
```

### 死亡原因常量（GameConstants.DeathReasons）

```java
GameConstants.DeathReasons.GENERIC       // 通用
GameConstants.DeathReasons.GRENADE       // 手雷（枯萎穿心攻击爆炸用此原因）
GameConstants.DeathReasons.BROKEN_HEART  // 恋人殉情
GameConstants.DeathReasons.GOD_COMMAND   // 强制处决
Noellesroles.id("voodoo")                // 巫毒处决
```

### 修饰符系统（WorldModifierComponent）

```java
// 获取组件
WorldModifierComponent wmc = WorldModifierComponent.KEY.get(level);

// 添加修饰符（如 JEB_ 静音）
wmc.addModifier(playerUuid, SEModifiers.JEB_);

// 移除修饰符
wmc.removeModifier(playerUuid, SEModifiers.JEB_);

// 检查是否有修饰符
if (wmc.isModifier(playerUuid, SEModifiers.JEB_)) { ... }
```

### 游戏状态检查

```java
// 获取游戏世界组件
SREGameWorldComponent gameWorld = SREGameWorldComponent.KEY.get(level);

// 检查游戏是否运行
gameWorld.isRunning()

// 检查玩家角色
gameWorld.isRole(player, ModRoles.KIRA)

// 获取玩家角色
SRERole role = gameWorld.getRole(player)

// 检查是否是杀手阵营
role.isKillerTeam()
```

### 工具方法

```java
// 检查玩家是否存活且为生存模式
GameUtils.isPlayerAliveAndSurvival(player)

// 获取玩家出生点
GameUtils.getSpawnPos(areasComponent, roomNumber)

// 房间映射
GameUtils.roomToPlayer.getOrDefault(playerUuid, 1)

// 延迟任务
GameUtils.serverAsynTaskLists.add(new ServerTaskInfoClasses.SchedulerTask(ticks, () -> { ... }));
```

### 商店系统

两种方式自定义商店：

**方式一：重写角色 getShopEntries()（推荐）**
```java
new NormalRole(...) {
    @Override
    public List<ShopEntry> getShopEntries() {
        return List.of(
            new ShopEntry(itemStack, price, type),
            new CustomShopEntry(itemStack, price, type)
        );
    }
}
```

**方式二：Mixin 注入 ShopContent.getShopEntries()**
```java
@Mixin(ShopContent.class)
public class ShopContentMixin {
    @Inject(method = "getShopEntries", at = @At("HEAD"), cancellable = true)
    private static void customShop(ResourceLocation role, CallbackInfoReturnable<List<ShopEntry>> cir) {
        if (roleId.equals(role)) {
            cir.setReturnValue(customList);
        }
    }
}
```

### 主模组关键类路径

| 类 | 路径 |
|----|------|
| TMMRoles | `io.wifi.starrailexpress.api.TMMRoles` |
| NormalRole | `io.wifi.starrailexpress.api.NormalRole` |
| SRERole | `io.wifi.starrailexpress.api.SRERole` |
| GameUtils | `io.wifi.starrailexpress.game.GameUtils` |
| GameConstants | `io.wifi.starrailexpress.game.GameConstants` |
| ShopContent | `io.wifi.starrailexpress.game.ShopContent` |
| ShopEntry | `io.wifi.starrailexpress.util.ShopEntry` |
| SREGameWorldComponent | `io.wifi.starrailexpress.cca.SREGameWorldComponent` |
| SREPlayerShopComponent | `io.wifi.starrailexpress.cca.SREPlayerShopComponent` |
| SREGameTimeComponent | `io.wifi.starrailexpress.cca.SREGameTimeComponent` |
| AreasWorldComponent | `io.wifi.starrailexpress.cca.AreasWorldComponent` |
| AllowPlayerDeath | `io.wifi.starrailexpress.event.AllowPlayerDeath` |
| AllowPlayerDeathWithKiller | `io.wifi.starrailexpress.event.AllowPlayerDeathWithKiller` |
| AllowGameEnd | `io.wifi.starrailexpress.event.AllowGameEnd` |
| OnPlayerKilledPlayer | `io.wifi.starrailexpress.event.OnPlayerKilledPlayer` |
| OnGameEnd | `io.wifi.starrailexpress.event.OnGameEnd` |
| OnGameTrueStarted | `io.wifi.starrailexpress.event.OnGameTrueStarted` |
| WorldModifierComponent | `org.agmas.harpymodloader.component.WorldModifierComponent` |
| SEModifiers | `pro.fazeclan.river.stupid_express.constants.SEModifiers` |
| Harpymodloader | `org.agmas.harpymodloader.Harpymodloader` |
| RoleAssignmentManager | `org.agmas.harpymodloader.modded_murder.RoleAssignmentManager` |
| GKeyRoleSkill | `org.agmas.noellesroles.client.GKeyRoleSkill` |
| ModEffects | `org.agmas.noellesroles.init.ModEffects` |
| Noellesroles | `org.agmas.noellesroles.Noellesroles` |