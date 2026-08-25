package icy.betterhorses.net.client.render;

/**
 * Gait tuning for the large-breed foal.
 *
 * <p>Every large breed shares one foal mesh -- {@code Percheron baby.bbmodel},
 * the file itself rather than a copy per breed -- so they must share its gait
 * numbers too. These sat in {@code PercheronFoalModel} while it was the only
 * large foal; they moved here the moment the Shire became the second, so that
 * tuning the foal's walk tunes it for every large breed at once instead of
 * letting the Shire's silently drift out of step with the Percheron's.
 *
 * <p>{@link #FRONT_HOLD} is the one that was actually hard to get right. It
 * holds the shoulder still while the leg swings, and it was sized twice on the
 * wrong evidence -- first on how far the leg box overlaps the barrel, which
 * ignores the lever the leg swings on, and then again after Icy reshaped the
 * mesh and the overlap went to zero.
 */
final class BhLargeFoalGait {

    static final float STRIDE = 0.55F;

    static final float FRONT_HOLD = 0.68F;
    static final float BACK_HOLD = 0.15F;

    static final float FRONT_REACH = 0.30F;
    static final float BACK_REACH = 0.70F;

    private BhLargeFoalGait() {
    }
}
