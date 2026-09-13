---
title: Riding improvements
nav_order: 8
---

# Riding improvements
{: .no_toc }

The pile of small fixes that make a horse feel like transport instead of a chore.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## Gaits

Press the **walk cycle key** (default <kbd>V</kbd>) while riding to shift up through four gaits. Footfall pattern, stride length and the way the body rocks all change with the gear, so you can see which one you're in without reading the action bar.

| Gear | Gait | Pace | Footfall |
|:---|:---|:---|:---|
| 1 | Walk | ~1.9 blk/s | Four beats, lateral |
| 2 | Trot | ~4.4 blk/s | Two beats, diagonal pairs |
| 3 | Canter | ~6.8 blk/s | Three beats, with a leading foreleg |
| 4 | Gallop | Full speed | Four beats, with a moment of suspension |

Shifting past top gear drops you back to a halt.

{: .note }
> Gears 1 to 3 are **fixed paces**, not a percentage of the horse. A plodding Shire and a bonded Thoroughbred both trot at the same speed, which is the point: it's a cruise control for riding in company or down a narrow path. Only **Gallop** hands the horse its full stat, so a fast horse is only fast in top gear. If the horse's own top speed is below the gear's pace, the gear can't push it past what it has.

Holding <kbd>W</kbd> or <kbd>S</kbd> hands speed control back to you and the horse animates to whatever pace you actually ride at. Let go and the selected gait takes over again.

{: .note }
> The [Icelandic](breeds/icelandic) has no trot. Gear 2 gives you its **tölt** instead -- a four-beat lateral gait that stays smooth in the saddle at a speed where other breeds are bouncing you around. Gears 3 and 4 canter and gallop as normal.

## Auto-ride

**Double-tap <kbd>W</kbd>** while riding to switch on auto-ride. The horse keeps moving forward on its own and you only steer with the mouse. Useful for long overland trips where you'd otherwise be holding <kbd>W</kbd> for ten minutes.

Tap any movement key again to take back full manual control.

## Free look

Mounting no longer snaps your camera to the horse's facing. Instead, **the horse turns to match you**.

While standing still you get up to **90 degrees** of free look before the horse starts to follow your gaze, so you can scan the horizon, check behind you, or line up a shot without dismounting. The moment you press a movement key the horse takes your facing again.

For the other 270 degrees, **hold the Free Look key** (default <kbd>Left Ctrl</kbd>). While it's held the horse ignores your camera completely and keeps its own heading, so you can look straight behind you at a gallop. Let go and it lines back up.

## Rearing

Press the **Rear key** (default <kbd>H</kbd>) to make a horse rear up: the one you're riding, or the one you're looking at within 12 blocks. It needs to be on the ground, and a war horse with its class perk intact refuses to rear when hurt. It's cosmetic, and it's the vanilla rear animation the mod otherwise suppresses so that jumping looks like jumping.

## The horse gets out of your way

Two changes keep the horse from blocking your view:

- **Look down** and the horse fades to nearly invisible, so it doesn't cover the block you're mining or placing.
- **While ridden**, the horse's head drops forward and out of your sightline.

## Step height and mining

- Step height is raised **10%**, so a galloping horse doesn't trip over slabs, dirt paths, or a single block in the way.
- Mounted block-breaking now matches your on-foot speed for most blocks. Where a penalty still applies, it's cut by **80%**.

---

## Terrain handling

### Water

Horses **float instead of sinking**, and they cross water noticeably faster than in vanilla. Rivers and lakes stop being walls.

### Leaves

Leaf blocks **lose collision while you're riding**. No more grinding to a halt in a forest canopy or taking the long way around a jungle. You ride straight through.

### Powder snow

Fit [Horse Hooves](equipment/horse-hooves) and your horse walks on powder snow instead of falling through it. Hooves also soften fall damage, and they accept the **Frost Walker** enchantment for freezing a path across open water.

---

## Two riders

Two players can share one horse. The **first rider keeps the reins** and does the steering; the second gets a back seat along for the trip.

Combined with [owner-only riding](ownership-and-bonding#owner-only-riding), this means a friend can ride with you but can never take your horse out from under you. Servers can disable multi-riding entirely. See [Configuration](configuration).

{: .note }
> Want to carry more than one passenger? A [Horse Cart](equipment/horse-cart) seats **four**: two on the driver's bench and two in the bed.

---

## Creative mode

Using a saddle, vanilla or [upgraded](equipment/upgraded-saddle), on a horse in Creative mode **tames it instantly**, no hearts required. Meant for testing builds and stables without grinding through taming.

---

## Quick reference

| Improvement | Detail |
|:---|:---|
| Gaits | Four gears on <kbd>V</kbd>: walk, trot, canter, gallop |
| Auto-ride | Double-tap <kbd>W</kbd>; any movement key cancels |
| Free look | Up to 90° while stationary; hold <kbd>Left Ctrl</kbd> for full |
| Rear | <kbd>H</kbd> on the horse you ride or look at |
| Camera | Horse fades when you look down; head drops while ridden |
| Step height | +10% |
| Mounted mining | Matches on-foot speed; remaining penalty cut 80% |
| Water | Floats, and moves faster than vanilla |
| Leaves | No collision while riding |
| Powder snow | Walkable with [Horse Hooves](equipment/horse-hooves) |
| Passengers | 2 per horse, owner drives |
| Creative taming | Instant with any saddle |

---

## Related pages

- [Horse carts](equipment/horse-cart): four seats and a double chest
- [Horse Hooves](equipment/horse-hooves): snow walking and Frost Walker
- [Horse Stabilizer](equipment/horse-stabilizer): surviving the drop off a cliff
- [Configuration](configuration): turning multi-riding off
