---
title: Configuration
nav_order: 15
---

# Configuration
{: .no_toc }

Eleven feature toggles, six tuning numbers, and a switch for every single breed ability, so a server can decide exactly which systems are live on a world.
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
  "hitchpost": "yes",
  "hooves": "yes",
  "horse_exclusivity": "yes",
  "multiriding": "yes",
  "horse_combat": "yes",
  "transparent_horses": "yes",
  "gender_breeding": "yes",
  "horse_pvp": "yes",
  "cart_pickup": "yes",
  "tuning": {
    "bond_per_interval": 1,
    "bond_interval_minutes": 1,
    "spawn_weight": 5,
    "spawn_group_min": 2,
    "spawn_group_max": 6,
    "spawn_probability_floor": 0.1
  },
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
  }
}
```

---

## Feature toggles

| Key | Default | What turning it off does |
|:---|:---:|:---|
| `stabilizer` | `yes` | [Horse Stabilizers](equipment/horse-stabilizer) stop deploying, so no gliding descent and no fall protection |
| `medkit` | `yes` | [Horse Medkits](equipment/horse-medkit) never activate |
| `hitchpost` | `yes` | [Hitchposts](equipment/hitchpost) stop tethering horses, and any horse already tied is released |
| `hooves` | `yes` | [Horse Hooves](equipment/horse-hooves) lose snow walking, fall reduction, and Frost Walker |
| `horse_exclusivity` | `yes` | **Any player can ride any owned horse**, useful for shared stables |
| `multiriding` | `yes` | Only one player per horse; no second rider |
| `horse_combat` | `yes` | No charging, kicking, owner defence or spooking. See [Combat](combat) |
| `transparent_horses` | `yes` | The horse stops fading out when you look down while riding it |
| `gender_breeding` | `yes` | Gender stops gating breeding, so any two horses can pair. See [Genetics](genetics) |
| `horse_pvp` | `yes` | A charge or kick can no longer hurt **any player**, or **any animal a player owns**. Horses still fight mobs |
| `cart_pickup` | `yes` | Animals stop climbing into a [cart](equipment/horse-cart) bed on their own. Players can still ride it |

{: .tip }
> `horse_pvp` is the one to turn off on a PvE or family server. With it off a charging horse ploughs through zombies exactly as before, but rides straight past other players, their wolves, their cats, and their own horses.

---

## Tuning

Six numbers under a `tuning` block. These are the values most servers actually want to move.

| Key | Default | What it does |
|:---|:---:|:---|
| `bond_per_interval` | `1` | Bond gained each interval while the owner is within 10 blocks. **`0` turns passive bonding off entirely**, leaving name tags and golden apples as the only sources |
| `bond_interval_minutes` | `1` | Minutes between each passive gain |
| `spawn_weight` | `5` | How often horses win against other animals in a biome. **`0` stops the mod adding horse spawns at all** |
| `spawn_group_min` | `2` | Fewest horses in a naturally spawned herd |
| `spawn_group_max` | `6` | Most horses in a herd |
| `spawn_probability_floor` | `0.1` | Biomes with no horses and a lower animal spawn chance get raised to this, so horses actually appear |

The two bond numbers are a pair. The default pair is "+1 per minute", which is 100 minutes from a fresh tame to full bond. To halve that, either double the gain or halve the interval:

```json
{ "tuning": { "bond_per_interval": 2, "bond_interval_minutes": 1 } }
```

{: .note }
> Spawn settings are read when a world loads its biomes, so changing them needs a restart, not a `/reload`.

---

## Ability toggles

Every [class perk and breed ability](breeds/) has its own switch under the `abilities` block, so you can keep the breeds and disable only the parts you don't want.

| Key | Default | Scope |
|:---|:---:|:---|
| `abilities.class_abilities` | `yes` | Master switch for all five **class** perks. Off means no war-horse steadiness, no draft hauling, no pony step height, and so on |
| `abilities.breed_abilities` | `yes` | Master switch for every **breed** ability. Off means the fifteen breeds keep their stats, coats, and biomes but lose their signature powers |
| `abilities.class.<name>` | `yes` | One class perk, e.g. `war_steady`, `pony_fall`, `draft_haul`, `western_road` |
| `abilities.breed.<name>` | `yes` | One breed ability, e.g. `thoroughbred_top_end`, `percheron_chain`, `appaloosa_herd` |

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

That means a server owner only has to edit the config once, on the server, and every player gets the same rules without downloading anything.

---

## Dummy mode

Disabling a gear item doesn't remove it from the game. Instead it drops into **dummy mode**: still craftable, still equippable, rendered normally, but inert.

That means a server that disables stabilizers doesn't break existing worlds or delete anyone's items, and players can still use the models decoratively in a display stable. Nothing fires in the world.

---

## In-game editing

With [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config) installed, the same options are editable through a settings screen at **Mods → Icy's Better Horses → Configure**, without touching the file. Changes are written straight back to `icys-better-horses.json`.

Both mods are **optional**. Without them, edit the JSON directly.

The screen has four tabs: **General** for the nine feature toggles, **Class abilities** and **Breed abilities** for the per-ability switches grouped by class and by breed, and **Keybinds** for all seven keys:

| Setting | Default | Purpose |
|:---|:---:|:---|
| Horse Whistle / Info | <kbd>P</kbd> | Whistles your horse, calls it off a fight, or opens the info screen while riding |
| Horse Command Wheel | <kbd>R</kbd> | Opens the command wheel on a horse you own or are trusted with |
| Manage Horses | <kbd>G</kbd> | Opens the roster of every horse you own |
| Walk Cycle | <kbd>V</kbd> | Shifts up through the [gaits](riding#gaits) while riding |
| Rear | <kbd>H</kbd> | Makes the horse you're riding, or looking at, rear up |
| Free Look | <kbd>Left Ctrl</kbd> | Held: look around freely without the horse turning at all |
| Cart Size | <kbd>Left Alt</kbd> | Swaps a [cart](equipment/horse-cart) between the small cart and the large wagon |

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

### Other tags

| Tag | Type | What it controls |
|:---|:---|:---|
| `icys-better-horses:ploughable` | block | What the cart [plough](equipment/horse-cart#fitting-a-plough) turns into farmland |
| `icys-better-horses:cart_cargo_blocked` | entity type | Mobs that may never ride in a cart bed |
| `icys-better-horses:cart_cargo_allowed` | entity type | Mobs that may ride regardless of size, for anything too wide by default |
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
