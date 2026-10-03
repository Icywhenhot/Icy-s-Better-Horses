# Icy's Better Horses

A Fabric mod for Minecraft 1.21.1 that turns horses from an early-game novelty into long-term companions: ownership and bonding, fifteen real breeds, dedicated tack, carts, and a stack of riding improvements.

**📖 [Read the wiki](https://icywhenhot.github.io/Icy-s-Better-Horses/)** for full documentation of the mod's systems, items, breeds, and configuration.

---

## Features

- **Ownership & bonding**: taming claims a horse as yours, with bond progression that improves its performance.
- **Fifteen breeds**: breed-specific coats, biome spawning, gender, mixed-breed foals, and inherited stats.
- **Command wheel**: order a horse to follow, stay, wander, return home, or use supported abilities.
- **Horse roster**: manage owned horses, whistle for them, send them home, set an active horse, or disown them.
- **Upgraded Saddle**: unlocks dedicated gear slots for storage, hooves, medkits, and stabilizers.
- **Horse carts**: tow configurable carts with passenger, cargo, and plough support.
- **Riding improvements**: gait controls, free look, leaf passthrough, water handling, step-height changes, multi-riding, and more.
- **Stable Handbook**: an in-game Modonomicon guide covering the mod's systems.

## Requirements

| | |
|:---|:---|
| Minecraft | 1.21.1 |
| Loader | Fabric Loader 0.16.14+ |
| Java | 21 or newer |
| Required | Fabric API 0.116.15+1.21.1, GeckoLib 4.7.5.1+, Modonomicon 1.120.4+ |
| Optional | Mod Menu 11.0.3+, Cloth Config 15.0.140+ |

## Configuration

Server and client options are stored in `config/icys-better-horses.json`. The configuration covers feature toggles, horse abilities, balance/spawn tuning, and HUD placement. Mod Menu and Cloth Config provide the optional in-game settings screen.

## Building

Install JDK 21, then run:

```bash
./gradlew clean build
```

On Windows PowerShell:

```powershell
.\gradlew.bat clean build
```

The built mod and sources JAR are written to `build/libs/`. The project uses Fabric Loom 1.10.5 and official Mojang mappings.

For development runs:

```bash
./gradlew runClient
./gradlew runServer
```

This branch targets **Minecraft 1.21.1 Fabric** and carries the 2.0.6 feature set in its 1.21.1-compatible implementation.

## License

Licensed under the **Bare Minimum License (BML) v1.0**. See [LICENSE](LICENSE) for the full terms. Code may be used and modified under the license; bundled artwork, textures, models, sounds, music, and documentation remain subject to the BML asset restrictions and anti-reposting terms.
