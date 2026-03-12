# XXL Enderchest

A Fabric mod for **Minecraft Java Edition** that expands your ender chest from the vanilla 3 rows (27 slots) to up to 6 rows (54 slots) — giving you up to 2× the storage without any extra blocks.

---

## Features

| Rows | Slots | vs. Vanilla |
|------|-------|-------------|
| 3    | 27    | Vanilla (mod disabled or `rows: 3`) |
| 4    | 36    | +33% |
| 5    | 45    | +67% |
| **6**| **54**| **+100% (default)** |

- Per-server configuration via a simple JSON file
- Hot-reloadable config — no restart required
- Items stored in higher rows are **never lost** when the row count is reduced; they are simply inaccessible until rows are increased again
- Works on **dedicated servers** and in **single-player**

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/).
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your `mods/` folder
3. Place `xxlenderchest-<version>.jar` in your `mods/` folder
4. Start the game — a default config is created automatically at `config/xxlenderchest.json`

---

## Configuration

The config file is located at:
```
<game or server directory>/config/xxlenderchest.json
```

Default contents:
```json
{
  "enabled": true,
  "rows": 6
}
```

| Field     | Type    | Values       | Description |
|-----------|---------|--------------|-------------|
| `enabled` | boolean | `true`/`false` | When `false`, the ender chest behaves like vanilla (3 rows). |
| `rows`    | integer | `3`–`6`      | Number of rows to show in the ender chest GUI. Values outside this range are clamped automatically. |

> **Tip:** After editing the file, use `/xxlenderchest reload` to apply changes without restarting the server.

---

## Commands

| Command                   | Permission | Description |
|---------------------------|------------|-------------|
| `/xxlenderchest info`     | All players | Shows whether the mod is enabled and how many rows are active. |
| `/xxlenderchest reload`   | OP (level 2+) | Reloads `xxlenderchest.json` from disk and applies the new settings immediately. |

---

## Important Notes

### Reducing the row count
When you reduce `rows` (e.g. from 6 to 4), items stored in the now-hidden rows **remain saved** in the player's NBT data — they are not deleted. They simply become inaccessible until `rows` is increased again. This is by design to prevent accidental item loss.

### Removing the mod
If you remove XXL Enderchest from an existing world, Minecraft will only load the first 27 slots of each player's ender chest. Items stored in slots 28–54 **will not be loaded** by vanilla Minecraft and may be lost. **Always back up your world before removing this mod.**

### Multiplayer / servers
The mod must be installed **server-side**. The config only needs to be on the server. Players do not need to install the mod on their client.

---

## Building from source

Requires Java 21 and an internet connection (to download Gradle and dependencies).

```bash
git clone https://github.com/locutus/xxlenderchest.git
cd xxlenderchest

# On Linux/macOS:
./gradlew build

# On Windows:
gradlew.bat build
```

## Technical details

The mod uses two Mixins:

- **`PlayerEnderChestContainerMixin`** — changes the ender chest container size from 27 to 54 slots at instantiation time, ensuring all item data is always preserved in NBT.
- **`EnderChestBlockMixin`** — intercepts the block interaction (`useWithoutItem`) and opens a `ChestMenu` with the configured number of rows instead of the vanilla 3-row menu.

---

## License

AGPL-3.0 — see [LICENSE](LICENSE) for details.
