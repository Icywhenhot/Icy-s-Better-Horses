---
title: Managing your horses
nav_order: 6
---

# Managing your horses
{: .no_toc }

The roster screen: every horse you own, in one list, from anywhere in the world.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## Opening the roster

Press <kbd>G</kbd> to open **Your Horses**. It lists every horse registered to you regardless of where it is: following you, parked at home, or standing in a different dimension.

If you haven't tamed anything yet:

> You don't have any horses yet.

Long lists scroll, with an arrow at whichever edge has more rows behind it. Selecting a horse shows a live 3D preview on the right, so you can tell your six chestnut mares apart at a glance.

---

## The preview pane

Under the 3D model, three lines describe the selected horse:

| Line | Meaning |
|:---|:---|
| **X, Y, Z** | Where the horse is standing right now |
| **Bond N** | Current bond out of 100 |
| **Home set** / **No home** | Whether the horse has a saved home location |

### Coordinates

The first line is the horse's block position, so you can write it down and go fetch a mount that's too far away to whistle — or too far away to whistle *across dimensions*.

For a horse loaded near you the readout follows it live as it moves. For a **resting** horse it's the last place it was standing before its chunk unloaded, which is where it will still be when you get there.

{: .note }
> Coordinates are given in the horse's own dimension, shown in the list row next to its gender. A horse resting in the Nether at `120, 68, -340` is at Nether coordinates.

---

## What each list row shows

| Label | Meaning |
|:---|:---|
| **Resting** | The horse is in an unloaded chunk. It's safe and will be restored when called. |
| **Active** | This is the horse your <kbd>P</kbd> whistle calls |
| **Fallen** | The horse died. It can be [summoned back or forgotten](#bringing-a-horse-back) |

{: .note }
> **Resting** isn't a problem. The mod stores owned horses persistently, so a horse in an unloaded chunk still exists and still answers the whistle. It gets restored when you call it.

---

## The four buttons

### Set Active

Marks this horse as the one your [whistle](ownership-and-bonding#whistling-for-your-horse) calls. With several horses to your name, only the active one responds to <kbd>P</kbd>. The button reads **Active** once it's selected.

### Whistle

Calls the horse to you from the screen. Within 32 blocks it walks over; beyond that it teleports, as long as it's inside the server's [whistle range](ownership-and-bonding#whistling-for-your-horse).

### Send Home

Sends the horse back to its saved home without you having to ride it there. If it never had a home set:

> No home set for this horse.

Set one first with **Set Home** on the [command wheel](command-wheel).

### Disown

Releases the horse back into the wild. A confirmation dialog appears, reading *"Are you sure? \<name\> goes back to the wild and forgets you."*, with **Disown** and **Cancel**.

The horse itself is **not deleted**. It stays exactly where it is, untamed, and starts grazing again. What it loses is everything that made it yours: ownership, its bond, its saved home, and its place on your roster.

{: .warning }
> **The bond does not come back.** You can walk up and tame the same horse again, but it starts from bond 0 and has to be re-bonded from scratch. Re-taming a released horse is the long way round, not an undo button.
>
> You can't disown a horse that's still carrying gear: *"Take your equipment off this horse before disowning it."* Strip the saddle and gear first, which also means you don't lose your diamonds by accident.

The [Horse Info screen](horse-info-screen) has the same **Disown** button for the horse you're riding.

---

## Bringing a horse back

A horse that dies doesn't vanish from the roster. It stays in the list, darkened and marked **Fallen**, with two buttons instead of the usual four:

| Button | What it does |
|:---|:---|
| **Summon** | Brings the horse back a couple of blocks in front of you, for **10 XP levels** by default. Free in Creative |
| **Forget** | Removes it from the roster for good, after a confirmation |

The preview shows what the horse will come back with, for example *Bond 60 → 40*: it loses **20 bond** by default, never dropping below 0. Everything else about it is the same horse, with the same name, breed, coat, gender and stats.

{: .warning }
> **Gear doesn't come back.** A horse drops its saddle, armour, chest and everything in it where it dies, the same as vanilla. The summoned horse arrives bare, so go back for the pile.

A horse towing a [cart](equipment/horse-cart) leaves the cart standing where it fell, with its chest and plough still on it.

Both numbers are set by the server with `revive_xp_levels` and `revive_bond_loss`, and `horse_revival` turns the whole thing off, so a dead horse stays dead. See [Configuration](configuration#tuning).
---

## When something doesn't work

| Message | What it means |
|:---|:---|
| *That horse is in another dimension.* | Whistle and Send Home only work within one dimension. Travel to it, or send it home from the same dimension. |
| *That horse's home is in another dimension.* | Send Home can't cross dimensions either. Ride it through the portal yourself. |
| *That horse can't be found any more.* | The horse no longer exists, usually removed by an admin. A horse that died shows as **Fallen** instead. |
| *That horse is too far away to hear your whistle.* | It's outside the server's [whistle range](ownership-and-bonding#whistling-for-your-horse). Get closer. |
| *There is no safe space for this horse nearby.* | Nowhere to put it down. Step out of the cave or off the ledge and retry. |
| *You don't have enough levels to summon this horse.* | [Summoning a fallen horse](#bringing-a-horse-back) costs XP levels. |
| *The horse management screen is turned off* | The server has switched the roster off with `horse_management`. Whistling and the info screen still work. |
| *Build some bond with this horse before it will answer your whistle.* | It's still at bond 0. See [bonding](ownership-and-bonding#the-bond-system). |
| *Unhitch the cart before calling or teleporting this horse.* | A horse towing a [cart](equipment/horse-cart) can't be whistled or sent home. |
| *Take your equipment off this horse before disowning it.* | Remove the saddle and gear first. |

---

## Related pages

- [Ownership & bonding](ownership-and-bonding): whistling and bond
- [Commands](commands): letting other players ride your horses
- [Command wheel](command-wheel): setting a home in the first place
- [Horse Info screen](horse-info-screen): the detailed stat view
- [Advancements](advancements): **Stable Genius** wants ten horses at once

A horse with a cart attached cannot be called or teleported. Unhitch the cart first; otherwise the command shows an error. Recall and home teleports also require safe space for the horse.
