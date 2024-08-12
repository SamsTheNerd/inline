package com.samsthenerd.inline.api.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.matching.InlineMatch.DataMatch;
import com.samsthenerd.inline.api.matching.InlineMatch.TextMatch;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public interface ContinuousMatcher extends InlineMatcher{

    ContinuousMatchResult match(String input, MatchContext matchContext);

    default void match(MatchContext matchContext){
        for(Entry<Integer, String> seqEntry : matchContext.getUnmatchedSequences().entrySet()){
            ContinuousMatchResult res = match(seqEntry.getValue(), matchContext);
            for(Pair<Pair<Integer, Integer>, InlineMatch> match : res.getMatches()){
                int matchStart = match.getLeft().getLeft() + seqEntry.getKey();
                int matchEnd = match.getLeft().getRight() + seqEntry.getKey();
                matchContext.addMatch(matchStart, matchEnd, match.getRight());
            }
        }
    }

    class ContinuousMatchResult {
        
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
