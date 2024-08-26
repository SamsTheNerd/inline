package com.samsthenerd.inline.api.client;

import net.minecraft.util.Identifier;

public interface InlineClientConfig {
    boolean isMatcherEnabled(Identifier matcherId);

    boolean shouldRenderModIcons();

    /**
     * Whether or not create display board mixins should apply.
     */
    boolean shouldDoCreateMixins();

    /**
     * Renderers should respect this cap if they're rendering in chat.
     * @return a value greater than 1 for how large an inline size modifier can be in chat.
     */
    double maxChatSizeModifier();
}
