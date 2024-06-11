package com.samsthenerd.inline.api.client;

import java.util.Map;

import com.samsthenerd.inline.impl.MatchContextImpl;

/**
 * This is the core of the matcher system. 
 * <p>
 * A MatchContext is made out of some input string with {@link MatchContext#forInput(input)}. 
 * It's then given to each matcher, which can parse it however it sees fit 
 * and add new matches on a given range. 
 */
public interface MatchContext {
    
    /**
     * Creates a MatchContext with the given input as its base.
     * @param inputText
     * @return
     */
    public static MatchContext forInput(String inputText){
        return new MatchContextImpl(inputText);
    }

    /**
     * Gets the raw input
     * @return the original text being matched against.
     */
    public String getOriginalText();

    /**
     * Gets the input but with already matched characters being replaced with redactedChar
     * @param redactedChar character to replace already matched characters with
     * @return the redacted input
     */
    public String getMatchableText(char redactedChar);

    /**
     * Attaches the given match to all characters in the range. 
     * <p>
     * Note that the same match object repeated consecutively will be treated 
     * as a single match until it's interrupted.
     * @param start start of range inclusive
     * @param end end of range exclusive
     * @param match match to assign
     * @return whether the match was successfully added. will return false if any 
     * character in this range is already matched.
     */
    public boolean addMatch(int start, int end, InlineMatch match);

    /**
     * Gets all unmatched segments.
     * <p>
     * For example, with the input string {@code texta :emote: textb}, if the {@code :emote:}
     * is matched, then this returns a {@code map} with: 
     * <p>
     * {@code map.size() == 2}, {@code map.get(0) == "texta "}, and {@code map.get(13) == " textb"}
     * @return a map with unmatched segments as entries, their starting index as 
     * the key and the segment string as the value
     */
    public Map<Integer, String> getUnmatchedSequences();

    // TODO: finish this page

    /**
     * Gets the text with all matches replaced by placeholders 
     * @return
     */
    public String getFinalText();

    /**
     * 
     * @return
     */
    public Map<Integer, InlineMatch> getFinalMatches();
}
