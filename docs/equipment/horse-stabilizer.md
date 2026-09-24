---
title: Horse Stabilizer
parent: Equipment
nav_order: 5
---

# Horse Stabilizer
{: .no_toc }

Cliff insurance. Deploys mid-fall, slows the descent, and can cancel the damage entirely.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## Crafting

{% include craft.html id="horse_stabilizer_gear" %}

That works out to 18 copper and 4 gold all in, once you count the canisters.

---

## What it does

During a long fall the stabilizer deploys automatically, slowing the horse's descent. Fall far enough with it open and the fall damage is cancelled completely.

It has **three visible states** you can see on the horse as it works:

| State | What's happening |
|:---|:---|
| **Closed** | Idle. Riding normally. |
| **Half open** | Deploying, with descent slowing to roughly a third of terminal speed |
| **Fully open** | Fully deployed, with descent slowed to about a **tenth** of free fall |

The gliding descent isn't just a damage check, it's an actual change in how fast you come down, so you can watch the ground approach slowly instead of hitting it.

{: .tip }
> Surviving a **30-block drop** with the stabilizer deployed earns the **Pegasus, Sort Of** advancement.

---

## Durability and refuelling

The stabilizer holds about **2 minutes** of gliding (2400 durability, one point per tick). It only wears down while it's **half open** or **fully open**, so riding around with it fitted costs nothing.

While it's deployed, a **fuel gauge** appears beside the left end of your hotbar: the stabilizer icon with a bar that runs from green to red and blinks once it drops below 20%. It stays up for a moment after the stabilizer closes.

When it runs dry it **doesn't break**. It stays on the horse, but won't deploy until you refuel it.

To refuel, put it in an **Anvil** with a [Canister](canister). One canister restores **80%** of its durability.

{: .warning }
> The anvil and a canister are the **only** way to repair it. Combining two stabilizers on an anvil, a grindstone, or in a crafting grid doesn't work.

---

## Fitting it

Two ways:

- **Right-click the horse** while holding the stabilizer. This is the fastest method, as long as the horse wears an [Upgraded Saddle](upgraded-saddle) and the stabilizer slot is empty.
- Open the horse's inventory screen and place it in the stabilizer slot directly.

{: .warning }
> **Horses only.** Mules, donkeys, skeleton horses, and zombie horses can't wear a stabilizer.
>
> The stabilizer also **shares its slot with the [Horse Cart](horse-cart)**. A horse tows a cart or wears a stabilizer, never both.

---

## Stabilizer vs. hooves

They solve different problems and stack well when you can only bring one:

| | [Horse Hooves](horse-hooves) | Horse Stabilizer |
|:---|:---|:---|
| Fall damage | Reduced | Can be cancelled entirely |
| Descent speed | Unchanged | Slowed dramatically |
| Cost | 7 copper, 1 leather | 18 copper, 4 gold, 1 leather |
| Slot | Hooves | Stabilizer (shared with cart) |
| Wear | None | About 2 minutes of gliding, refuelled with canisters |
| Also does | Powder snow, Frost Walker | Nothing else |

Hooves are everyday tack. The stabilizer is for routes where a mistake means a hundred-block drop.

---

## Disabling

Stabilizers can be switched off server-side with the `stabilizer` config toggle. Disabled, the item stays craftable in **dummy mode** as a decorative model but never deploys. See [Configuration](../configuration).

---

## Related pages

- [Canister](canister): the component you need two of, and its fuel
- [Horse Cart](horse-cart): the alternative use for this slot
- [Recipes](../recipes)
