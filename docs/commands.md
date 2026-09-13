---
title: Commands
nav_order: 7
---

# Commands
{: .no_toc }

Trusting other players with your horses, from chat.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## The `/horse` command

Everything the mod adds to chat lives under one root command. No operator permission is needed — every command here only ever touches your *own* horses.

| Command | What it does |
|:---|:---|
| `/horse trust <player>` | Lets that player ride and handle every horse you own |
| `/horse untrust <player>` | Takes the permission back |
| `/horse trusted` | Lists everyone you currently trust |

All three are player-only. Running them from the server console or a command block does nothing useful, because there's no owner to attach the list to.

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
| Tie it to a [hitchpost](equipment/hitchpost) | **Yes** |
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

## Related pages

- [Ownership & bonding](ownership-and-bonding): what owner-only riding blocks
- [Managing your horses](managing-your-horses): the roster screen
- [Configuration](configuration): the `horse_exclusivity` toggle
