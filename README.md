# Plants & Zombies: Skyblock Wheel

A Plants & Zombies sky island addon: spin a wheel for plant packets, zombies, mystery chests or vanilla loot, survive ten hordes from a fixed spawn block every 5 days, and earn an end portal to fight the dragon.

CurseForge page: <https://www.curseforge.com/minecraft/mc-mods/plants-zombies-skyblock-wheel>

## What you need

- Fabric Loader, Minecraft 26.1.2, Java 25, and **Plants & Zombies (plantz) 1.4 or newer**. Without Plants & Zombies this mod does nothing at all, so install that one first.
- This is a beta mod. If something breaks or a number feels wrong, mail <wiliiowdean@outlook.com> and I will look into it.

## How you start

You spawn on a small grass sky island about 8 by 9 blocks of solid ground. The mod gives you **2 sunflower seed packets** and **10 sun** on first join. Plant the sunflowers on flat ground — they are your income.

## Sun is your money

The sun counter sits on the left side of your screen, next to a sunflower icon. That number is the only currency in the mod, and you never have to pick sun up by hand.

- Sunflowers give **1 sun every 10 seconds**.
- Sun shrooms give **1 sun every 10 seconds** while small, and **2 sun every 20 seconds** once grown up.
- If you kill a plant yourself with your own weapon, half of what it cost to plant comes back automatically.
- Planting a plant costs sun from the same counter, so the loop is: grow sun, plant more, spin the wheel.

## The wheel

Press **N** to open it. One spin costs **10 sun**. There are 20 slices in four colours.

- **RED, 10%** — summons a PvZ zombie that comes after you and your plants.
- **YELLOW, 3%** — a mystery chest. Place it down and open it for a fixed set: enchanted netherite helmet, chestplate, leggings and boots, a bow with Infinity, a netherite sword, 64 arrows, and an elytra with Unbreaking III and Mending. Every enchantment can come from a level 30 enchanting table, and no enchantment is written twice on the same item. Empty the chest and it disappears.
- **GREEN, about 48%** — a PvZ plant seed packet, 1 to 3 of them. The packet already remembers which plant it is.
- **WHITE, about 39%** — one random vanilla item, 1 to 64 of it. Building blocks and food show up more often. Items come plain except enchanted books, which always carry one random enchantment and never a curse.

### The board, and when it changes

The wheel keeps one board per player. It does not re-roll on its own.

- Spin, then close it — the next time you open the wheel you get a freshly rolled board.
- Like what you see? Do not close it. While the screen stays open the board stays the same, so you can spin the same one over and over.
- Open it and close it without spinning — nothing changes.

## Where zombies come from

Everything the mod spawns comes out of the **same fixed block on the island** — both wheel zombies and the horde. On a fresh island that is **X 1, Y 67, Z 1**, the grass layer you stand on. It never moves and is never scattered. If that block is covered, the game searches outward in rings and uses the closest free spot.

## The horde

**Ten waves, one every 5 in-game days.** The first lands on day 5, then day 10, 15, and so on up to day 50. Wave sizes: **3, 7, 10, 15, 24, 30, 38, 48, 55, 64**.

Which zombie you get is random every time, so an early wave can throw a Gargantuar and a late wave can be full of ordinary ones. From wave 4 zombies get speed, from wave 6 strength, and waves 8 and 10 push that further. Zombies attack your plants, not just you, and they can pick a plant on the far side and walk there. Anything knocked off the island is gone for good, and flying zombies that drift off get pulled back. The bottom right of your HUD shows the current day and wave — it turns red while a wave is running, and says the waves are over after the tenth.

## How to win

Clear wave ten and you get **12 end portal frames and 12 eyes of ender**. That is exactly one complete portal with every slot lit, so you can go to the End from your island. If your inventory is full when items are handed to you, they drop on the ground underneath you.

## Feedback

Bug reports and ideas: <wiliiowdean@outlook.com>

Thanks for giving it a try. I made this because I wanted the rush of rolling the wheel while playing PvZ up on a sky island. I am still working on it, and it will keep changing based on feedback.

## License

[MPL-2.0](LICENSE)
