---
title: Hitchpost
parent: Equipment
nav_order: 8
---

# Hitchpost
{: .no_toc }

A placeable post that ties a horse to one spot. Cheap, and more immersive than the Stay command.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## Crafting

{% include craft.html id="hitchpost" %}

Any fence works, because the recipe uses the `#minecraft:fences` tag, so wood, nether brick, and modded fences are all fine.

About as cheap as mod content gets, and worth placing anywhere you regularly park.

---

## Using it

Place a hitchpost near a ridden horse, or near another horse you own, and the horse is tethered to it:

> Horse is now tethered to hitch post.

Breaking the post releases the tether.

### When it doesn't work

| Message | Cause |
|:---|:---|
| *No hitchpost within range. Place one nearby first.* | You tried to tether with no post in range |
| *No owned horse close enough to tether.* | No horse of **yours** is near the post |

Hitchposts only work on **your own** horses. Someone else's post won't hold your horse, and yours won't hold theirs.

---

## Hitchpost vs. Stay

Both keep a horse in one place. They differ in how visible and how permanent they are:

| | Hitchpost | [Stay command](../command-wheel#stay) |
|:---|:---|:---|
| Cost | 1 lead + 1 fence | Free |
| Visible in world | Yes, a physical post | No |
| Set up | Place a block | Press <kbd>R</kbd>, pick a wedge |
| Best for | Permanent stables, towns, hitching rails | Ad-hoc stops mid-journey |

For a stable you'll come back to a hundred times, the post is worth it, because it looks like a stable. For "wait here while I check this cave", use Stay.

---

## Disabling

Hitchposts can be switched off server-side with the `hitchpost` config toggle. Disabled, the block stays craftable in **dummy mode** as decoration but won't tether anything. See [Configuration](../configuration).

---

## Related pages

- [Command wheel](../command-wheel): the Stay and Set Home alternatives
- [Upgraded Saddle](upgraded-saddle): a hitchpost can also ride in its own gear slot
- [Recipes](../recipes)
