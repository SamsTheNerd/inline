package com.samsthenerd.inline.utils;

/**
 * A data class storing UV coordinates to refer to a specific region.
 * All values should be between 0 and 1.
 */
public record SpriteUVRegion(double minU, double minV, double maxU, double maxV){

    public static SpriteUVRegion FULL = new SpriteUVRegion(0, 0, 1, 1);

    public double uWidth(){
        return maxU - minU;
    }

    public double vHeight(){
        return maxV - minV;
    }

    /**
     * Applies the given UVs to this such that the new UVs are inside of this.
     */
    public SpriteUVRegion focusWith(SpriteUVRegion luvs){
        double w = uWidth();
        double h = vHeight();
        return new SpriteUVRegion(
            luvs.minU() * w + minU(),
            luvs.minV() * h + minV(),
            luvs.maxU() * w + minU(),
            luvs.maxV() * h + minV()
        );
    }

    /**
     * Returns a lens which focuses based on this.
     */
    public SpriteUVLens asLens(){
        return (uvs, time) -> focusWith(uvs);
    }
}
