package com.samsthenerd.inline.api.client;

import java.util.Set;

import javax.annotation.Nullable;

import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.renderers.InlineErrorRenderer;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.impl.InlineClientImpl;

import net.minecraft.util.Identifier;

/**
 * The main Inline Client API. Primarly for managing matchers and renderers.
 */
public interface InlineClientAPI {

    static final InlineClientAPI INSTANCE = new InlineClientImpl();

    /**
     * Register an {@link InlineRenderer}
     * @param renderer
     */
    void addRenderer(InlineRenderer<?> renderer);

    /**
     * Get an {@link InlineRenderer} by its ID
     * @param id 
     * @return renderer with the given id, or a {@link InlineErrorRenderer} if none could be found.
     */
    InlineRenderer<?> getRenderer(Identifier id);

    /**
     * Get all registered renderers
     * @return all registered renderers
     */
    Set<InlineRenderer<?>> getAllRenderers();

    /**
     * Register an {@link InlineMatcher}
     * @param matcher
     */
    void addMatcher(InlineMatcher matcher);

    /**
     * Get an {@link InlineMatcher} by its ID
     * @param id 
     * @return matcher with the given id, or null if it does not exist.
     */
    @Nullable
    InlineMatcher getMatcher(Identifier id);

    /**
     * Get all registered matchers
     * @return all registered matchers
     */
    Set<InlineMatcher> getAllMatchers();

    /**
     * Get the client config.
     * @return inline client config.
     */
    InlineClientConfig getConfig();
}
