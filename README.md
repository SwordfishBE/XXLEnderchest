# XXL Enderchest

Server-side Fabric mod for Minecraft Java Edition that expands the ender chest beyond the vanilla 3 rows.

[![GitHub Release](https://img.shields.io/github/v/release/SwordfishBE/XXLEnderchest?display_name=release&logo=github)](https://github.com/SwordfishBE/XXLEnderchest/releases)
[![GitHub Downloads](https://img.shields.io/github/downloads/SwordfishBE/XXLEnderchest/total?logo=github)](https://github.com/SwordfishBE/XXLEnderchest/releases)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/Gq8wsb3l?logo=modrinth&logoColor=white&label=Modrinth%20downloads)](https://modrinth.com/mod/Gq8wsb3l)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1487878?logo=curseforge&logoColor=white&label=CurseForge%20downloads)](https://www.curseforge.com/minecraft/mc-mods/xxl-enderchest)

## ✨ Features

| Rows | Slots | Use case |
|------|-------|----------|
| 3 | 27 | Vanilla default, always available to everyone |
| 4 | 36 | Extra storage for selected players or groups |
| 5 | 45 | Mid-tier storage upgrade |
| 6 | 54 | Maximum XXL storage |

- Vanilla access is preserved: every player can always use a normal 3-row ender chest.
- Fixed config mode for simple servers or single-player worlds.
- Optional LuckPerms mode for per-group row upgrades.
- Automatic fallback to config mode if LuckPerms is enabled in config but the mod is not installed.
- Hidden rows keep their items stored safely; lowering access does not delete inventory contents.
- Works on dedicated servers and in single-player.

## ❗ How it works

XXL Enderchest always keeps the internal ender chest container at 54 slots so data is never thrown away.

What players can actually open depends on the active mode:

- `enabled=false`: vanilla behavior only, 3 rows.
- `enabled=true` and `useLuckPerms=false`: everyone gets the configured `rows` value.
- `enabled=true` and `useLuckPerms=true` with LuckPerms installed: everyone keeps vanilla 3 rows by default, and permissions can upgrade that to 4, 5, or 6 rows.
- `enabled=true` and `useLuckPerms=true` without LuckPerms installed: automatic fallback to configured `rows`.

## ⚙️ Configuration

Config file: `config/xxlenderchest.json`

```json
{
  "enabled": true,
  "useLuckPerms": false,
  "rows": 6
}
```

| Field | Type | Description |
|-------|------|-------------|
| `enabled` | boolean | When `false`, the mod stays inactive and the ender chest remains vanilla. |
| `useLuckPerms` | boolean | When `true`, XXL Enderchest checks LuckPerms row nodes if LuckPerms is installed. |
| `rows` | integer | Fallback row count from `3` to `6`. Used when LuckPerms mode is off, or when LuckPerms is missing. |

After editing the config, run `/xxlenderchest reload`.

## 🔄 LuckPerms permissions

There is intentionally no `use` permission.
Every player must always be able to open a vanilla 3-row ender chest.

If LuckPerms mode is active, these nodes can increase the available rows:

- `xxlenderchest.rows.4`
- `xxlenderchest.rows.5`
- `xxlenderchest.rows.6`

Highest granted row wins.

### LuckPerms quick start

Example: default users get 4 rows, moderators get 5 rows, VIPs get 6 rows.

```text
/lp group default permission set xxlenderchest.rows.4 true
/lp group moderator permission set xxlenderchest.rows.5 true
/lp group vip permission set xxlenderchest.rows.6 true
```

Example: give one player full 6-row access.

```text
/lp user <player> permission set xxlenderchest.rows.6 true
```

Official LuckPerms docs:

- [LuckPerms Wiki](https://luckperms.net/wiki/Home)
- [LuckPerms Command Usage](https://luckperms.net/wiki/Command-Usage)

## 🔄 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/xxlenderchest info` | OP | Shows mod status, whether LuckPerms is loaded, and whether row access currently uses config mode or LuckPerms mode. |
| `/xxlenderchest reload` | OP | Reloads `config/xxlenderchest.json` without restarting the server. |

## 📦 Installation

### Downloads

| Platform | Link |
|----------|------|
| GitHub | [Releases](https://github.com/SwordfishBE/XXLEnderchest/releases) |
| Modrinth | [XXL Enderchest](https://modrinth.com/mod/Gq8wsb3l) |
| CurseForge | [XXL Enderchest](https://www.curseforge.com/minecraft/mc-mods/xxl-enderchest) |

1. Install [Fabric Loader](https://fabricmc.net/use/installer/).
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Place `xxlenderchest-<version>.jar` in your `mods/` folder.
4. Start the game or server once to generate `config/xxlenderchest.json`.
5. Optional: install LuckPerms as well if you want permission-based row upgrades.

Clients do not need this mod installed when it is used on a server.

## ❗ Important notes

### Lowering row access

If a player had access to more rows before, the extra items remain stored.
They are simply hidden until that player regains enough rows again.

### Removing the mod

Vanilla Minecraft only uses the first 27 ender chest slots.
If you remove XXL Enderchest from an existing world, items stored above the first 3 rows may no longer be accessible.
Make a backup before removing the mod from a world or server.

## 🧱 Building from source

Requires Java 25 and internet access for Gradle dependencies.

```bash
git clone https://github.com/SwordfishBE/XXLEnderchest.git
cd XXLEnderchest

# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

## 🤓 Technical details

- `PlayerEnderChestContainerMixin` keeps the underlying storage at 54 slots.
- `EnderChestBlockMixin` opens the correct chest size for each player.
- `PermissionHelper` detects LuckPerms and resolves row upgrades with fallback behavior.

## 📄 License

Released under the [AGPL-3.0 License](LICENSE).