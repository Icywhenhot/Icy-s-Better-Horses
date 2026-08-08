# Horse Stabilizer — Blockbench working files

Drag `st.geo.json` onto Blockbench (or File -> Open Model). It opens as a Bedrock Entity
project, identifier `geometry.st`, UV space 256x256.

Then:

1. Load `horse_stabilizer.png` as the texture. It is 512x512, exactly 2x the UV space, so it maps 1:1.
2. Switch to Animate mode and load `st.animation.json` (Animations panel -> folder button, or drag the file in).

## What is in the model

Bones: `wingsL`, `wingsL2`, `brace`, `canister right`, `canister left`

Animations:

- `animation` — 0.75s deploy, moves the wings and both canisters
- `wingflap` — 0.8333s glide loop, wings only

## Do not rename these

The Java side looks them up by name:

- `wingsL` / `wingsL2` — `HorseStabilizerGeoRenderer` hides them when the wings are not deployed
- `animation` / `wingflap` — `HorseStabilizerAnimatable`

## Exporting back into the mod

These are working copies. The files the game actually loads are:

| Blockbench file | Ships to |
| --- | --- |
| `st.geo.json` | `src/main/resources/assets/icys-better-horses/geckolib/models/st.geo.json` |
| `st.animation.json` | `src/main/resources/assets/icys-better-horses/geckolib/animations/st.animation.json` |
| `horse_stabilizer.png` | `src/main/resources/assets/icys-better-horses/textures/entity/horse_stabilizer.png` |

Editing the copies in this folder changes nothing in game until they are copied over those paths.

Save the Blockbench project itself as `stabilizer.bbmodel` in this folder so there is a real
source file next time, like `Horse_cart.bbmodel` one directory up.
