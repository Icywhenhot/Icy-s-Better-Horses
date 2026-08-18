---
title: Configuration
nav_order: 15
---

# Configuration
{: .no_toc }

Six toggles that let a server decide which systems are available on a world.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## The config file

Better Horses writes a JSON file to your config directory on first launch:

```
config/icys-better-horses.json
```

It's created automatically with every option enabled, so you only need to touch it if you want to turn something off.

The default file:

```json
{
  "stabilizer": "yes",
  "medkit": "yes",
  "hitchpost": "yes",
  "hooves": "yes",
  "horse_exclusivity": "yes",
  "multiriding": "yes"
}
```

---

## Options

| Key | Default | What turning it off does |
|:---|:---:|:---|
| `stabilizer` | `yes` | [Horse Stabilizers](equipment/horse-stabilizer) stop deploying, so no gliding descent and no fall protection |
| `medkit` | `yes` | [Horse Medkits](equipment/horse-medkit) never activate |
| `hitchpost` | `yes` | [Hitchposts](equipment/hitchpost) stop tethering horses |
| `hooves` | `yes` | [Horse Hooves](equipment/horse-hooves) lose snow walking, fall reduction, and Frost Walker |
| `horse_exclusivity` | `yes` | **Any player can ride any owned horse**, useful for shared stables |
| `multiriding` | `yes` | Only one player per horse; no second rider |

### Accepted values

Each key is forgiving about format. All of these mean the same thing:

| True | False |
|:---|:---|
| `"yes"`, `"true"`, `"on"`, `"1"`, `"enabled"` | `"no"`, `"false"`, `"off"`, `"0"`, `"disabled"` |
| `true` *(boolean)* | `false` *(boolean)* |
| any non-zero number | `0` |

Unrecognised values fall back to the default and log a warning. A malformed file is replaced with defaults rather than crashing the game.

{: .note }
> Missing keys are filled in and the file is rewritten automatically, so a config from an older version picks up new options without you editing it by hand.

---

## Dummy mode

Disabling a gear item doesn't remove it from the game. Instead it drops into **dummy mode**: still craftable, still equippable, rendered normally, but inert.

That means a server that disables stabilizers doesn't break existing worlds or delete anyone's items, and players can still use the models decoratively in a display stable. Nothing fires in the world.

---

## In-game editing

With [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config) installed, the same options are editable through a settings screen at **Mods → Icy's Better Horses → Configure**, without touching the file. Changes are written straight back to `icys-better-horses.json`.

Both mods are **optional**. Without them, edit the JSON directly.

The settings screen also exposes the three keybinds:

| Setting | Default | Purpose |
|:---|:---:|:---|
| Horse Whistle / Info Key | <kbd>P</kbd> | Whistles your horse; opens the info screen while riding |
| Horse Command Wheel Key | <kbd>R</kbd> | Opens the command wheel on an owned horse |
| Manage Horses Key | <kbd>G</kbd> | Opens the roster of every horse you own |

Keybinds are also rebindable the normal way in **Options → Controls**.

---

## Common setups

**Shared/community server.** Let everyone use every horse:

```json
{ "horse_exclusivity": "no" }
```

{: .tip }
> Before reaching for this, consider leaving exclusivity on and having players run `/horse trust <player>` for the friends they actually ride with. Trusted players get everything except disowning, so ownership itself stays protected. See [Commands](commands).

**Vanilla-flavoured.** Keep the breeds, bonding, and riding fixes but drop the gadgets:

```json
{ "stabilizer": "no", "medkit": "no" }
```

**Solo play.** No reason to change anything. The defaults are the intended experience.

---

## Related pages

- [Equipment](equipment/): what each toggle affects
- [Ownership & bonding](ownership-and-bonding#owner-only-riding): what exclusivity does
- [Riding improvements](riding#two-riders): what multi-riding does
