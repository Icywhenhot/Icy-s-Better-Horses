---
title: Genetics & breeding
nav_order: 11
---

# Genetics & breeding
{: .no_toc }

How foals inherit gender, breed, coat, and stats, and how to breed toward a better horse.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## The four traits

Beyond [bond](ownership-and-bonding), every horse now carries four pieces of identity:

| Trait | What it is |
|:---|:---|
| **Gender** | Male or female. Even donkeys and mules have one. |
| **Breed** | One of the [fifteen breeds](breeds/) for ordinary horses; a species label for donkeys, mules, and skeleton or zombie horses. |
| **Mixed-breed** | A **(mix)** tag on foals that inherited two different breeds. |
| **Coat** | Colour plus markings, drawn from the palette its breed allows. |

All four are visible on the [Horse Info screen](horse-info-screen). Gender and breed also show in the quick overlay when you hold an [Upgraded Saddle](equipment/upgraded-saddle) and look at a horse.

{: .note }
> **Adding the mod to an existing world.** Nothing is deleted and no horse is lost, but every horse **does** change appearance once. The mod reads the vanilla coat it is already wearing, infers the closest matching breed from it, and then gives it one of that breed's own coats. Coats with no clean match are filed as [Mustang](breeds/mustang). Ownership, name, bond, gear, and inventory all carry over untouched; only the colour changes.

---

## Breeding rules

### Gender gate

Two horses of the **same gender won't breed**. Both go into love mode as normal, hearts and all, then pair off with nothing. When they give up you'll see:

> These horses are the same gender and can't breed.

{: .warning }
> **Both apples are still consumed.** Love mode is set the moment you feed, before the mod checks gender, so the hearts are not a promise. Check genders on the [info screen](horse-info-screen) *before* spending golden apples. This is the single most common way to waste them.

The gate only exists while `gender_breeding` is on. Set it to `no` in the [config](configuration) and any two horses pair up, vanilla style.

### What the foal inherits

| Trait | How it's decided |
|:---|:---|
| **Gender** | A coin flip, 50/50 male or female |
| **Breed** | Taken from one parent at random, 50/50 |
| **Coat** | 50/50: either one parent's coat, or a **different** one from its breed's palette |
| **Health, speed, jump** | The average of both parents and a fresh roll from the foal's class range, then clamped to that range |

### Matching vs. mixed pairs

Breed two horses of the **same breed** and the foal inherits that breed cleanly, and its coat comes from that breed's palette. Two [Quarter Horses](breeds/quarter) always give a Quarter-pattern coat.

Breed **different breeds** and the foal takes one parent's breed at random and gets a **(mix)** tag. Its coat comes from whichever breed it landed on, so a mixed pair can surprise you with either side's palette.

### The coat coin flip

Half the time the foal simply wears one of its parents' coats. The other half it comes out in a coat **neither parent wears**, drawn from the rest of its breed's palette. So a matched pair is not a guarantee: two black Shires will often throw a foal that is bay, roan, or something else entirely.

{: .note }
> The only exception is a breed with nothing left to give. [Friesians](breeds/friesian) have just two coats, so if the parents already wear both, the foal takes one of theirs.

Breeding toward a specific coat therefore takes patience. Keep the foals wearing the coat you want and breed those together, and the half that inherits will hold the line while the other half keeps wandering.

Cross-species pairs such as horse with donkey give the matching species placeholder, as in vanilla.

{: .tip }
> Producing a mixed-breed foal earns **Best of Both Worlds**. Any successful breeding earns **Foal Play**. See [Advancements](advancements).

---

## Breeding for better stats

Stat inheritance is the part worth planning around:

```
foal stat = (parent A + parent B + fresh class roll) / 3, clamped to the class range
```

The fresh roll is drawn from the same range the breed's [class](breeds/index) rolls wild horses from, and it leans toward the middle of that range rather than the edges.

Three consequences:

1. **It is an average, not a jackpot.** Pairing your best mare with a poor stallion drags the foal toward the middle. Breed good stock to good stock or you undo your own work.
2. **A foal can beat both parents, but only inside its class range.** The random third of the formula is what lets a line climb, and it is also what makes a foal occasionally come out worse. Expect to cull.
3. **The class sets the ceiling and the floor.** A [Race](breeds/index) horse cannot be bred past Race top speed, and a [Draft](breeds/index) horse cannot be bred below Draft health. Crossing classes moves the foal onto whichever class its breed landed on, ceiling and all.

{: .note }
> Because the fresh roll pulls toward the middle of the class range, a line of already-excellent horses trends **down** slightly if you keep the wrong foals. Only keep the ones that beat both parents, and the average carries the line up.

### A practical breeding loop

1. Scan wild herds holding an [Upgraded Saddle](equipment/upgraded-saddle) and tame the best speed/jump stock you find.
2. Check genders on the [info screen](horse-info-screen). You need one of each.
3. Breed with golden apples. Keep the foal **only** if it beat both parents, and retire the weaker parent.
4. Repeat. Each kept generation nudges the average upward until it presses against the class ceiling.
5. Then [bond](ownership-and-bonding) the best result to 100 for a further **+75%** on top.

{: .note }
> Bonding and breeding stack. Breeding raises the base stat; bonding multiplies it. A maxed Race line at full bond is where **Built Different** (ride at 25 blocks/second) becomes reachable.

---

## Related pages

- [Breeds](breeds/): all fifteen palettes and biome ranges
- [Ownership & bonding](ownership-and-bonding): the multiplier on top of good stats
- [Horse Info screen](horse-info-screen): checking gender and stats before you breed
- [Advancements](advancements): the breeding goals
