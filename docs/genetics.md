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
> **Existing worlds are safe.** Horses you already own aren't forced into a new appearance. The mod reads the coat they're already wearing and infers a matching breed. Coats with no clean match are filed as [Mustang](breeds/mustang).

---

## Breeding rules

### Gender gate

Two horses of the **same gender won't breed**. Feed a golden apple to a horse whose partner matches its gender and you'll see:

> These horses are the same gender and can't breed.

{: .warning }
> **The apple is still consumed** and love mode resets. Check genders on the [info screen](horse-info-screen) before spending golden apples. This is the single most common way to waste them.

### What the foal inherits

| Trait | How it's decided |
|:---|:---|
| **Gender** | A coin flip, 50/50 male or female |
| **Breed** | Taken from one parent at random, 50/50 |
| **Coat** | Re-rolled from the palette its inherited breed allows |
| **Health, speed, jump** | `max(parent1, parent2) + delta`, where delta rolls from **−0.5 to +1.0** |

### Matching vs. mixed pairs

Breed two horses of the **same breed** and the foal inherits that breed cleanly, with a coat rolled from that breed's palette. Two [Quarter Horses](breeds/quarter) always give a Quarter-pattern coat.

Breed **different breeds** and the foal takes one parent's breed at random and gets a **(mix)** tag. Its coat is re-rolled from whichever breed it landed on, so a mixed pair can surprise you with either side's palette.

Cross-species pairs such as horse with donkey give the matching species placeholder, as in vanilla.

{: .tip }
> Producing a mixed-breed foal earns **Best of Both Worlds**. Any successful breeding earns **Foal Play**. See [Advancements](advancements).

---

## Breeding for better stats

Stat inheritance is the part worth planning around:

```
foal stat = max(parent A, parent B) + roll(−0.5 … +1.0)
```

Three consequences:

1. **The foal starts from the better parent, not the average.** Pairing a fast horse with a slow one doesn't drag the result down to the middle. The foal builds on the faster of the two.
2. **Foals can beat both parents.** The delta skews positive (a −0.5 to +1.0 range averages +0.25), so a breeding line trends upward over generations.
3. **There's a hard ceiling.** The vanilla base stat caps the result, so you can't breed runaway superhorses. Expect diminishing returns as a line approaches the cap.

### A practical breeding loop

1. Scan wild herds holding an [Upgraded Saddle](equipment/upgraded-saddle) and tame the best speed/jump stock you find.
2. Check genders on the [info screen](horse-info-screen). You need one of each.
3. Breed with golden apples, keep the foal if it beat its parents, and retire the weaker parent.
4. Repeat. Each generation nudges the line upward until it hits the ceiling.
5. Then [bond](ownership-and-bonding) the best result to 100 for a further **+75%** on top.

{: .note }
> Bonding and breeding stack. Breeding raises the base stat; bonding multiplies it. A maxed line at full bond is where **Built Different** (ride at 25 blocks/second) becomes reachable.

---

## Related pages

- [Breeds](breeds/): all fifteen palettes and biome ranges
- [Ownership & bonding](ownership-and-bonding): the multiplier on top of good stats
- [Horse Info screen](horse-info-screen): checking gender and stats before you breed
- [Advancements](advancements): the breeding goals
