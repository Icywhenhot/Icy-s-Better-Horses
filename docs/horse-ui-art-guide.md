# Horse UI Art Guide

## Item sprite PNGs

Put custom item sprites here:

- `src/main/resources/assets/icys-better-horses/textures/item/upgraded_saddle.png`
- `src/main/resources/assets/icys-better-horses/textures/item/horse_chest_gear.png`
- `src/main/resources/assets/icys-better-horses/textures/item/horse_hooves_gear.png`
- `src/main/resources/assets/icys-better-horses/textures/item/hitchpost.png`
- `src/main/resources/assets/icys-better-horses/textures/item/horse_stabilizer_gear.png`

The matching model files already live here:

- `src/main/resources/assets/icys-better-horses/models/item/`

When you are ready to switch from placeholder vanilla icons to your own art, change each model file from:

```json
"layer0": "minecraft:item/chest"
```

to:

```json
"layer0": "icys-better-horses:item/horse_chest_gear"
```

Do the same pattern for the other item models.

## Custom GUI PNG

Put the horse screen texture here:

- `src/main/resources/assets/icys-better-horses/textures/gui/horse_inventory.png`

Suggested first pass:

- `256x256` PNG
- keep the vanilla horse inventory layout proportions
- leave clean space for:
  - saddle slot
  - armor slot
  - 4 custom gear slots
  - 3x9 chest storage
  - player inventory
  - hotbar

## Recommended workflow

1. Keep the current code while blocking out your art.
2. Draw the screen background first.
3. Draw the item icons second.
4. After the PNGs exist, point the item model JSON files at your mod textures.
5. If you want the whole screen to use a painted background instead of the current code-drawn panels, swap the screen mixin to blit `textures/gui/horse_inventory.png`.

## Notes

- Item PNGs are usually `16x16`.
- GUI sheets are often `256x256`, even if much of the image is empty space.
- Transparent backgrounds are fine.
