---
title: FAQ
nav_order: 15
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

Yes. Horses you already have keep their appearance, because the mod reads their existing coat and assigns a matching breed. Coats that don't match anything become [Mustangs](breeds/mustang). Nothing is deleted or re-rolled.

### Does it work on a server?

Yes. The gear toggles and `horse_exclusivity` exist specifically for multiplayer. See [Configuration](configuration).

---

## Horses and ownership

### Why can't my friend ride my horse?

That's [owner-only riding](ownership-and-bonding#owner-only-riding), on by default. They *can* ride behind you as a second rider while you're in the saddle. To let anyone ride anything, set `horse_exclusivity` to `no`.

### My horse won't come when I whistle.

Check three things:

1. **Is it the active horse?** Only the horse marked **Set Active** on the [roster](managing-your-horses) answers the whistle. 
2. **Same dimension?** *"That horse is in another dimension."*
3. **Are you mounted?** While riding, <kbd>P</kbd> opens the info screen instead of whistling.

### What does "Resting" mean on the roster?

The horse is in an unloaded chunk. It's fine. Owned horses are stored persistently and get restored when you call them.

### Can I get a disowned horse back?

No. [Disowning](managing-your-horses#disown) is permanent, which is why it has a confirmation dialog. Its bond and home are gone with it.

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

They're the same gender: *"These horses are the same gender and can't breed."* Check on the [info screen](horse-info-screen) before feeding golden apples, because the apple is consumed either way.

### How do I get a better horse?

Foal stats are `max(parent1, parent2) + roll(−0.5 to +1.0)`, so the foal builds on the **better** parent and can beat both. Breed the best of each generation, retire the rest. The vanilla base stat is the ceiling. Full method: [breeding loop](genetics#a-practical-breeding-loop).

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

<kbd>P</kbd> whistle/info, <kbd>R</kbd> command wheel, <kbd>G</kbd> manage horses. All rebindable in **Options → Controls → Icy's Better Horses**.

### How do I turn on auto-ride?

**Double-tap <kbd>W</kbd>** while riding. Tap any movement key to cancel.

### The horse turns when I move the mouse. Can I look around?

You already can. You get up to **90 degrees** of free look while stationary before the horse follows your gaze. Past that it turns to match you.

---

## Still stuck?

Open an issue on [GitHub](https://github.com/Icywhenhot/Icy-s-Better-Horses/issues).
