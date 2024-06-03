package com.samsthenerd.inline.api.matchers;

import java.util.Map.Entry;

import com.samsthenerd.inline.api.InlineMatch;
import com.samsthenerd.inline.api.InlineMatchResult;
import com.samsthenerd.inline.api.InlineMatcher;
import com.samsthenerd.inline.api.MatchContext;

import net.minecraft.util.Pair;

public interface ContinousMatcher extends InlineMatcher{

    public InlineMatchResult match(String input);

    public default void match(MatchContext matchContext){
        for(Entry<Integer, String> seqEntry : matchContext.getUnmatchedSequences().entrySet()){
            InlineMatchResult res = match(seqEntry.getValue());
            for(Pair<Pair<Integer, Integer>, InlineMatch> match : res.getMatches()){
                int matchStart = match.getLeft().getLeft() + seqEntry.getKey();
                int matchEnd = match.getLeft().getRight() + seqEntry.getKey();
                matchContext.addMatch(matchStart, matchEnd, match.getRight());
            }
        }
    }
}
