---
title: Ownership & bonding
nav_order: 3
---

# Ownership & bonding
{: .no_toc }

1. TOC
{:toc}

---

## Claiming a horse

Tame a horse the vanilla way and it becomes **owned** by you the moment taming completes. There's no separate claiming step. Ownership is what switches on the rest of the mod for that horse.

An owned horse is registered to your account permanently. It shows up on your [horse roster](managing-your-horses) whether it's standing next to you, waiting at home, or in another dimension.

## Owner-only riding

Only the owner can take the reins. Another player who tries to mount an owned horse as the primary rider is **bucked off immediately** and sees:

> This horse doesn't trust you enough to ride.

The same protection covers the horse's inventory and its saddle:

| Action by a non-owner | Result |
|:---|:---|
| Mount as primary rider | Bucked off |
| Open the horse's gear screen | *"This horse won't let you access its gear."* |
| Shear off the saddle | *"This horse refuses to let you remove its saddle."* |
| Ride as a **second** rider | Allowed, but only while an allowed rider is already in the saddle |

{: .note }
> Running a shared stable? Set `horse_exclusivity` to `no` in the config and any player can ride any owned horse. See [Configuration](configuration).

### Letting a friend ride

You don't have to choose between "nobody" and "everybody". Trust a specific player and they can ride every horse you own:

```
/horse trust Alex
```

Trust is granted per player, so it covers horses you tame later too, and `/horse untrust` takes it back — bucking them off if they happen to be riding at the time. It's a riding permission only: a trusted friend still can't open your horse's gear, shear its saddle, or disown it. See [Commands](commands) for the full breakdown.

---

## The bond system

Every owned horse tracks a **bond** value from **0 to 100**. Bond is saved with the horse and persists across sessions.

### Gaining bond

| Source | Amount | Notes |
|:---|:---|:---|
| Proximity to the owner | **+1 per minute** | Owner must be within **10 blocks**, in the same dimension |
| Name Tag | **+10** | Once per horse; a second name tag gives nothing |
| Golden Apple | **+2** | Repeatable, and also triggers breeding |

Passive gain ticks once a minute on the server for every owned horse whose owner is nearby, so a horse parked next to your base while you build gains bond the whole time. Going from 0 to 100 on proximity alone takes about **100 minutes**; a name tag and a handful of golden apples cut that down.

{: .warning }
> Bond resets to **0** if the horse loses its owner. Disowning a horse from the roster discards it for good.

### What bond gives you

Bond is banded into five tiers. Every full 20 points adds **+15%** to both movement speed and jump strength, applied as a multiplier on the horse's base stats:

| Bond | Tier | Speed & jump bonus |
|:---:|:---:|:---:|
| 0–19 | 0 | None |
| 20–39 | 1 | +15% |
| 40–59 | 2 | +30% |
| 60–79 | 3 | +45% |
| 80–99 | 4 | +60% |
| 100 | 5 | **+75%** |

Because the bonus is a percentage of the horse's own base stats, a fast horse gains more raw speed from bonding than a slow one. A well-bred [Thoroughbred](breeds/thoroughbred) at 100 bond is dramatically quicker than a bonded draft horse.

The mod's ceilings, shown as full bars on the [Horse Info screen](horse-info-screen):

| Stat | Maximum |
|:---|:---|
| Speed | 25.5 blocks/second |
| Jump | 9.5 blocks |
| Health | 30 HP |

{: .tip }
> Hitting 100 bond earns the **Ride or Die** advancement, and riding at 25 blocks/second earns **Built Different**. See [Advancements](advancements).

---

## Whistling for your horse

Press <kbd>P</kbd> (the Horse Whistle key) and your **active** horse responds. What happens depends on distance:

| Distance | Behaviour |
|:---|:---|
| Within 32 blocks | The horse switches to **Follow** and walks to you |
| Beyond 32 blocks | The horse **teleports** to you |

While you're already mounted, <kbd>P</kbd> opens the [Horse Info screen](horse-info-screen) instead of whistling.

Which horse answers is set from the [roster](managing-your-horses) using **Set Active**. If you own a dozen horses, the active one is the one that comes when you whistle.

---

## Related pages

- [Commands](commands): trusting other players with your horses
- [Command wheel](command-wheel): follow, stay, wander, and home commands
- [Managing your horses](managing-your-horses): the roster screen
- [Horse Info screen](horse-info-screen): reading a horse's stats
- [Genetics & breeding](genetics): passing traits to foals
