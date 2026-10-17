---
title: Combat
nav_order: 8.5
---

# Combat
{: .no_toc }

A horse at a gallop is a weapon. It also kicks, defends you, and panics when things go badly.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## Running them down

A horse at a full **gallop** in a straight line hits whatever you steer it into, with no swing of your own.

The charge needs about **a second** of straight movement to arm and keeps building for **three**. Turning sharply or stopping resets the wind-up; slower straight movement and jumps keep it building. It only fires on the ground, with a rider aboard, and at a true gallop: free riding, or the **Gallop** [gait](riding#gaits).

### The charge meter

A meter sits beside your hotbar while you gallop and fills as the charge winds up. A full meter means the next thing you touch gets all of it. After a hit it refills over about **five seconds**, and one charge only hits each mob once.

A bond-100 [Percheron](breeds/percheron) skips that wait whenever its charge kills, so it can run straight through a group.

The **Toggle Horse Charge** key (default <kbd>B</kbd>) turns your own charge off and on. On a server it only changes it for you. The meter's position is set in the [HUD config](configuration#hud-placement).

### How hard it lands

The base hit is **7** damage, scaled by the horse's [class](breeds/index):

| Class | Charge and kick | Knockback |
|:---|:---:|:---:|
| War | 1.5x | 1.2x |
| Draft | 1.25x | 2.0x |
| Western | 1.0x | 1.0x |
| Pony | 0.75x | 0.8x |
| Race | 0.6x | 0.8x |

- **Horse armour** adds up to **3** damage on top. A [Clydesdale](breeds/clydesdale) wears it better and gets up to **5**.
- Speed and wind-up scale the whole hit, close to doubling it on a long straight run.
- Nothing lands harder than **30**.

### What it will not hit

- You, and anyone riding with you
- Players you [trust](commands#trusting-a-player)
- Other horses you own
- Anything allied to you or to the horse

With `horse_pvp` off in the [config](configuration), a charge or kick can't hurt **any** player, or any animal a player owns. Horses still run down mobs exactly as before.

---

## Kicks

Come at a horse from behind and it kicks, with the same class multiplier as the charge. Owned or wild, saddled or loose, they all do it.

Its owner, anyone the owner trusts, and whoever is in the saddle never get kicked. After a kick, a horse that isn't already defending someone bolts for about three seconds.

---

## Coming to your aid

When something hurts you, every horse you own within **16 blocks** knows about it.

A loose horse at **bond 40** or better goes for your attacker, chasing it down and running it over. It breaks off if it drops below about a third of its health, or if the fight drags more than **32 blocks** from where it started. Below bond 40 a loose horse won't fight for you.

A horse told to **Stay** stays, the same way a sitting dog stays sat.

### Calling them off

Your **Horse Whistle** key (default <kbd>P</kbd>) breaks off every horse you own that is fighting or already bolting. They don't trot back to you: calling them off sends them running for about **three seconds**, which is the point, since it pulls a horse out from under something far too big for it. Whistle again once they've put some ground between themselves and the fight.

---

## Spooked

The horse under you reacts the other way: when you get hit, it may panic and throw you off.

| Bond | Spook chance |
|:---|:---:|
| 0 to 39 | 10% |
| 40 to 99 | 7% |
| 100 | 5% |

[Draft](breeds/index) horses spook half as often, and a [War](breeds/index) horse never does. A horse won't dump you in midair or over a long drop.

### Storms and blasts

Lightning or a creeper going off within **16 blocks** scares a horse far more than a hit. A green horse always bolts, throwing you if you're riding it. Bond **40** cuts that to **70%**, bond **100** to **50%**.

Draft horses bolt half as often, war horses never, and a horse told to **Stay** holds its ground. This happens even with `horse_spook` turned off.

---

## Turning it off

`horse_combat` in the [config](configuration#combat-toggles) turns all of this off at once. Each part has its own switch too: `horse_charge`, `horse_kick`, `horse_defend`, `horse_spook` and `horse_pvp`.

---

## Related pages

- [Breeds](breeds/): class multipliers and the abilities that change a fight
- [Configuration](configuration#combat-toggles): the combat switches
- [Riding improvements](riding#gaits): getting into a gallop
