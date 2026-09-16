# Non Stop Quests

**Always something to do, always something worth doing it for.**
**Non Stop Quests** is a full quest board for Minecraft — Daily, Weekly, and Monthly objectives running side by side, plus a permanent set of Mystery quests, one for every rarity tier. Finish one and it's replaced immediately, so there's never a moment where the board goes quiet.
**Made by M8use with Claude, and some coding help from  mimikyumochi.**

It's built to sit quietly in your inventory until you need it: one button, one quest log, zero clutter, no config required. **Made with Claude mainly by M8use, with mimikyu helping out on some of the coding.**

**Available on NeoForge (1.21.1)**

---

## What You Get

- **A quest log in your pocket.** A small button lives right in your inventory screen. Click it any time to open the log — no commands to remember.
- **Four boards running at once.** 6 Daily slots, 3 Weekly slots, 4 Monthly slots, and 6 Mystery slots (one per rarity) — all active simultaneously, all tracked independently.
- **Genuinely varied objectives.** Mining, building, fishing, farming, brewing, trading, enchanting, taming, traveling, and more — over 200 unique quest definitions across the four boards, so it never feels like the same three quests reskinned.
- **Instant replacement.** Claim a Daily, Weekly, or Monthly quest and a brand new one — freshly rolled, correct rarity odds, no immediate repeats — takes its slot right away. The board is never sitting there half-empty.
- **Six rarities that actually mean something.** Common through Mythic, each with its own color, glow, and reward scale — right up to a Mythic-tier quest that asks you to kill 1,000 pigs for a Netherite Block, with its name rendered in a permanent moving rainbow to match the absurdity.
- **An info button on every quest.** Not sure exactly what counts? Tap the small "i" next to any Daily, Weekly, or Monthly quest for a plain-language breakdown of the objective, what does and doesn't count toward it, and the exact reward — generated straight from the quest's own definition, so it's never out of date.
- **Mystery Boxes with a real collection to manage.** Boxes you earn go into a proper collection screen, sorted by rarity, where you can open one at a time or in bulk, or upgrade ten of one rarity into one of the next tier up.

## ✨ Presentation

We didn't want this to feel like a checklist:

- A rotating sunray effect on the inventory button lets you know at a glance when something's claimable — gone the instant everything's picked up
- Every quest card gets an animated, per-rarity gradient background, with Mythic getting the full moving-rainbow treatment on both the card and its text
- Opening a Mystery Box is a real event: a horizontal roulette of possible rewards, accelerating and decelerating like a proper slot machine, complete with a rarity-synced tick sound that speeds and slows with the wheel
- The winning item falls and grows into an oversized reveal card with its own sunburst, landing with a satisfying bounce, its quantity staying attached to it the whole way down
- Confetti and a success chime on every win — bigger for the rarer pulls
- A live countdown to your next Daily/Weekly/Monthly reset sits right next to the inventory button, pulsing gently in your category's color

![Quests Menu](https://cdn.modrinth.com/data/cached_images/c6b06aed9a9da1eb6631e9342f4d04a86849d795.png)

## 🔒 Fair By Design

Everything that matters is decided and checked on the server, never the client:

- Quest generation, progress tracking, and reward rolls all happen server-side — the client only ever displays what the server has already decided
- A Mystery Box's contents aren't added to your inventory until its reveal animation actually finishes landing — so there's no peeking at your hotbar to spoil the surprise mid-spin
- Block-breaking, item-collection, and travel-distance quests all have safeguards against the obvious exploits (placing and re-breaking your own blocks, farming the same biome twice, etc.)

## 🧪 For Server Admins

A small set of testing commands, gated behind the same permission level as `/give` or `/gamemode` (so only ops or cheats-enabled worlds can use them):

- `/questsmod claim <daily|weekly|monthly> <slot>` — instantly complete and claim a specific slot, replacement included
- `/questsmod mystery complete <rarity>` — instantly complete the active Mystery quest for a given rarity
- `/questsmod boxes <rarity> <amount>` — add any number of Mystery Boxes of a given rarity directly to your collection, no vanilla item spawned

## 📥 Installation Guide

1. Install NeoForge for Minecraft 1.21.1.
2. Drop the Non Stop Quests `.jar` into your `mods` folder.
3. Launch the game. That's it — nothing to configure.

Open the inventory button any time to see your current Daily, Weekly, Monthly, and Mystery quests.

## ⚙️ Compatibility

Non Stop Quests is fully standalone — it has no dependency on any other mod and doesn't require anything else installed. Its inventory button also detects other compatible inventory-button mods at runtime and automatically stacks beneath them in the correct order, so it never overlaps or floats awkwardly whether you're running it alone or alongside others.

- **Mod loader:** NeoForge
- **Minecraft version:** 1.21.1
- **Client & Server:** required on both — this is a server-authoritative system by design

## 🐛 How to Report Issues

Found a bug, a visual glitch, or a quest that feels off? [Add your issue tracker / Discord / contact link here] — please include your Minecraft and NeoForge version, and a screenshot or log file if you can.
