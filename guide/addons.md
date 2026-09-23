# Addon guide

If you want to make addons for the mod, here's a little guide.

## IDs and registration

There are two historical namespaces in this branch. Prefer the supplied constants and registry objects instead of constructing built-in IDs yourself.

| Purpose | Value |
| --- | --- |
| Forge mod ID | `icys_better_horses` |
| Custom registry namespace | `icys-better-horses` |
| Built-in breed, ability, and command entry namespace | `icys_better_horses` |
| Asset and equipment tag namespace | `icys-better-horses` |
| Your addon entries and assets | Your own mod ID, such as `trailaddon` |

`BhRegistries` exposes Forge registries for breeds, archetypes, abilities, commands, genders, and species. you should prolly use a `DeferredRegister` with the corresponding registry key. Do not replace built-in registry entries or register under another mod's namespace, obviously.

## A working ability and command addon

This example appends a healing skill to Shires and adds a command that stops navigation. Put these two Java files in your addon's source tree. The ability has a cooldown, which survives saving and loading the horse.

`trailaddon/TrailAddon.java`:

```java
package trailaddon;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.AbilityType;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.CommandType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(TrailAddon.ID)
public final class TrailAddon {
    public static final String ID = "trailaddon";
    public static final DeferredRegister<AbilityType> ABILITIES =
            DeferredRegister.create(BhRegistries.ABILITY_TYPES, ID);
    public static final DeferredRegister<CommandType> COMMANDS =
            DeferredRegister.create(BhRegistries.COMMAND_TYPES, ID);

    public static final RegistryObject<AbilityType> RECOVER = ABILITIES.register(
            "recover", () -> new AbilityType(Recover::new, true));
    public static final RegistryObject<CommandType> HALT = COMMANDS.register(
            "halt", () -> new CommandType(
                    (horse, player) -> IHorseData.of(horse).bh_getBond() >= 20,
                    (horse, player) -> {
                        horse.getNavigation().stop();
                        IHorseData.of(horse).bh_setCommand(BhContent.COMMAND_STAY.getKey());
                    }));

    public TrailAddon() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ABILITIES.register(bus);
        COMMANDS.register(bus);
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BhContent.SHIRE.get().addAbility(RECOVER.getKey()));
    }
}
```

`trailaddon/Recover.java`:

```java
package trailaddon;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.BhAbilityState;
import icy.betterhorses.net.feature.breed.BreedAbility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class Recover implements BreedAbility {
    private int cooldown;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (cooldown > 0) cooldown--;
    }

    @Override
    public boolean hasActiveSkill() {
        return true;
    }

    @Override
    public void onActivate(AbstractHorse horse, IHorseData data) {
        if (cooldown > 0 || horse.getHealth() >= horse.getMaxHealth()) return;
        horse.heal(2.0F);
        cooldown = 200;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("cooldown", cooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        cooldown = Math.max(0, tag.getInt("cooldown"));
    }
}
```

Add `assets/trailaddon/lang/en_us.json`:

```json
{
  "command.trailaddon.halt": "Halt",
  "ability.trailaddon.recover": "Recover"
}
```

### Ability lifecycle

The factory must return a **new instance** for each horse. Do not return a shared singleton. Better Horses also constructs client instances for active-skill discovery, so constructors must be safe on either physical side.

| Hook | Contract |
| --- | --- |
| `tick(horse, data, state)` | Runs on the logical server for enabled abilities. |
| `hasActiveSkill()` | Describes whether a skill has an activation button; evaluated on client and server. Keep this independent of unsynchronized cooldown fields. |
| `onActivate(horse, data)` | Runs on the server after the selected registered ability, horse access, and command distance have been checked. Enforce your cooldown, costs, and other gameplay conditions here. |
| `onDetach(horse, data)` | Clean up transient effects when the ability is disabled, the breed changes, or the horse is removed. Make cleanup safe to repeat. |
| `save(tag)` | Writes this instance's persistent state into a fresh compound. Do not mutate gameplay state here. |
| `load(tag)` | Restores state before server use. New horses and older saves supply an empty compound. |

Saved ability state lives inside `BH_Abilities`, keyed first by breed ID and then by ability ID. Two addons can use the same field name within their own compounds. Stored data for an unavailable ability is retained, and a horse saved again before its first tick retains pending state. Registry IDs are save identities: renaming them requires your own migration strategy.

Persistent state is not automatically synchronized to clients. For HUD state, use the existing `BhSurge` helpers or your addon's networking. The built-in surge HUD has four indexed ability slots; additional abilities still tick, activate, and save, but do not acquire extra built-in HUD slots. `BhSurge.set(data, abilityKey, packed)` avoids hardcoding a slot index. Namespaced keys passed to `BhHorseAttributes.apply` also prevent two addons from reusing the same modifier identity.

`AbilityType`'s `defaultEnabled` governs addon abilities. Built-in abilities retain their existing configuration behavior. An addon's ability is not disabled merely because the built-in Shire ability is disabled. For a configurable addon ability, implement and synchronize your own configuration and enforce it in `tick` and `onActivate`.

### Extending an existing breed

Call `BreedType.addAbility(key)` inside `FMLCommonSetupEvent.enqueueWork`, on both client and server. It appends without removing built-in abilities and ignores duplicate additions. `abilities()` returns an immutable snapshot.

Finish additions before any horses are created. Runtime mutation and datapack-driven changes to the ability list are not supported. Use the same addons and registration order on the client and server so indexed HUD slots match.

### Multiple active abilities

The radial wheel lists every enabled active ability by its own ID, including skills after the first slot. Appaloosa's existing toggle remains a separate entry. Labels use `ability.<namespace>.<path>`.

`IHorseAbilityHost.bh_activeAbilities()` returns available active-skill IDs. `bh_activateAbility(key)` selects one on the logical server and returns whether it was dispatched; it does not report whether a cooldown check inside the ability allowed an effect. The host method is an internal gameplay entry point without a player permission check. Player requests should go through the radial command handler or through an addon handler that validates access itself.

## Custom commands

Use the two-argument `CommandType` constructor shown above. Its predicate controls visibility and is rechecked server-side. It must be safe on both sides and use synchronized data when visibility depends on horse state. The callback runs only on the server.

Custom entries appear after the built-in commands and active skills, sorted by command ID. Register translations as `command.<namespace>.<path>`. The server rejects missing command IDs, horses outside the managed set, untamed horses, players without handling permission, and requests more than 12 blocks away. It also rejects unavailable commands and ability IDs not present among the horse's enabled active skills.

A custom callback does not automatically change the horse's follow/stay command. Set that explicitly when it is part of your action, as the Halt example does. A new `CommandType()` without a callback is only a command-state marker; it does not automatically get a menu entry or custom AI behavior.

## Addon equipment

The four existing equipment slots accept additional items through these item tags:

| Slot | Tag |
| --- | --- |
| Chest | `icys-better-horses:gear/chest` |
| Hooves | `icys-better-horses:gear/hooves` |
| Medkit | `icys-better-horses:gear/medkit` |
| Stabilizer/cart | `icys-better-horses:gear/stabilizer` |

For example, put this in `data/icys-better-horses/tags/items/gear/hooves.json`:

```json
{
  "replace": false,
  "values": ["trailaddon:trail_shoes"]
}
```

Register `trailaddon:trail_shoes` as an item in your addon. `GearSlot.items()` exposes the tag key, and `accepts(stack)` checks both the tag and the original built-in items. This adds compatibility to the existing slots; it does not add new slots or bypass the upgraded-saddle requirement.

Slot occupancy contributes to the existing gear flags. Some built-in behavior checks those flags; other behavior still deliberately checks exact items. For example, putting an addon item in the medkit slot does not make it the built-in consumable medkit, and a stabilizer-slot tag does not turn an item into a horse cart. Implement custom effects explicitly using an ability or a feature hook, and inspect the equipped stack before applying them. Access equipment through `IHorseData.bh_getGearContainer()` and `GearSlot.ordinal()`; use container setters so normal change notifications run.

## Attach behavior independently of breed abilities

Listen for `HorseFeaturesEvent` on `MinecraftForge.EVENT_BUS`. It is posted once per managed horse's feature-list initialization on either logical side. Add a new instance of your `HorseFeature` for that horse:

```java
MinecraftForge.EVENT_BUS.addListener((icy.betterhorses.net.api.HorseFeaturesEvent event) -> {
    event.add((horse, data) -> {
        if (horse.level().isClientSide() || horse.tickCount % 100 != 0) return;
        if (horse.isInWater()) horse.getPersistentData().putBoolean("trailaddon:been_wet", true);
    });
});
```

Register the listener once from your mod constructor. `HorseFeature` also has `onLoad`, `onRemoved`, and `onInventoryChanged` hooks. Features append after the built-in feature list. Do not share a mutable feature instance across horses.

Feature initialization can happen while the horse's inventory or saved data is being restored. Do not assume all breed or equipment fields are final inside the attachment event; evaluate changing eligibility inside your feature methods. Features can run on both sides, so guard server-only gameplay work. For feature persistence use a namespaced key in Forge's entity persistent data or your own capability; automatic per-instance save/load in this API belongs to `BreedAbility`.

## Register a new breed

Use `DeferredRegister<BreedType>` with `BhRegistries.BREED_TYPES`, and register an entity type for a subclass of `BhBreedHorse`. Implement `bhFixedBreed()` to return your breed's key. `BhBreedHorse` supplies coat storage and inheritance; `bhAttributes(archetype)` supplies the matching starting attribute builder.

A breed registration has this shape, where `TRAIL_HORSE` is your registered entity type and `RECOVER` is your registered ability:

```java
BREEDS.register("trail_horse", () -> BreedType.builder(BhContent.DRAFT.getKey())
        .coats("trailaddon", "trail_horse", java.util.List.of("bay", "black"), true)
        .entityType(TRAIL_HORSE.getKey())
        .chestRows(4)
        .bondedChestRows(4)
        .ability(RECOVER.getKey())
        .build());
```

Register the entity's attributes with Forge's entity attribute creation event, and register its renderer and model layers on the physical client. Review `PercheronHorse`, `PercheronHorseRenderer`, and `BhHorseRenderer` for the existing entity and tack-rendering pattern. Breed registration does not generate models, entity registration, renderers, spawn rules, sounds, or spawn eggs for you.

Coat paths for this example are:

```text
assets/trailaddon/textures/entity/horse/trail_horse/bay.png
assets/trailaddon/textures/entity/horse/trail_horse/black.png
assets/trailaddon/textures/entity/horse/trail_horse/baby/bay.png
assets/trailaddon/textures/entity/horse/trail_horse/baby/black.png
```

With `hasFoalVariant=false`, the coat set uses the adult textures for foals. Keep coat ordering stable for existing saves. Add `breed.trailaddon.trail_horse` and `breed.trailaddon.mix_format` translations; the latter accepts the breed name as `%s`.

Registered breed entity types are recognized by `BhHorseKind.managed`. For other compatible `AbstractHorse` types, the `icys-better-horses:horses` entity-type tag opts them into managed behavior. Do not add arbitrary non-horse entities or another mod's entities without testing their behavior. Use `BhHorseKind.managed(entity)` before interacting with horse-specific data on arbitrary entities.

You can also register an `ArchetypeType` through `BhRegistries.ARCHETYPE_TYPES` to choose stat ranges and supported archetype properties. Attach executable custom abilities through `BreedType` rather than relying on `ArchetypeType.Builder.ability`; the existing archetype class-perk list is not dispatched by the breed-ability controller.

## Compatibility and testing

Reuse `IHorseData`, `IHorseAbilityHost`, the registries, and the attachment event before adding mixins. Keep gameplay mutations on the server. Do not replace global entity movement, camera setup, or horse tick methods to implement an addon skill.



