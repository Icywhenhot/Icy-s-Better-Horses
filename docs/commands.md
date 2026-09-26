---
title: Commands
nav_order: 7
---

# Commands
{: .no_toc }

Trusting other players with your horses, looking up coats, and the operator tools for editing and spawning horses.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## The `/horse` command

Everything the mod adds to chat lives under `/horse`. The commands split into two groups, and the split is about who they can touch.

Anyone can use these. The trust commands only ever affect your own horses, and `coats` just reads:

| Command | What it does |
|:---|:---|
| `/horse trust <player>` | Lets that player ride and handle every horse you own |
| `/horse untrust <player>` | Takes the permission back |
| `/horse trusted` | Lists everyone you currently trust |
| `/horse coats [breed]` | Lists the coats a breed can have |

The trust commands are player-only. Running them from the server console or a command block does nothing useful, because there's no owner to attach the list to.

These need cheats on in singleplayer, or op level 2 on a server. They work on any breed horse, owned or not, so hand them out accordingly:

| Command | What it does |
|:---|:---|
| `/horse set [targets] <property> <value>` | Changes the coat, gender, speed, jump, health or bond of an existing horse |
| `/horse spawn <breed> [options]` | Spawns a new horse, with as much or as little of it picked as you like |

Neither one touches vanilla horses, donkeys or mules. They're for the mod's breeds only.

---

## Trusting a player

```
/horse trust Alex
```

> Alex can now ride your horses.

Alex, if they're online, sees:

> \<your name\> trusts you with their horses. You can ride them and handle their gear now.

Trust is stored **per player, not per horse**. One `/horse trust` covers every horse you own now *and* every horse you tame later — you never have to re-run it when you claim a new mount.

The list is saved with the world, so it survives restarts. It's also symmetric-free: Alex trusting you does nothing for Alex's horses unless you run the command too.

{: .note }
> Tab completion suggests players who are online. Offline players work too, as long as they've joined the server before, because the name resolves against the server's profile cache. If a trusted player later changes their name, they stay trusted — the list keys on their account, not their username.

---

## What trust actually allows

A trusted player is treated as you are, with **one** exception: they can't give your horse away.

| Action by a trusted player | Allowed? |
|:---|:---|
| Mount the horse and ride it | **Yes** |
| Ride as a second rider | **Yes** |
| Take the reins of a [horse cart](equipment/horse-cart) | **Yes** |
| Open the horse's gear screen | **Yes** |
| Use its saddlebags and a cart's chest | **Yes** |
| Fit a cart or stabilizer by hand | **Yes** |
| Shear off the saddle | **Yes** |
| Use the [command wheel](command-wheel) on it | **Yes** |
| Disown it | No |

Disowning stays with the owner. Everything else — the gear screen, the storage hanging off it, the commands — is open, because a riding companion who can't open the saddlebags on a trip isn't much of one.

{: .warning }
> This is a lot of permission. A trusted rider can ride your horse **away**, empty its chest, and strip its armour, and while they're in the saddle you can't whistle it back out from under them. Trust people you'd hand a lead to.

---

## Revoking trust

```
/horse untrust Alex
```

> Alex can no longer ride your horses.

If Alex is **in the saddle** when you revoke, the horse bucks them off on the next tick — the same check that stops strangers mounting runs continuously, not just at mount time.

To see where you stand:

```
/horse trusted
```

> Trusted with your horses (2): Alex, Steve

Tab completion on `/horse untrust` suggests only the players already on your list, so you don't have to remember the spelling.

---

## Trust and the exclusivity config

Trust is only meaningful while owner-only riding is switched on. With `horse_exclusivity` set to `no`, anyone can ride anything and the trust list has nothing left to do — it's kept, just ignored, and starts mattering again the moment the setting goes back on. See [Configuration](configuration).

---

## Looking up coats

```
/horse coats thoroughbred
```

> Thoroughbred coats (7): brown, dark_brown, red, blood_bay, jet_black, chestnut_chrome, steel_grey

Leave the breed off and you get the coats of the horse you're riding, or the one you're standing next to. It needs no cheats, so it's also the quickest way for a player to find out what a breed can come in.

The ids in that list are exactly what `/horse set coat` and `coat=` expect. If you'd rather see the coats than read them, the Stable Handbook shows each breed's coats on a live model.

---

## Editing a horse

```
/horse set coat blood_bay
```

> Thoroughbred Horse now has the Blood Bay coat.

`/horse set` changes one thing about a horse that already exists. With no target it picks the horse for you, in this order:

1. the horse you're riding
2. the horse under your crosshair, up to 6 blocks away
3. the nearest breed horse within 8 blocks

In practice that means you walk up to the horse, look at it, and type. You only need a target when you want one particular horse out of a crowd, or a lot of horses at once. Put the selector in front of the property:

```
/horse set @e[type=icys-better-horses:shire_horse,distance=..20] coat grey
```

Every breed horse the selector catches gets the change, and anything else it catches is ignored (players, cows, vanilla horses). If a horse's breed has no coat by that name, that horse is skipped and chat names it, so a mixed batch never half-fails without telling you.

{: .note }
> Entity ids use a hyphen: `icys-better-horses:<breed>_horse`, as in `icys-better-horses:american_paint_horse`. The breed names inside the commands themselves don't need the prefix at all.

### What you can set

| Property | Value | Example |
|:---|:---|:---|
| `coat` | One of that breed's coat ids, or `random` | `/horse set coat jet_black` |
| `gender` | `male` or `female` | `/horse set gender female` |
| `speed` | Blocks per second | `/horse set speed 14.5` |
| `jump` | Blocks | `/horse set jump 4` |
| `health` | Hit points, two per heart | `/horse set health 20` |
| `bond` | 0 to 100 | `/horse set bond 100` |

Tab after `coat` only offers the coats of the horse you're aiming at, not every coat in the mod. Setting `health` also heals the horse up to its new maximum.

### Stats stay inside the breed's range

Speed, jump and health snap to the range of the horse's class. Ask for a 20 blk/s Thoroughbred and you get 15, plus a yellow line saying so:

> Thoroughbred Horse speed clamped to 15.0 blk/s (breed range 11.0 blk/s to 15.0 blk/s).

That's on purpose. A draft horse that outruns a racehorse would make the classes pointless, so the best horse you can make is one at the top of its range. Press Tab on a stat and it suggests the bottom and top for that horse, which beats looking them up here:

| Class | Speed (blk/s) | Jump (blocks) | Health (HP) |
|:---|:---|:---|:---|
| Race | 11.0 to 15.0 | 3.3 to 5.1 | 15 to 20 |
| War | 9.0 to 15.0 | 2.4 to 4.8 | 25 to 40 |
| Western | 8.0 to 15.0 | 2.4 to 4.0 | 15 to 30 |
| Pony | 8.0 to 13.0 | 2.4 to 4.0 | 25 to 40 |
| Draft | 7.0 to 10.0 | 1.3 to 3.3 | 35 to 50 |

Which class a breed belongs to is on its [breed page](breeds/). Breeds from addons use whatever their own class says.

The units match the horse's inventory and the [info screen](horse-info-screen). They're base stats, though. Bond still adds its bonus on top, so a 15 blk/s horse at full bond reads higher than 15 on the info screen.

---

## Spawning a horse

```
/horse spawn thoroughbred
```

This puts a new horse of that breed at your feet, rolled the same way a wild one would be. Everything after the breed is optional. Options go in as `key=value`, in any order, and anything you leave out is rolled as normal:

```
/horse spawn arabian coat=wild_bay gender=female speed=15 jump=4.5 health=20 bond=100 owner=Steve
```

> Spawned a new Arabian: Wild Bay, Female, speed 15.0 blk/s, jump 4.5 blk, health 20.0 HP.

| Option | Value | If you leave it out |
|:---|:---|:---|
| `coat` | A coat id of that breed, or `random` | Random coat |
| `gender` | `male`, `female` or `random` | Random |
| `speed`, `jump`, `health` | Same units and limits as `/horse set` | Rolled like a wild horse |
| `bond` | 0 to 100 | 0 |
| `baby` | `true` or `false` | An adult |
| `owner` | The name of an online player | Wild and untamed |

`owner=` tames the horse to that player on the spot. It lands on their [roster](managing-your-horses) and follows the usual owner-only riding rules. The one thing it skips is the taming advancement, since nobody actually broke it in. The player has to be online when you run the command.

Tab completion works the whole way along the line: breed names first, then option names, then values for whichever option you're typing. That's the breed's coats for `coat=`, the class range for the stats, and online players for `owner=`.

A typo stops the spawn before anything appears, and chat says what was wrong. A bad coat gets the list of real ones, so you can fix it without running `/horse coats` first.

The horse appears wherever the command runs. From a command block or the console, that's the block's position or the world spawn, and `/execute positioned` moves it anywhere else:

```
/execute positioned 120 70 -45 run horse spawn shire coat=black_feather
```

---

## Related pages

- [Breeds](breeds/): every breed, its class and its ability
- [Ownership & bonding](ownership-and-bonding): what owner-only riding blocks
- [Managing your horses](managing-your-horses): the roster screen
- [Configuration](configuration): the `horse_exclusivity` toggle
