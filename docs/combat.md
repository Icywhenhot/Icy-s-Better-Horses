---
title: Combat
nav_order: 55
---

# Combat
{: .no_toc }

Horses fight now. Every breed can charge, kick, and take a side when something attacks you.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

---

## Charge

Ride at something fast enough and the horse runs it down. Damage scales with three things: how fast you were going, the horse's [class](breeds/index), and its armour.

Barding matters more than you would expect. Charge and kick damage both read the horse's armour value, so putting diamond barding on a war horse makes it hit noticeably harder as well as survive longer. A Clydesdale gains 25% more armour from equipped barding, giving it a modest extra bonus.

Straight movement builds charge wind-up over three seconds. Turning sharply or stopping resets it; slower straight movement and jumps preserve it. The hit itself requires a rider, the ground, and a gallop.

There is a five second cooldown, and a single charge only hits each mob once no matter how many it passes through.

The charge will never hit you, your pillion rider, the horse's owner, a player you have trusted, or another horse with the same owner.

{: .note }
> On a server with `horse_pvp` set to `no`, a charge or kick can't hurt **any** player, or any animal a player owns: not their wolves, not their cats, not their horses. Horses still run down mobs exactly as before. See [Configuration](configuration).

## Kick

Anything that hits the horse from behind gets kicked. Same damage formula, no cooldown.

## Class damage

| Class | Charge and kick | Knockback |
|:---|:---|:---|
| War | 1.5x | 1.2x |
| Draft | 1.25x | 2.0x |
| Western | 1.0x | 1.0x |
| Pony | 0.75x | 0.8x |
| Race | 0.6x | 0.8x |

War horses hit hardest. Draft horses hit nearly as hard and send things flying twice as far as anything else.

## When you get hurt

A mob that damages you wakes up every horse you own within about 16 blocks, and what happens next depends on whether you are riding.

**Riding.** The horse may spook: it throws you off and bolts. Whistle with <kbd>P</kbd> to call it back, though it will not listen for the first three seconds.

**Not riding.** The horse engages instead. It chases the mob down and keeps hitting it until the mob dies or you whistle it off. It will break off on its own if it drops below 30% health, or if the fight drags it too far from you.

A horse on **Stay** does neither, the same way a sitting dog stays sat.

Owner defence unlocks at **bond 40**. Below that a loose horse will not fight for you.

## Spook chance

| Class | Bond 0 | Bond 40 | Bond 100 |
|:---|:---|:---|:---|
| War | 0% | 0% | 0% |
| Draft | 5% | 3.5% | 2.5% |
| Race, Western, Pony | 10% | 7% | 5% |

**War horses never spook.** That is the reason to ride one somewhere dangerous, and it is the class's headline perk.

A horse will not throw you off over a drop, so a spook can startle you but should not kill you.

## The whistle

<kbd>P</kbd> does three things now, in this order:

1. If any horse you own is fighting or bolting, it calls all of them off.
2. Otherwise, if you are mounted, it opens the [Horse Info screen](horse-info-screen).
3. Otherwise, it whistles your horse to you.

**Calling a horse off sends it running, on purpose.** It drops its target and bolts for about three seconds rather than trotting back to you. That is the useful behaviour: when a loose horse has picked a fight with something far too big for it, the whistle is how you pull it out, and walking back toward you would just keep it in range. Whistle again once it has put some ground between itself and the fight and it will come to you normally.

## Turning it off

Combat is a single config toggle, `horse_combat`, on by default. Turning it off removes charging, kicking, owner defence and spooking in one go. See [Configuration](configuration).

The class perks that feed into combat have their own switches. **War horses never spook** only while `war_steady` is on; turn that one off and a war horse spooks at the normal 10/7/5% rate. Same for the [Clydesdale's](breeds/clydesdale) armour bonus and projectile deflection. See [ability toggles](configuration#ability-toggles).
