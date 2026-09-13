---
title: Spawning
nav_order: 12
---

# Spawning
{: .no_toc }

Horses now live almost everywhere: mountains, taigas, deserts, snowfields, badlands.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## What changed

In vanilla, horses spawn in exactly five biomes:

- Plains
- Sunflower Plains
- Savanna
- Savanna Plateau
- Windswept Savanna

Better Horses adds horse spawns to **every biome that any of its [fifteen breeds](breeds/) calls home**, roughly thirty biomes in total, spanning deserts, badlands, taigas, forests, meadows, snowfields, and mountain peaks.

The practical effect: you find horses where you actually are, instead of having to go looking for a plains biome. And because each breed has its own range, *where* you explore decides *what* you find. The [breeds index](breeds/#which-breed-lives-where) has a biome-to-breed lookup table.

---

## Spawn parameters

| Setting | Default | Config key |
|:---|:---|:---|
| Spawn weight | **5** | `tuning.spawn_weight` |
| Group size | **2–6** horses | `tuning.spawn_group_min` / `_max` |
| Minimum creature spawn probability | **0.10** | `tuning.spawn_probability_floor` |

All three are editable. Setting the weight to `0` stops the mod adding horse spawns entirely, which is what you want if another mod in the pack is managing animal spawning. See [Configuration](configuration#tuning).

Two rules keep this from being intrusive:

1. **Biomes that already have horses are left alone.** If a biome spawns horses in vanilla, or another mod already added them, Better Horses doesn't stack a second entry on top. No double spawn rates in the plains.
2. **The probability floor only applies where it's needed.** If a biome had no horses *and* a creature spawn probability below 0.10, it's raised to 0.10 so horses actually appear. Biomes already above that keep their own value.

---

## Where horses can spawn

A horse needs light level **above 8**, so daylight or a well-lit area, same as vanilla animals, plus one of these blocks underneath:

| Category | Blocks |
|:---|:---|
| Standard | Anything in the `animals_spawnable_on` tag (grass and friends) |
| Ground | Dirt, sand, gravel, stone, terracotta |
| Cold | Snow, snow blocks, powder snow, ice, packed ice |

That block list is what makes mountain and snow spawning work. Vanilla horses need grass; a Better Horses [Icelandic](breeds/icelandic) can spawn on packed ice in an Ice Spikes biome, and a [Haflinger](breeds/haflinger) on the stone of a Jagged Peak.

---

## Modded biomes

Short version: **out of the box, no.** The mod ships a biome list for each breed, and every entry on it is a vanilla biome. Drop the mod into a pack built on Biomes O' Plenty, Terralith, or anything else that replaces the overworld, and horses keep spawning only in whatever vanilla biomes survive.

The fix is a datapack, and it is two lines. Each breed reads a **biome tag**, so adding a modded biome to that tag is all it takes:

```
data/icys-better-horses/tags/worldgen/biome/spawns/mustang.json
```

```json
{
  "replace": false,
  "values": [
    "biomesoplenty:prairie",
    "terralith:yellowstone"
  ]
}
```

`"replace": false` matters. It adds your biomes to the mod's own list instead of wiping it, so vanilla plains still work.

There are fifteen of these tags, one per breed, named after the breed: `spawns/thoroughbred`, `spawns/icelandic`, `spawns/shire`, and so on. Put a biome in one tag and that breed can spawn there. Put the same biome in several tags and the game rolls between them, weighted by each breed's `spawn_weight`.

{: .tip }
> Match the breed to the biome rather than dumping every breed into every modded biome. An [Icelandic](breeds/icelandic) belongs in a modded snowfield, a [Shire](breeds/shire) in a modded old-growth wood. The whole point of the biome ranges is that where you explore decides what you find.

You do not need a separate "turn horses on here" step. The umbrella tag `icys-better-horses:spawns_horses` just includes the fifteen breed tags, so a biome listed in any one of them is automatically a horse biome.

Everything else about a breed is a datapack file too, including which class it belongs to and how big its chest is. See [datapack hooks](configuration#datapack-hooks).

---

## Finding a specific breed

1. Look up the breed on its page, or use the [biome lookup table](breeds/#which-breed-lives-where).
2. Travel to one of its biomes.
3. Hold an [Upgraded Saddle](equipment/upgraded-saddle) and look at each horse to read its breed off the overlay before taming.

Some breeds are much easier than others. [Plains](breeds/) alone hosts eight breeds, so a plains herd is a lottery. Single-biome breeds are more predictable: an [Arabian](breeds/arabian) in the desert, a [Friesian](breeds/friesian) in a dark forest, an [Icelandic](breeds/icelandic) in ice spikes.

{: .tip }
> Chasing **Gotta Tame 'Em All** (own all fifteen breeds)? Work through the biome table rather than wandering. [Cherry Grove](breeds/andalusian), [Ice Spikes](breeds/icelandic), and [Eroded Badlands](breeds/arabian) each host exactly one breed and are the easiest guaranteed finds.

---

## Related pages

- [Breeds](breeds/): all fifteen, with biome ranges
- [Genetics & breeding](genetics): breeding what you catch
- [Advancements](advancements): **Gotta Tame 'Em All**
