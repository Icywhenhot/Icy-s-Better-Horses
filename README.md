# Icy's Better Horses

A NeoForge mod for Minecraft 26.1.2 that turns horses from an early-game novelty into long-term companions: ownership and bonding, fifteen real breeds, dedicated tack, carts, and a stack of riding fixes.

**📖 [Read the wiki](https://icywhenhot.github.io/Icy-s-Better-Horses/)** for full documentation of every system, item, breed, and config option.

---

## Features

- **Ownership & bonding**: taming claims a horse as yours. Bond grows 0–100 and adds up to **+75%** speed and jump.
- **Fifteen breeds**: real breeds with their own coat palettes and biome ranges, plus gender, mixed-breed foals, and stat inheritance.
- **Command wheel**: a radial menu to make a horse follow, stay, wander, or return to a saved home.
- **Horse roster**: a screen listing every horse you own, so you can whistle it, send it home, or disown it from anywhere.
- **Upgraded Saddle**: unlocks four gear slots for a chest, hooves, medkit, and stabilizer.
- **Horse carts**: a four-seat cart your horse tows, with an optional double chest of cargo.
- **Riding improvements**: auto-ride, free look, leaf passthrough, water floating, higher step height, and two riders per horse.
- **Stable Handbook**: an in-game Modonomicon guide covering all of it.

Full detail on each: **[the wiki](https://icywhenhot.github.io/Icy-s-Better-Horses/)**.

## Requirements

| | |
|:---|:---|
| Minecraft | 26.1.2 |
| Loader | NeoForge 26.1.2.71+ |
| Java | 25 or newer |
| Required | GeckoLib 5.5.1+, Modonomicon 2.2.0+ |
| Optional | Cloth Config 26.1.154+, for the in-game settings screen |

## Configuration

Five toggles in `config/icys-better-horses.json` let a server disable stabilizers, medkits, hooves, owner-only riding, or multi-riding. See [Configuration](https://icywhenhot.github.io/Icy-s-Better-Horses/configuration).

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`. For IDE setup see the [NeoForge documentation](https://docs.neoforged.net/). Art and model notes are in [`docs/horse-stabilizer-model-guide.md`](docs/horse-stabilizer-model-guide.md) and [`docs/horse-ui-art-guide.md`](docs/horse-ui-art-guide.md).

The repository keeps a branch per Minecraft version; this release is maintained on **`26.1.2-neo`**.

## License

All Rights Reserved. See [LICENSE](LICENSE). Play it, pack it into a free modpack, but don't reupload or ship pieces of it elsewhere without asking first.
