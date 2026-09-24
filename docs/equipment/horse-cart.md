---
title: Horse Cart
parent: Equipment
nav_order: 7
---

# Horse Cart
{: .no_toc }

Four seats, a double chest of cargo, a plough on the back, and it rolls along behind your horse as you ride. Draft horses can upgrade it to a six-seat wagon.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## Crafting

{% include craft.html id="horse_cart_gear" %}

Make the wheels first. Any wood type works throughout.

---

## Hitching it to a horse

The cart slots into the **stabilizer slot** of an [Upgraded Saddle](upgraded-saddle). Once fitted it appears behind your horse and rolls along as you ride.

Quickest method: **right-click the horse while holding the cart**. It goes straight on, provided the horse wears an Upgraded Saddle and that slot is empty.

{: .warning }
> The cart **shares its slot with the [Horse Stabilizer](horse-stabilizer)**. A horse tows a cart or wears a stabilizer, never both. Hauling cargo means giving up your fall protection, so pick your route accordingly.

---

## Passengers

An empty small cart seats **four**:

| Position | Seats | Notes |
|:---|:---:|:---|
| Driver's bench (front) | 2 | Whoever sits down **first drives the horse** |
| Bed (rear) | 2 | Lost if a chest is fitted |

The [large wagon](#the-large-wagon) seats six, and keeps two of its bed seats with a chest fitted.

The cart accepts **anything a boat would**. Animals that wander into the bed climb aboard on their own, so you can herd livestock in and drive them home instead of leading them one at a time. Once you're driving, an animal can ride shotgun in the free seat beside you.

{: .note }
> Auto-boarding can be switched off with `cart_pickup` in the [config](../configuration). With it off the bed stays empty unless a player sits in it. Which mobs are eligible is a tag, `icys-better-horses:cart_cargo_blocked` to keep something out and `icys-better-horses:cart_cargo_allowed` to let something oversized in. See [datapack hooks](../configuration#datapack-hooks).

### Unloading

- **Sneak + right-click an animal** to set that one down.
- **Sneak + right-click the cart** to unload everything at once.

---

## Fitting a chest

**Right-click the cart with a Chest** in hand to mount it in the bed.

**Sneak + right-click the cart** to open it. You get a full **double chest** of storage, and the lid swings up while you're browsing.

{: .note }
> The chest fills the bed, so a cart hauling cargo carries **no back-seat passengers**. The driver's bench still seats two.

### Taking the chest off

**Shears** remove the chest and drop it beside the horse.

Empty it first, because a loaded chest refuses to come off rather than spilling across the road:

> Empty the cart's chest before shearing it off.

---

## The large wagon

The cart has a second size. Put one on a **draft horse** and press the **Cart Size key** (default <kbd>Left Alt</kbd>) while looking at the horse or the cart, and it swaps to a longer wagon on a bigger frame.

| | Small cart | Large wagon |
|:---|:---:|:---:|
| Who can pull it | Any horse | **Draft only** |
| Bed seats | 2 | **4** |
| Bed seats with a chest | 0 | **2** |
| Chest size | Double chest, 54 slots | **90 slots** |
| Takes a plough | Yes | No |
| Shades riders from the sun | No | **Yes** |

Only the four draft breeds can pull it: [Percheron](../breeds/percheron), [Clydesdale](../breeds/clydesdale), [Shire](../breeds/shire), and [Belgian](../breeds/belgian). Ask any other horse and it refuses:

> Only draft horses can pull the large cart.

Press the key again to go back down to the small cart. The swap is refused, with a message saying which, if the wagon is carrying more than the small cart can hold: too many passengers, a plough fitted, or items sitting in the outer chest columns. Clear the problem and it swaps.

{: .tip }
> The wagon has a roof, so **undead riding in it don't burn in daylight**. It's a safe way to haul zombies or a zombie villager you're taking home to cure.

{: .note }
> This is the reason to raise a draft horse. They are the slowest class in the mod, and the wagon is what they are for: six seats and ninety slots is a moving base camp.

---

## Fitting a plough

**Right-click the small cart with any hoe** and a plough drops down behind the bed. From then on, driving the cart over **grass, dirt or coarse dirt** turns a **three-block-wide** strip into farmland behind you, one pass, no dismounting.

The block above has to be clear, same as tilling by hand, so the plough skips anything with a plant or a block sitting on it. It also respects claim and spawn protection: anything the driver couldn't till by hand, the plough leaves alone.

{: .note }
> Which blocks it turns is the `icys-better-horses:ploughable` tag, so a pack can add its own soil types. See [datapack hooks](../configuration#datapack-hooks).

{: .note }
> Only the small cart takes a plough. The big wagon has no room for one, and a cart wearing a plough refuses to swap up to the large size until you shear it off.

### Wear

The hoe on the cart takes **one point of durability per block turned**, and it keeps its enchantments while it is on there, so **Unbreaking** and **Mending** are worth having. When it finally breaks, the plough drops off the cart with it.

### Taking the plough off

**Shears**, the same as the chest. If the cart has both fitted, the shears take the **plough first** and the chest on the next click.

---

## Parking a cart on its own

**Right-click the ground** with the cart to stand one up by itself, shafts down, no horse needed.

A parked cart is **decoration** and nobody rides it, but a **Chest** and a **plough** both fit it exactly as they do a hitched one (a parked plough turns nothing, of course). That makes it a good-looking storage prop for a stable, market stall, or camp.

To pick it back up, **hit it a few times**, like a boat. Breaking one drops whatever was fitted to it: the chest and everything inside, and the plough's hoe.

---

## Removal order

Gear comes off in the reverse order it went on. The slot **flashes red** if you try to skip a step:

1. **Shear the plough** off, if one is fitted
2. **Shear the chest** off the cart
3. **Unhitch the cart**
4. **Then** the saddle can come off

The relevant messages:

> Shear the chest off the cart before unhitching it.

> Unhitch the cart before taking the saddle off.

---

## Related pages

- [Wheel](wheel): the component you need two of
- [Horse Stabilizer](horse-stabilizer): the alternative use for this slot
- [Upgraded Saddle](upgraded-saddle): required to hitch a cart at all
- [Recipes](../recipes)
