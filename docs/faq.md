---
title: FAQ
nav_order: 16
---

# FAQ
{: .no_toc }

1. TOC
{:toc}

---

## Installing

### What do I need to run it?

For the currently supported version, Minecraft **26.2**, Fabric loader **0.19.3+**, and **Java 25 or newer**. Required mods: [Fabric API](https://modrinth.com/mod/fabric-api), [GeckoLib](https://modrinth.com/mod/geckolib) 5.5.0+, and [Modonomicon](https://modrinth.com/mod/modonomicon).

### The game won't start and it mentions Modonomicon.

Modonomicon is a **hard dependency**, not optional. Install it and the game will boot.

### Do I need Mod Menu and Cloth Config?

No. They're optional and only add the in-game settings screen. Without them, edit `config/icys-better-horses.json` directly. See [Configuration](configuration).

### Is it safe to add to an existing world?

Yes, nothing is deleted. Your horses keep their name, owner, bond, gear, and inventory.

They **do** change colour once, though. The mod reads the vanilla coat each horse is wearing, works out the closest matching breed from it, then gives it one of that breed's own coats. Coats that don't match anything become [Mustangs](breeds/mustang). It's a one-time change on first load and the new coat sticks.

### Does it work on a server?

Yes. The gear toggles, `horse_exclusivity` and `horse_pvp` exist specifically for multiplayer, and the server's config applies to everyone who joins. See [Configuration](configuration).

### Will horses spawn in my modded biomes?

Not on their own. Every breed ships with a list of vanilla biomes, so in a pack that replaces the overworld you'll find horses only in whatever vanilla biomes are left.

Adding them is a small datapack: each breed reads a biome tag, so you add your biomes to `icys-better-horses:tags/worldgen/biome/spawns/<breed>` and they spawn there. Worked example on the [Spawning](spawning#modded-biomes) page.

---

## Horses and ownership

### Why can't my friend ride my horse?

That's [owner-only riding](ownership-and-bonding#owner-only-riding), on by default. They *can* ride behind you as a second rider while you're in the saddle.

To let one specific friend ride on their own, run `/horse trust <player>`. To let *anyone* ride *anything*, set `horse_exclusivity` to `no`. See [Commands](commands).

### Does trusting someone give them my saddle and gear?

Yes. `/horse trust` hands over everything short of ownership: riding, the gear screen, the saddlebags, a cart's chest, shearing the saddle, hitching, and the command wheel. The only thing a trusted player can't do is disown the horse.

They can also ride it away and empty it, so only trust people you'd hand a lead to. See [Commands](commands#what-trust-actually-allows).

### Where is my horse?

Open the [roster](managing-your-horses) with <kbd>G</kbd> and select the horse. Its coordinates are printed under the 3D model, along with the dimension it's in on the list row.

### My horse won't come when I whistle.

Check three things:

1. **Is its bond above 0?** A freshly tamed horse ignores the whistle entirely: *"Build some bond with this horse before it will answer your whistle."* One point is enough.
2. **Is it the active horse?** Only the horse marked **Set Active** on the [roster](managing-your-horses) answers the whistle.
3. **Same dimension?** *"That horse is in another dimension."*
4. **Is it towing a cart?** *"Unhitch the cart before calling or teleporting this horse."*
5. **Are you mounted?** While riding, <kbd>P</kbd> opens the info screen instead of whistling.
6. **Is one of your horses fighting?** Then <kbd>P</kbd> calls it off instead of summoning anything. Press it again once the fight is over.

### What does "Resting" mean on the roster?

The horse is in an unloaded chunk. It's fine. Owned horses are stored persistently and get restored when you call them.

### Can I get a disowned horse back?

Sort of. [Disowning](managing-your-horses#disown) releases the horse rather than deleting it, so it's still standing there, untamed. You can walk up and tame it again.

What you can't get back is the **bond**, the saved home, and its roster entry. A re-tamed horse starts from bond 0, so treat disowning as throwing away the hours you put in, not the animal.

### Why won't it let me disown a horse?

It's still wearing gear: *"Take your equipment off this horse before disowning it."* Strip the saddle and gear first. This also stops you accidentally throwing away five diamonds.

---

## Bonding

### How long does it take to reach 100 bond?

About **100 minutes** of standing within 10 blocks of it (+1/minute). A [Name Tag](ownership-and-bonding#gaining-bond) gives +10 once, and each Golden Apple +2, so feeding shortens it considerably.

### Do a second and third name tag give more bond?

No. The +10 is **once per horse**.

### Does bond gain work while I'm away?

No. The owner has to be within 10 blocks, in the same dimension. Park the horse near where you're building and it accumulates while you play.

### Why is my bonded horse still slow?

Bond is a **percentage** bonus on the horse's own base stats, up to +75% at 100 bond. A slow horse bonded to 100 is still a slow horse. For real speed you need good base stats too, which means [breeding](genetics).

---

## Breeding

### My two horses won't breed.

Almost always the same gender: *"These horses are the same gender and can't breed."* They both go into love mode first, hearts and all, so the hearts aren't a promise. Check on the [info screen](horse-info-screen) *before* feeding golden apples, because both apples are consumed either way.

If you'd rather not think about it, set `gender_breeding` to `no` in the [config](configuration).

### How do I get a better horse?

Foal stats are the **average** of both parents plus one fresh roll from the breed's class range, clamped to that range. So a foal can beat both parents, but a bad pairing drags it down, and no amount of breeding pushes a horse past its class ceiling. Keep only the foals that beat both parents. Full method: [breeding loop](genetics#a-practical-breeding-loop).

### What is the (mix) tag?

The foal inherited two different breeds. Its breed came from one parent at random and its coat was re-rolled from that breed's palette. Producing one earns **Best of Both Worlds**.

---

## Gear

### Why can't I put a stabilizer and a cart on the same horse?

They **share one slot**. A horse tows a cart or wears a stabilizer, never both. Pick per trip: stabilizer for mountains, cart for hauling.

### Why won't the saddle come off?

Gear comes off in reverse order. If a cart is hitched: *"Unhitch the cart before taking the saddle off."* If the cart has a chest: shear the chest off first. The slot flashes **red** when you skip a step.

### Why won't the chest come off the cart?

It isn't empty. A loaded chest refuses removal rather than spilling its contents.

### My stabilizer won't fit on my mule.

Stabilizers are **horses only**, not mules, donkeys, skeleton horses, or zombie horses.

### Where did my medkit go?

It fired. Medkits are **one use**, consumed the moment the horse drops below half health. Craft a replacement; an empty slot looks the same as a full one at a glance.

### Can I enchant horse gear?

Only [Horse Hooves](equipment/horse-hooves#frost-walker), and only with **Frost Walker**, applied at an anvil from a book.

---

## Controls

### What are the default keys?

<kbd>P</kbd> whistle/info, <kbd>R</kbd> command wheel, <kbd>G</kbd> manage horses, <kbd>V</kbd> walk cycle, <kbd>H</kbd> rear, <kbd>Left Ctrl</kbd> free look, <kbd>Left Alt</kbd> cart size. All rebindable in **Options → Controls → Icy's Better Horses**.

<kbd>P</kbd>, <kbd>Left Ctrl</kbd> and <kbd>Left Alt</kbd> clash with vanilla Social Interactions, Sprint, and whatever your other mods use. Minecraft flags clashes in red on the controls screen.

### How do I get the big wagon?

Put a cart on a **draft horse** ([Percheron](breeds/percheron), [Clydesdale](breeds/clydesdale), [Shire](breeds/shire), [Belgian](breeds/belgian)), then press <kbd>Left Alt</kbd> while looking at it. Only draft horses can pull it. See [Horse Cart](equipment/horse-cart#the-large-wagon).

### How do I turn on auto-ride?

**Double-tap <kbd>W</kbd>** while riding. Tap any movement key to cancel.

### The horse turns when I move the mouse. Can I look around?

You already can. You get up to **90 degrees** of free look while stationary before the horse follows your gaze. Past that it turns to match you.

---

## Still stuck?

Open an issue on [GitHub](https://github.com/Icywhenhot/Icy-s-Better-Horses/issues).
