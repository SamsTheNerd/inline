package com.samsthenerd.inline.api.client;

import com.samsthenerd.inline.impl.InlineStyle;

/**
 * Indicates how the render system should handle glow effects. Glow effects happen when a sign is clicked with a glow ink
 * sac. Vanilla text handles this by rendering text in 8 offsets, doing this with inline renders tends to create a busy
 * z-fighting mess.
 * <p>
 * The render system can work around this by first rendering to a framebuffer, flattening the image to a flat color, adding a border,
 * and then rendering it as a texture. This can be a tad bit laggy though, especially for many renders, so if your renderer is
 * able to neatly handle its own outline (for example if it's already a flat color) then it should do so and indicate {@link None}
 * as its preference. Otherwise indicate {@link Full} if the render system should handle it for you. If your render is
 * static, meaning it won't animate with time, then you can indicate a cache id as well, using {@link Full#Full(String)}
 * so that the glow texture doesn't need to be remade every frame.
 */
public sealed class GlowHandling permits GlowHandling.None, GlowHandling.Full {
    /**
     * Indicates that the renderer will handle the glow effect on its own with no help from the render system.
     * Note that this will allow the vanilla offset calls to happen so if you don't check using {@link InlineStyle#GLOWY_MARKER_COMP}
     * you'll end up with 9 renders slightly offset from eachother.
     */
    public static final class None extends GlowHandling{}

    /**
     * Indicates that the render system will handle the glow effect. In this case you don't need to worry any more about
     * glow effects. If your render is static/not animated then you can indicate a cache id here as well for better performance.
     * The cacheId should be able to be used as an {@link net.minecraft.util.Identifier}
     */
    public static final class Full extends GlowHandling{
        public final String cacheId;
        public Full(){ cacheId = null; }

        /**
         * @param cacheId an identifier that uniquely represents the data being rendered that can be used with an
         *                {@link net.minecraft.util.Identifier}. Should only be used for non-moving renders.
         */
        public Full(String cacheId){ this.cacheId = cacheId;}
    }
}
