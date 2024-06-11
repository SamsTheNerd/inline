package com.samsthenerd.inline.api.client;

import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.matchers.ContinuousMatcher;
import com.samsthenerd.inline.api.client.matchers.RegexMatcher;

import net.minecraft.util.Identifier;

/**
 * Matches patterns in any rendered text. 
 * <p> 
 * Matchers can replace portions of the input text with some {@link InlineMatch}
 * in order to insert text or attach some arbitrary {@link InlineData}. Most matchers
 * can and should be made using one of the {@link RegexMatcher}s but the raw matcher 
 * interface is available here in case it's needed.
 * <p>
 * Regardless of how the matcher is made, it should be registered using {@link InlineClientAPI#addMatcher}.
 * 
 * @see ContinuousMatcher
 * @see RegexMatcher
 * @see RegexMatcher.Simple
 * @see RegexMatcher.Standard
 */
public interface InlineMatcher {
    /**
     * Matches some input text. See {@link MatchContext} for more information on 
     * how to add the matches.
     * @param matchContext
     */
    public void match(MatchContext matchContext);

    /**
     * Get info about this matcher.
     * @return matcher's info.
     */
    public MatcherInfo getInfo();

    /**
     * Get the ID for this matcher
     * @return matcher's ID
     */
    public Identifier getId();
}
