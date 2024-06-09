package com.samsthenerd.inline.api.client;

import net.minecraft.util.Identifier;

public interface InlineClientConfig {
    boolean isMatcherEnabled(Identifier matcherId);

    boolean shouldRenderModIcons();
}
