---
title: Horse Medkit
parent: Equipment
nav_order: 3
---

# Horse Medkit
{: .no_toc }

A one-shot rescue that fires the instant your horse is in real trouble.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## Crafting

{% include craft.html id="horse_medkit_gear" %}

Expensive, deliberately so. It's insurance, not a consumable you stack.

---

## What it does

The medkit sits in its slot doing nothing until a hit would drop your horse **below half health**. Then it's consumed and immediately grants:

- **Regeneration**
- **Instant Health**
- **Resistance**
- **Fire Resistance**

That combination usually turns a dead horse into a close call. Instant Health stops the bleeding right away, Regeneration keeps topping it up, and Resistance plus Fire Resistance buy time to get clear of whatever caused it.

{: .warning }
> **One use.** Once it fires, the medkit is gone and the slot is empty. Craft a replacement before the next dangerous trip, because an empty medkit slot looks identical to a full one at a glance.

---

## When it's worth it

| Situation | Verdict |
|:---|:---|
| Nether travel | **Yes**, fire resistance alone can save the horse |
| Combat-heavy exploration | **Yes** |
| Long overland trips far from base | **Yes**, losing a 100-bond horse hurts |
| Pottering around a safe base | Skip it |

A horse you've bonded to 100 represents about a hundred minutes of investment plus whatever you spent breeding it. The medkit is cheap next to replacing that.

---

## Disabling

Medkits can be switched off server-side with the `medkit` config toggle. Disabled, the item stays craftable in **dummy mode** as a decorative model but never activates. See [Configuration](../configuration).

---

## Related pages

- [Horse Stabilizer](horse-stabilizer): the other emergency system
- [Ownership & bonding](../ownership-and-bonding): what you're protecting
- [Recipes](../recipes)
