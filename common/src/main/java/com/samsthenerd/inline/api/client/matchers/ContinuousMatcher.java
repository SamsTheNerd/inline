package com.samsthenerd.inline.api.client.matchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.InlineMatch;
import com.samsthenerd.inline.api.client.InlineMatch.DataMatch;
import com.samsthenerd.inline.api.client.InlineMatch.TextMatch;
import com.samsthenerd.inline.api.client.InlineMatcher;
import com.samsthenerd.inline.api.client.MatchContext;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public interface ContinuousMatcher extends InlineMatcher{

    public ContinuousMatchResult match(String input);

    public default void match(MatchContext matchContext){
        for(Entry<Integer, String> seqEntry : matchContext.getUnmatchedSequences().entrySet()){
            ContinuousMatchResult res = match(seqEntry.getValue());
            for(Pair<Pair<Integer, Integer>, InlineMatch> match : res.getMatches()){
                int matchStart = match.getLeft().getLeft() + seqEntry.getKey();
                int matchEnd = match.getLeft().getRight() + seqEntry.getKey();
                matchContext.addMatch(matchStart, matchEnd, match.getRight());
            }
        }
    }

    public static class ContinuousMatchResult {
        
        // Horrible generic, but whatever, consider it an implementation detail
        private List<Pair<Pair<Integer, Integer>, InlineMatch>> matches = new ArrayList<>();

        public ContinuousMatchResult addMatch(int start, int end, InlineData data){
            return addMatch(start, end, data, Style.EMPTY);
        }
        
        public ContinuousMatchResult addMatch(int start, int end, Text text){
            return addMatch(start, end, new TextMatch(text));
        }

        public ContinuousMatchResult addMatch(int start, int end, InlineData data, Style style){
            return addMatch(start, end, new DataMatch(data, style));
        }

        public ContinuousMatchResult addMatch(int start, int end, InlineMatch match){
            // TODO: add conflict detection here ?
            matches.add(new Pair<>(new Pair<>(start, end), match));
            return this;
        }

        public List<Pair<Pair<Integer, Integer>, InlineMatch>> getMatches(){
            return new ArrayList<>(matches);
        }    
    }
}
