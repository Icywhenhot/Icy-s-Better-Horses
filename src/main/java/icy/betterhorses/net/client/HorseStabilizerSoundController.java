package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class HorseStabilizerSoundController {

    private static final int INTRO_DURATION_TICKS = 104;
    private static final float ACTIVATION_FALL_DISTANCE = 4.0F;
    private static final Map<Integer, ActiveStabilizerSound> ACTIVE_SOUNDS = new HashMap<>();

    public static void tick(Minecraft client) {
        if (client.level == null) {
            stopAll();
            return;
        }

        Set<Integer> seenHorseIds = new HashSet<>();
        for (net.minecraft.world.entity.Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof AbstractHorse horse) || !(horse instanceof IHorseData data)) {
                continue;
            }

            seenHorseIds.add(horse.getId());
            ActiveStabilizerSound controller = ACTIVE_SOUNDS.computeIfAbsent(
                    horse.getId(),
                    id -> new ActiveStabilizerSound(horse));
            controller.setHorse(horse);
            controller.setActive(
                    data.bh_getStabilizerState() != HorseStabilizerState.CLOSED,
                    horse.fallDistance >= ACTIVATION_FALL_DISTANCE);
            controller.tick(client.getSoundManager());
        }

        Iterator<Map.Entry<Integer, ActiveStabilizerSound>> iterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ActiveStabilizerSound> entry = iterator.next();
            if (!seenHorseIds.contains(entry.getKey())) {
                entry.getValue().stopImmediately();
                iterator.remove();
                continue;
            }

            if (entry.getValue().isFinished()) {
                iterator.remove();
            }
        }
    }

    private static void stopAll() {
        for (ActiveStabilizerSound controller : ACTIVE_SOUNDS.values()) {
            controller.stopImmediately();
        }
        ACTIVE_SOUNDS.clear();
    }

    private HorseStabilizerSoundController() {}

    private static final class ActiveStabilizerSound {
        private final int horseId;
        private AbstractHorse horse;
        private boolean active;
        private int introTicks;
        private StabilizerSoundInstance introSound;
        private StabilizerSoundInstance loopSound;

        private ActiveStabilizerSound(AbstractHorse horse) {
            this.horseId = horse.getId();
            this.horse = horse;
        }

        private void setHorse(AbstractHorse horse) {
            this.horse = horse;
        }

        private void setActive(boolean stabilizerActive, boolean thresholdReached) {
            this.active = stabilizerActive && (thresholdReached || this.introSound != null || this.loopSound != null);
        }

        private void tick(SoundManager soundManager) {
            if (this.horse.isRemoved() || !this.horse.isAlive()) {
                this.stopImmediately();
                return;
            }

            if (this.active) {
                if (this.loopSound != null && this.loopSound.isStopped()) {
                    this.loopSound = null;
                }

                if (this.introSound == null && this.loopSound == null) {
                    this.startIntro(soundManager);
                } else if (this.introSound != null && this.introSound.isFadingOut()) {
                    this.stopImmediately();
                    this.startIntro(soundManager);
                } else if (this.loopSound != null && this.loopSound.isFadingOut()) {
                    this.stopImmediately();
                    this.startIntro(soundManager);
                } else if (this.loopSound == null) {
                    this.introTicks++;
                    if (this.introTicks >= INTRO_DURATION_TICKS) {
                        this.startLoop(soundManager);
                    }
                }
            } else {
                if (this.introSound != null) {
                    this.introSound.fadeOut();
                }
                if (this.loopSound != null) {
                    this.loopSound.fadeOut();
                }
            }

            if (this.introSound != null && this.introSound.isStopped()) {
                this.introSound = null;
            }
            if (this.loopSound != null && this.loopSound.isStopped()) {
                this.loopSound = null;
            }
        }

        private void startIntro(SoundManager soundManager) {
            this.introTicks = 0;
            this.introSound = new StabilizerSoundInstance(this.horse, ModSounds.STABILIZER_INTRO, false);
            soundManager.play(this.introSound);
        }

        private void startLoop(SoundManager soundManager) {
            if (this.loopSound != null || !this.active) {
                return;
            }

            this.loopSound = new StabilizerSoundInstance(this.horse, ModSounds.STABILIZER_LOOP, true);
            soundManager.play(this.loopSound);
        }

        private void stopImmediately() {
            if (this.introSound != null) {
                this.introSound.stopNow();
                this.introSound = null;
            }
            if (this.loopSound != null) {
                this.loopSound.stopNow();
                this.loopSound = null;
            }
        }

        private boolean isFinished() {
            return !this.active && this.introSound == null && this.loopSound == null;
        }
    }

    private static final class StabilizerSoundInstance extends AbstractTickableSoundInstance {
        private static final int FADE_OUT_TICKS = 6;

        private final AbstractHorse horse;
        private boolean fadingOut = false;
        private float fadeStep = 1.0F / FADE_OUT_TICKS;

        private StabilizerSoundInstance(AbstractHorse horse, SoundEvent sound, boolean looping) {
            super(sound, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
            this.horse = horse;
            this.looping = looping;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.delay = 0;
            this.relative = false;
            this.attenuation = Attenuation.LINEAR;
            this.syncPosition();
        }

        @Override
        public void tick() {
            if (this.horse.isRemoved() || !this.horse.isAlive()) {
                this.stop();
                return;
            }

            this.syncPosition();
            if (!this.fadingOut) {
                return;
            }

            this.volume = Math.max(0.0F, this.volume - this.fadeStep);
            if (this.volume <= 0.0F) {
                this.stop();
            }
        }

        private void syncPosition() {
            this.x = this.horse.getX();
            this.y = this.horse.getY() + this.horse.getBbHeight() * 0.7D;
            this.z = this.horse.getZ();
        }

        private void fadeOut() {
            this.fadingOut = true;
        }

        private boolean isFadingOut() {
            return this.fadingOut;
        }

        private void stopNow() {
            this.stop();
        }
    }
}
