# Horse Stabilizer Model Guide

This mod now exposes three stabilizer states in code:

- `CLOSED`
- `HALF_OPEN`
- `OPEN`

Those states live in `HorseStabilizerState` and are synced through `IHorseData.bh_getStabilizerState()`.

## 1. Item Model Workflow

Use Blockbench for the inventory/hand item model.

1. Create a new `Java Block/Item` model.
2. Build the stabilizer as a standalone item first:
   - center leather harness body
   - left wing assembly
   - right wing assembly
   - small air compressor on each side
3. Keep the texture on one 16x16 or 32x32 texture atlas at first.
4. Export as a Java item model JSON.
5. Put the JSON in:
   - `src/main/resources/assets/icys-better-horses/models/item/horse_stabilizer_gear.json`
6. Put the texture in:
   - `src/main/resources/assets/icys-better-horses/textures/item/horse_stabilizer.png`

If you only want a flat icon, keep using `item/generated`.
If you want a true 3D inventory model, switch the item JSON from `item/generated` to the exported Blockbench JSON.

## 2. Horse-Mounted Model Workflow

The horse-worn stabilizer is not an item JSON problem. It needs an entity render layer.

You will eventually want:

1. A custom horse render layer or horse renderer mixin on the client.
2. A Blockbench model made for a horse-sized attachment, not a handheld item.
3. Separate poses for:
   - closed wings
   - half-open wings
   - fully open wings

Recommended approach:

1. In Blockbench, create one stabilizer model aligned around the horse torso.
2. Save three pose variants:
   - `stabilizer_closed`
   - `stabilizer_half_open`
   - `stabilizer_open`
3. Export those as Java entity model data, or keep one model and animate wing rotations in code.

## 3. Best Next Step

For this project, the cleanest path is:

1. Build one shared stabilizer model.
2. Make the harness/body fixed.
3. Make only the wings and compressors rotate.
4. Drive those rotations from `HorseStabilizerState`.

That is better than exporting three totally separate full models unless you want very different silhouettes.

## 4. Suggested Blockbench Part Layout

Use part names like:

- `body_harness`
- `back_strap`
- `left_wing_root`
- `left_wing_outer`
- `right_wing_root`
- `right_wing_outer`
- `left_compressor`
- `right_compressor`

With that layout, code can do:

- `CLOSED`: wings folded tight to the body
- `HALF_OPEN`: wings rotated partway out
- `OPEN`: wings fully deployed and compressors visibly angled downward

## 5. Current Logic Already In Code

Right now the gameplay side is already wired:

- stabilizer gear slows descent when the horse falls far enough
- stabilizer cancels fall damage
- hooves reduce fall damage by 50% when the stabilizer is not present

What is still missing for visuals is only the client render layer.

## 6. When You Want The Visual Pass

The next implementation step will be:

1. Add a client-side horse render layer.
2. Read `((IHorseData) horse).bh_getStabilizerState()`.
3. Render the stabilizer model with different wing rotations for each state.

That is the point where your steampunk wing animation becomes visible in-game.
