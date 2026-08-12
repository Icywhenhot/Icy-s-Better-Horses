package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;

/**
 * Marks an entity whose breed is fixed by its type rather than inferred from its coat.
 *
 * <p>The original breed system had to guess: every horse was a vanilla {@code Horse}, so
 * {@code HorseBreed.breedsMatchingCoat} picked a plausible breed from the coat and markings
 * the horse already wore. That is still the right behaviour for plain vanilla horses in
 * existing worlds - a palomino stays a palomino.
 *
 * <p>It is the wrong behaviour for a dedicated breed mob. An {@link IcelandicHorse} is an
 * Icelandic regardless of what coat it rolled, so it declares that here and the coat-guessing
 * path skips it entirely.
 *
 * <p>Every future breed mob should implement this. It is the single hook that keeps the info
 * screen, the roster, the handbook and the breeding rules reporting the right thing without
 * any of them needing to know which breeds have their own entity type yet.
 */
public interface BhBreedEntity {

    /** The breed this entity type always is. Never {@code null}. */
    HorseBreed bhFixedBreed();
}
