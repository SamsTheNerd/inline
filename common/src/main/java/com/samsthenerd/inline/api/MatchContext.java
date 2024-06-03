package com.samsthenerd.inline.api;

import java.util.Map;

import com.samsthenerd.inline.impl.MatchContextImpl;

public interface MatchContext {
    
    public static MatchContext forInput(String inputText){
        return new MatchContextImpl(inputText);
    }

    public String getOriginalText();

    public String getMatchableText(char redactedChar);

    public boolean addMatch(int start, int end, InlineMatch match);

    public Map<Integer, String> getUnmatchedSequences();

    public String getFinalText();

    public Map<Integer, InlineMatch> getFinalMatches();
}
