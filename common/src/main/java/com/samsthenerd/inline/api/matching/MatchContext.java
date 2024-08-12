package com.samsthenerd.inline.api.matching;

import java.util.Map;

import com.samsthenerd.inline.impl.MatchContextImpl;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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
     * @return a MatchContext for the input.
     */
    static MatchContext forInput(String inputText){
        return new MatchContextImpl(inputText);
    }

    static MatchContext forTextInput(Text inputText){
        return new MatchContextImpl(inputText);
    }

    /**
     * Gets the raw input
     * @return the original text being matched against.
     */
    String getOriginalText();

    /**
     * Gets the input but with already matched characters being replaced with redactedChar
     * @param redactedChar character to replace already matched characters with
     * @return the redacted input
     */
    String getMatchableText(char redactedChar);

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
    boolean addMatch(int start, int end, InlineMatch match);

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
    Map<Integer, String> getUnmatchedSequences();

    /**
     * Gets the text with all matched characters replaced by placeholders for 
     * the length of the match. This is mostly for internal use.
     * <p>
     * For example, {@code "texta :emote: textb"} with a single length match on 
     * {@code ":emote:"} would return {@code "texta . textb"}
     * @return
     */
    String getFinalText();

    /**
     * Gets the final text with the style baked into it already.
     * This is mostly for internal use.
     * @return
     */
    Text getFinalStyledText();

    /**
     * Gets a map of all matches added. The key is the start index of the match 
     * in the string returned by {@link MatchContext#getFinalText()}, the value is the match.
     * This is mostly for internal use.
     * <p>
     * Note again that the same match object on consecutive characters will count as a single match.
     * @return
     */
    Map<Integer, InlineMatch> getFinalMatches();

    /**
     * An extension of MatchContext that has the player sending the message. Meant to be used for
     */
    interface ChatMatchContext extends MatchContext{
        ServerPlayerEntity getChatSender();

        static ChatMatchContext of(ServerPlayerEntity player, Text originalMessage){ return new MatchContextImpl.ChatMatchContextImpl(player, originalMessage); }
    }
}
