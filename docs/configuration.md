---
title: Configuration
nav_order: 15
---

# Configuration
{: .no_toc }

Nineteen feature toggles, nine tuning numbers, HUD placement, a breeding food list, and a switch for every single breed ability, so a server can decide exactly which systems are live on a world.
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

The default file, abbreviated. The `abilities` block holds one entry for every class perk and every breed ability, all `"yes"` except the two noted below:

```json
{
  "stabilizer": "yes",
  "medkit": "yes",
  "hooves": "yes",
  "horse_exclusivity": "yes",
  "multiriding": "yes",
  "horse_combat": "yes",
  "transparent_horses": "yes",
  "gender_breeding": "yes",
  "cart_pickup": "yes",
  "leaf_passthrough": "yes",
  "hoof_dust": "yes",
  "rear_neigh": "yes",
  "horse_management": "yes",
  "horse_revival": "yes",
  "horse_charge": "yes",
  "horse_kick": "yes",
  "horse_defend": "yes",
  "horse_spook": "yes",
  "horse_pvp": "yes",
  "abilities": {
    "class_abilities": "yes",
    "breed_abilities": "yes",
    "class": {
      "war_steady": "yes",
      "pony_fall": "yes"
    },
    "breed": {
      "shire_brick": "no",
      "belgian_brick": "no",
      "thoroughbred_top_end": "yes"
    }
  },
  "tuning": {
    "bond_per_interval": 1,
    "bond_interval_minutes": 1,
    "spawn_weight": 5,
    "spawn_group_min": 2,
    "spawn_group_max": 6,
    "spawn_probability_floor": 0.1,
    "revive_xp_levels": 10,
    "revive_bond_loss": 20,
    "whistle_range": 0
  },
  "hud": {
    "charge_meter_side": "right",
    "charge_meter_gap": 4,
    "harness_gauge_side": "left",
    "harness_gauge_gap": 4
  },
  "breeding_foods": [
    "minecraft:golden_carrot",
    "minecraft:golden_apple",
    "minecraft:enchanted_golden_apple"
  ]
}
```

---

## Feature toggles

| Key | Default | What turning it off does |
|:---|:---:|:---|
| `stabilizer` | `yes` | [Glidewing Harnesses](equipment/horse-stabilizer) stop deploying, so no gliding descent and no fall protection |
| `medkit` | `yes` | [Horse Medkits](equipment/horse-medkit) never activate, and the medkit slot disappears |
| `hooves` | `yes` | [Horseshoes](equipment/horse-hooves) lose snow walking, fall reduction, and Frost Walker, and the horseshoe slot disappears |
| `horse_exclusivity` | `yes` | **Any player can ride any owned horse**, useful for shared stables |
| `multiriding` | `yes` | Only one player per horse; no second rider |
| `transparent_horses` | `yes` | The horse stops fading out when you look down while riding it |
| `gender_breeding` | `yes` | Gender stops gating breeding, so any two horses can pair. See [Genetics](genetics) |
| `cart_pickup` | `yes` | No [cart](equipment/horse-cart) picks up mobs, whatever its own **Riders** tab says. Players can still ride |
| `leaf_passthrough` | `yes` | Horses and their riders collide with leaves again |
| `hoof_dust` | `yes` | No dust puffs where the hooves land |
| `rear_neigh` | `yes` | The horse rears silently when you use the Rear key |
| `horse_management` | `yes` | The Manage Horses key does nothing and the [roster](managing-your-horses) can't be opened. Whistling and the info screen still work |
| `horse_revival` | `yes` | Dead horses are hidden from the roster and can't be [summoned back](managing-your-horses#bringing-a-horse-back) |

### Combat toggles

`horse_combat` is the master switch. Turn it off and every part of [combat](combat) stops, whatever the five below say.

| Key | Default | What turning it off does |
|:---|:---:|:---|
| `horse_combat` | `yes` | No charging, kicking, owner defence or spooking |
| `horse_charge` | `yes` | A galloping horse no longer bashes what it runs into. The **Toggle Horse Charge** key flips this in game, and on a server it only changes it for you |
| `horse_kick` | `yes` | A horse struck from behind no longer kicks back |
| `horse_defend` | `yes` | Loose horses you own stop running down whatever hurts you |
| `horse_spook` | `yes` | Your horse never throws you when something hits you. Lightning and creepers still spook horses either way |
| `horse_pvp` | `yes` | A charge or kick can no longer hurt **any player**, or **any animal a player owns**. Horses still fight mobs |

{: .tip }
> `horse_pvp` is the one to turn off on a PvE or family server. With it off a charging horse ploughs through zombies exactly as before, but rides straight past other players, their wolves, their cats, and their own horses.

---

## Tuning

Nine numbers under a `tuning` block. These are the values most servers actually want to move.

| Key | Default | What it does |
|:---|:---:|:---|
| `bond_per_interval` | `1` | Bond gained each interval while the owner is within 10 blocks. **`0` turns passive bonding off entirely**, leaving name tags, golden apples and brushing as the only sources |
| `bond_interval_minutes` | `1` | Minutes between each passive gain |
| `spawn_weight` | `5` | How often horses win against other animals in a biome. **`0` stops the mod adding horse spawns at all** |
| `spawn_group_min` | `2` | Fewest horses in a naturally spawned herd |
| `spawn_group_max` | `6` | Most horses in a herd |
| `spawn_probability_floor` | `0.1` | Biomes with no horses and a lower animal spawn chance get raised to this, so horses actually appear |
| `revive_xp_levels` | `10` | XP levels it costs to [summon a dead horse back](managing-your-horses#bringing-a-horse-back). Always free in Creative |
| `revive_bond_loss` | `20` | Bond a horse loses when it comes back. It never drops below 0 |
| `whistle_range` | `0` | How far your [whistle](ownership-and-bonding#whistling-for-your-horse) reaches, in blocks. **`0` means anywhere**; anything else is at least 16 |

The two bond numbers are a pair. The default pair is "+1 per minute", which is 100 minutes from a fresh tame to full bond. To halve that, either double the gain or halve the interval:

```json
{ "tuning": { "bond_per_interval": 2, "bond_interval_minutes": 1 } }
```

{: .note }
> Spawn settings are read when a world loads its biomes, so changing them needs a restart, not a `/reload`.

---

## HUD placement

The `hud` block moves the two gauges that sit beside your hotbar. These are **your own** settings: a server never overrides them.

| Key | Default | What it does |
|:---|:---:|:---|
| `charge_meter_side` | `right` | Which side of the hotbar the [charge meter](combat) sits on, `left` or `right` |
| `charge_meter_gap` | `4` | How far it sits from the edge of the hotbar, 0 to 100 |
| `harness_gauge_side` | `left` | Which side the [Glidewing Harness](equipment/horse-stabilizer) fuel gauge sits on |
| `harness_gauge_gap` | `4` | How far it sits from the edge of the hotbar, 0 to 100 |

Room for the offhand slot is only kept while your offhand actually holds something.

---

## Breeding foods

`breeding_foods` is the list of items that put a tamed adult horse in love. The default is the golden carrot, the golden apple and the enchanted golden apple.

```json
{ "breeding_foods": ["minecraft:golden_carrot", "minecraft:apple"] }
```

Take a food off the list and it still heals and tames as before; it just won't start breeding. Unknown ids are ignored, and the settings screen marks them in red.

---

## Ability toggles

Every [class perk and breed ability](breeds/) has its own switch under the `abilities` block, so you can keep the breeds and disable only the parts you don't want.

| Key | Default | Scope |
|:---|:---:|:---|
| `abilities.class_abilities` | `yes` | Master switch for all five **class** perks. Off means no war-horse steadiness, no draft hauling, no pony step height, and so on |
| `abilities.breed_abilities` | `yes` | Master switch for every **breed** ability. Off means the fifteen breeds keep their stats, coats, and biomes but lose their signature powers |
| `abilities.class.<name>` | `yes` | One class perk, e.g. `war_steady`, `pony_fall`, `draft_haul`, `western_road` |
| `abilities.breed.<name>` | `yes` | One breed ability, e.g. `thoroughbred_top_end`, `percheron_chain`, `appaloosa_herd`, `paint_looting` |

Two are **off by default**, because they let a horse break the world:

| Key | Default | What turning it on does |
|:---|:---:|:---|
| `abilities.breed.shire_brick` | `no` | A bond-100 Shire at a gallop smashes blocks on the `horse_breakable` tag |
| `abilities.breed.belgian_brick` | `no` | The same for a bond-100 Belgian |

The full list of key names is written into the file on first launch, so the easiest way to find one is to open the generated config and read it.

### Accepted values

Each key is forgiving about format. All of these mean the same thing:

| True | False |
|:---|:---|
| `"yes"`, `"true"`, `"on"`, `"1"`, `"enabled"` | `"no"`, `"false"`, `"off"`, `"0"`, `"disabled"` |
| `true` *(boolean)* | `false` *(boolean)* |
| any non-zero number | `0` |

Unrecognised values fall back to the default and log a warning.

{: .note }
> Missing keys are filled in and the file is rewritten automatically, so a config from an older version picks up new options without you editing it by hand.

{: .warning }
> If the file is **malformed**, the mod runs on defaults for that session and **leaves your file untouched**, so nothing you set is lost to a stray comma. It logs a warning telling you so. Fix the JSON and restart to get your settings back; the file is never overwritten out from under you.

---

## On a server

The config is **server-authoritative**. When you join a dedicated server the host's values take over for the session, and the in-game settings screen locks with a note saying so. Your own file is remembered and comes back the moment you disconnect.

That means a server owner only has to edit the config once, on the server, and every player gets the same rules without downloading anything. The two exceptions are the `hud` block, which stays yours, and the charge toggle key, which only flips charging for you.

---

## Turning gear off

Disabling a gear item doesn't remove it from the game. It stays craftable, and nobody's items are deleted.

Gear already on a horse stays there and still shows, it just stops working. New pieces can't be fitted: the medkit and horseshoe slots disappear from the horse screen, and the harness slot only takes a [cart](equipment/horse-cart). Turn the option back on and everything works again.

---

## In-game editing

With [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config) installed, the same options are editable through a settings screen at **Mods → Icy's Better Horses → Configure**, without touching the file. Changes are written straight back to `icys-better-horses.json`.

Both mods are **optional**. Without them, edit the JSON directly.

The screen has seven tabs: **General**, **Horse Combat**, **Tuning**, **Class Abilities**, **Breed Abilities**, **HUD**, and **Keybinds** for all nine keys:

| Setting | Default | Purpose |
|:---|:---:|:---|
| Horse Whistle / Info | <kbd>P</kbd> | Whistles your horse, calls it off a fight, or opens the info screen while riding |
| Horse Command Wheel | <kbd>R</kbd> | Opens the command wheel on a horse you own or are trusted with |
| Manage Horses | <kbd>G</kbd> | Opens the roster of every horse you own |
| Walk Cycle (gear up) | <kbd>V</kbd> | Shifts up through the [gaits](riding#gaits) while riding |
| Walk Cycle (gear down) | <kbd>Z</kbd> | Shifts back down, all the way to a halt |
| Rear Horse | <kbd>H</kbd> | Makes the horse you're riding, or looking at, rear up |
| Free Look | <kbd>Left Ctrl</kbd> | Held: look around freely without the horse turning at all |
| Open Cart | <kbd>Left Alt</kbd> | Opens the [cart](equipment/horse-cart) you're riding or looking at |
| Toggle Horse Charge | <kbd>B</kbd> | Turns your own [charge](combat) on or off |

Keybinds are also rebindable the normal way in **Options → Controls → Icy's Better Horses**.

{: .warning }
> Three defaults collide with vanilla keys: <kbd>P</kbd> is Social Interactions, <kbd>Left Ctrl</kbd> is Sprint, and <kbd>Left Alt</kbd> is commonly bound by other mods. Minecraft marks clashes in red on the controls screen. Rebind whichever side you use more.

---

## Datapack hooks

Some things aren't config keys at all, because a tag or a data file does the job better. All of these work from an ordinary datapack, and all of them are overridable without touching the mod.

### Where a breed lives

Each breed reads its own **biome tag**. This is how you get horses into modded biomes.

```
data/icys-better-horses/tags/worldgen/biome/spawns/<breed>.json
```

```json
{ "replace": false, "values": ["yourmod:redwood_forest"] }
```

With `"replace": false` your entries are added to the mod's own list rather than replacing it. There is also an umbrella tag, `icys-better-horses:spawns_horses`, which decides where horses spawn **at all**; it just includes the fifteen per-breed tags, so adding a biome to any breed tag adds it to both.

See [Spawning](spawning#modded-biomes) for the worked example.

### What a breed is

```
data/icys-better-horses/better_horses/breed/<breed>.json
```

```json
{
  "class": "draft",
  "chest_rows": 4,
  "bonded_chest_rows": 4,
  "spawn_weight": 5
}
```

| Field | What it does |
|:---|:---|
| `class` | Which [class](breeds/index) the breed belongs to: `race`, `war`, `western`, `draft`, or `pony`. Sets its stat range, damage, spook chance, and class perks |
| `chest_rows` | Storage rows with a chest fitted, 0 to 6 |
| `bonded_chest_rows` | Rows once the horse reaches bond 100. Never lower than `chest_rows` |
| `spawn_weight` | How likely this breed is when a horse spawns in a biome it shares with others. A plains horse rolls between the eight plains breeds by weight |

Every field is optional; anything you leave out keeps the mod's value. These reload with `/reload` and are sent to every player on a server, so clients do not need the datapack.

### How fast a class is

```
data/icys-better-horses/better_horses/class/<class>.json
```

```json
{ "min_speed": 7, "max_speed": 10 }
```

The speed range a class rolls from, in **blocks per second**, the same unit the info screen shows. The defaults:

| Class | `min_speed` | `max_speed` |
|:---|:---:|:---:|
| `race` | 11 | 15 |
| `war` | 9 | 15 |
| `western` | 8 | 15 |
| `pony` | 8 | 13 |
| `draft` | 7 | 10 |

Like breed files, these reload with `/reload` and sync to every player.

### Other tags

| Tag | Type | What it controls |
|:---|:---|:---|
| `icys-better-horses:horse_snacks` | item | Extra foods a horse eats to heal, grow and tame faster. Carrots, potatoes, beetroot, melon slices, sweet and glow berries, and sugar cane by default |
| `icys-better-horses:horse_armor` | item | Horse armour that takes armour enchantments at an anvil |
| `icys-better-horses:ploughable` | block | What the cart [plough](equipment/horse-cart#fitting-a-plough) turns into farmland |
| `icys-better-horses:cart_cargo_blocked` | entity type | Mobs that may never ride in a cart bed |
| `icys-better-horses:cart_cargo_allowed` | entity type | Mobs that may ride regardless of size, for anything too wide by default |
| `icys-better-horses:livestock` | entity type | What the [Appaloosa](breeds/appaloosa) herds and feeds |
| `icys-better-horses:blocked_riders` | entity type | Mobs that may never ride a horse |
| `icys-better-horses:self_seated_riders` | entity type | Modded riders that seat themselves, so the mod leaves their position alone |
| `icys-better-horses:horse_breakable` | block | What a bonded Belgian or Shire can smash at a gallop |
| `icys-better-horses:horse_road` | block | What counts as a road for the Western class speed bonus |

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

**Breeds without the powers.** Keep fifteen breeds with their own stats, coats, and biome ranges, but no special abilities:

```json
{ "abilities": { "class_abilities": "no", "breed_abilities": "no" } }
```

**PvE server.** Horses fight mobs but never players or their pets:

```json
{ "horse_pvp": "no" }
```

**Hardcore horses.** A dead horse stays dead:

```json
{ "horse_revival": "no" }
```

**Expensive revival.** Keep it, but make it hurt:

```json
{ "tuning": { "revive_xp_levels": 30, "revive_bond_loss": 50 } }
```

**No long-distance whistle.** Horses only come if they can hear you from 64 blocks:

```json
{ "tuning": { "whistle_range": 64 } }
```

**Faster bonding.** Full bond in 25 minutes instead of 100:

```json
{ "tuning": { "bond_per_interval": 4 } }
```

**Let another mod handle spawning.** Keep everything else, add no horse spawns:

```json
{ "tuning": { "spawn_weight": 0 } }
```

**Solo play.** No reason to change anything. The defaults are the intended experience.

---

## Related pages

- [Equipment](equipment/): what each toggle affects
- [Ownership & bonding](ownership-and-bonding#owner-only-riding): what exclusivity does
- [Riding improvements](riding#two-riders): what multi-riding does
- [Combat](combat): what each combat toggle switches off
