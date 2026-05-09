# BazaarFlip Helper Mod
**Fabric 1.21.1 | Client-side only**

A helper mod for Hypixel Skyblock Bazaar flipping.  
It reads prices directly from the Bazaar GUI and alerts you when you are undercut — no automation, fully ToS-safe.

---

## Features
- **HUD overlay** — shows top buy/sell prices and your active order prices while playing
- **Undercut detection** — compares your order price with the best in queue
- **Recommended prices** — tells you exactly what to set (+0.1 for buys, −0.1 for sells)
- **Chat alerts** — periodic in-game messages when you're undercut
- **Queue position** — shows `#1`, `#2`, etc. next to your order

---

## Commands
| Command | Effect |
|---|---|
| `/bflip Sugar` | Start tracking "Sugar" |
| `/bflip` | Stop tracking / toggle off |

---

## How to use
1. Run `/bflip <item>` (e.g. `/bflip Enchanted Sugar`)
2. Open the Bazaar in Hypixel Skyblock (`/bz`)
3. Navigate to your item
4. Click **"Your Orders"** — the mod reads your active order prices from the GUI
5. Go back to the product page — the mod reads the current best prices
6. The HUD (top-left corner) shows whether you're undercut and what price to set
7. **You manually** cancel the order and re-place it at the recommended price

---

## Build Instructions

### Prerequisites
- JDK 21 ([download](https://adoptium.net/))
- Internet connection (Gradle downloads dependencies automatically)

### Steps
```bash
# 1. Clone / extract this folder
cd bazaarflip

# 2. Generate Gradle wrapper (first time only)
gradle wrapper

# 3. Build the mod
./gradlew build

# 4. The .jar is in:
#    build/libs/bazaarflip-1.0.0.jar
```

### Install
Copy `build/libs/bazaarflip-1.0.0.jar` to your `.minecraft/mods/` folder (requires Fabric Loader + Fabric API).

---

## Adjusting the +0.1 increment
In `BazaarTracker.java`, change the `0.1` in `getRecommendedBuyPrice()` and `getRecommendedSellPrice()`.

## Adjusting HUD position
In `HudRenderer.java`, change `int x = PAD;` and `int y = PAD;`.

---

## Notes
- The mod parses lore text from Hypixel's Bazaar GUI. If Hypixel changes their lore format, the regex patterns in `HandledScreenMixin.java` may need updating.
- Alert cooldown is 6 seconds (configurable in `BazaarTracker.java` → `ALERT_COOLDOWN_MS`).
