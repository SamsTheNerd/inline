package com.samsthenerd.inline.api;

import java.util.Set;

import com.samsthenerd.inline.impl.InlineClientImpl;

import net.minecraft.util.Identifier;

/**
 * This API is primarily for adding and getting renderers and matchers. 
 */
public interface InlineClientAPI {

    static final InlineClientAPI INSTANCE = new InlineClientImpl();

    void addRenderer(InlineRenderer<?> renderer);

    InlineRenderer<?> getRenderer(Identifier id);

    Set<InlineRenderer<?>> getAllRenderers();

    void addMatcher(InlineMatcher matcher);

    InlineMatcher getMatcher(Identifier id);

    Set<InlineMatcher> getAllMatchers();

    InlineClientConfig getConfig();
}
