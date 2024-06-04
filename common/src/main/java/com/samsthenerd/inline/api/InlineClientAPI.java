package com.samsthenerd.inline.api;

import java.util.Set;

import com.samsthenerd.inline.impl.InlineClientImpl;

import net.minecraft.util.Identifier;

/**
 * This API is primarily for adding and getting renderers and matchers. 
 */
public abstract class InlineClientAPI {

    public static final InlineClientAPI INSTANCE = new InlineClientImpl();

    public abstract void addRenderer(InlineRenderer<?> renderer);

    public abstract InlineRenderer<?> getRenderer(Identifier id);

    public abstract Set<InlineRenderer<?>> getAllRenderers();

    public abstract void addMatcher(InlineMatcher matcher);

    public abstract InlineMatcher getMatcher(Identifier id);

    public abstract Set<InlineMatcher> getAllMatchers();

}
