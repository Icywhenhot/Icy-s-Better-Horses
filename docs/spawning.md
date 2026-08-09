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

| Setting | Value |
|:---|:---|
| Spawn weight | **5** |
| Group size | **2–6** horses |
| Minimum creature spawn probability | **0.10** |

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
