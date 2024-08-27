package com.samsthenerd.inline.api.client;

import com.samsthenerd.inline.api.client.renderers.InlineErrorRenderer;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.matching.MatchContext;
import com.samsthenerd.inline.impl.InlineClientImpl;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Set;

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
     * Gets a frozen/read-only {@link MatchContext} with all currently enabled client-side matchers
     * (see {@link InlineClientAPI#getAllMatchers} and {@link InlineClientConfig#isMatcherEnabled}) applied to it.
     * <p>
     * This is backed by a cache for faster matching of repeated values. Since the returned MatchContexts are cached/re-used,
     * it's likely that some of their operations (such as {@link MatchContext#getFinalMatches()} or {@link MatchContext#getFinalText()})
     * will have already been ran and cached as well.
     * @param input string to match against
     * @return read-only MatchContext with all currently enabled client-side matchers applied to it
     */
    MatchContext getMatched(String input);

    /**
     * Like {@link InlineClientAPI#getMatched(String)} but for {@link Text} input.
     * @param input Text to match against
     * @return read-only MatchContext with all currently enabled client-side matchers applied to it
     */
    MatchContext getMatched(Text input);

    /**
     * Get the client config.
     * @return inline client config.
     */
    InlineClientConfig getConfig();
}
