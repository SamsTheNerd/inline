package com.samsthenerd.inline.utils;

import java.util.Arrays;

/**
 * Effectively a time-variant unary operator over SpriteUVData.
 * <p>
 *     UV lenses allow specifying static region lenses (constructed via {@link SpriteUVRegion#asLens})
 *     or animated region lenses (via {@link AnimUVLens}).
 *
 *     More complex lenses can be constructed by composition with {@link SpriteUVLens#andThen}.
 *     For example, one may want to select a static region within an animated region.
 *
 *     Finally, since it's an open interface, you may define your own lenses if no existing ones fit your needs.
 * </p>
 */
@FunctionalInterface
public interface SpriteUVLens {

    /**
     * Apply a lens to a given UV region.
     * @param uvs base region to start from. See {@link SpriteUVRegion#FULL} to start with.
     * @param time in milliseconds, used for animated region calculations.
     *             See {@link SpriteUVLens#getSysTime()} for a usable input.
     */
    SpriteUVRegion focusUVs(SpriteUVRegion uvs, long time);

    /**
     * focusUVs but applied to a full region
     */
    default SpriteUVRegion genUVs(long time){
        return focusUVs(SpriteUVRegion.FULL, time);
    }

    static SpriteUVRegion identity(SpriteUVRegion uvs, long time){
        return uvs;
    }

    /**
     * Returns a new lens which first applies this lens and then applies the given lens.
     * @param lens the lens to apply after this lens
     * @return a composed lens which first applies this lens and then applies the given lens.
     */
    default SpriteUVLens andThen(SpriteUVLens lens){
        if(lens == null) return this;
        return (uvs, time) -> lens.focusUVs(this.focusUVs(uvs, time), time);
    }

    /**
     * A lens referring to an animated region. Based on a gif.
     * @param frameSize size of a single frame along the varying axis.
     * @param isVertical determines which axis should vary. The other axis will be full (min 0, max 1).
     * @param delays how many milliseconds each frame should appear for.
     * @param totalLength the total number of milliseconds this gif should last for.
     */
    record AnimUVLens(double frameSize, boolean isVertical, int[] delays, int totalLength) implements SpriteUVLens{

        public AnimUVLens(double frameSize, boolean isVertical, int[] delays){
            this(frameSize, isVertical, delays, Arrays.stream(delays).sum());
        }

        @Override
        public SpriteUVRegion focusUVs(SpriteUVRegion uvs, long time){
            if(totalLength == 0) return uvs;
            long currentMS = time % totalLength;
            int delayAcc = 0;
            int i;
            // TODO: can/should this be preprocessed to avoid O(n) walkthrough each time?
            for (i = 0; i < delays.length; i++) {
                delayAcc += delays[i];
                if (delayAcc >= currentMS){
                    break;
                }
            }
            SpriteUVRegion frameUVs;
            double minFP = Math.max(frameSize()*i, 0);
            double maxFP = Math.min(frameSize()*(i+1), 1);
            if(isVertical()){
                frameUVs = new SpriteUVRegion(0, minFP, 1, maxFP);
            } else {
                frameUVs = new SpriteUVRegion(minFP,0, maxFP, 1);
            }
            return uvs.focusWith(frameUVs);
        }
    }

    // this can be used as a reasonable input for focusUVs
    // it is an arbitrary initial value but has consistent changes in value. Value is in milliseconds
    static long getSysTime(){
        return System.nanoTime() / 1000000L;
    }
}
