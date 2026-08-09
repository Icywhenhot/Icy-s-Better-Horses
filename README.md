# Icy's Better Horses

A Fabric mod for Minecraft 26.2 that turns horses from an early-game novelty into long-term companions: ownership and bonding, fifteen real breeds, dedicated tack, carts, and a stack of riding fixes.

**📖 [Read the wiki](https://icywhenhot.github.io/Icy-s-Better-Horses/)** for full documentation of every system, item, breed, and config option.

---

## Features

- **Ownership & bonding**: taming claims a horse as yours. Bond grows 0–100 and adds up to **+75%** speed and jump.
- **Fifteen breeds**: real breeds with their own coat palettes and biome ranges, plus gender, mixed-breed foals, and stat inheritance.
- **Command wheel**: a radial menu to make a horse follow, stay, wander, or return to a saved home.
- **Horse roster**: a screen listing every horse you own, so you can whistle it, send it home, or disown it from anywhere.
- **Upgraded Saddle**: unlocks five gear slots for a chest, hooves, medkit, stabilizer, and hitchpost.
- **Horse carts**: a four-seat cart your horse tows, with an optional double chest of cargo.
- **Riding improvements**: auto-ride, free look, leaf passthrough, water floating, higher step height, and two riders per horse.
- **Stable Handbook**: an in-game Modonomicon guide covering all of it.

Full detail on each: **[the wiki](https://icywhenhot.github.io/Icy-s-Better-Horses/)**.

## Requirements

| | |
|:---|:---|
| Minecraft | 26.2 |
| Loader | Fabric 0.19.3+ |
| Java | 25 or newer |
| Required | Fabric API, GeckoLib 5.5.0+, Modonomicon |
| Optional | Mod Menu + Cloth Config, for the in-game settings screen |

## Configuration

Six toggles in `config/icys-better-horses.json` let a server disable stabilizers, medkits, hitchposts, hooves, owner-only riding, or multi-riding. See [Configuration](https://icywhenhot.github.io/Icy-s-Better-Horses/configuration).

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`. For IDE setup see the [Fabric documentation](https://docs.fabricmc.net/). Art and model notes are in [`docs/horse-stabilizer-model-guide.md`](docs/horse-stabilizer-model-guide.md) and [`docs/horse-ui-art-guide.md`](docs/horse-ui-art-guide.md).

The repository keeps a branch per Minecraft version; active development is on **`26.2-fab`**.

## License

Released under **CC0**. Learn from it, fork it, and reuse pieces of it in your own projects. No attribution required.
