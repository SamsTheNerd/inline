package com.samsthenerd.inline.impl;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.samsthenerd.inline.api.InlineMatch;
import com.samsthenerd.inline.api.MatchContext;

public class MatchContextImpl implements MatchContext{
    private String fullInput;
    private TreeMap<Integer, InlineMatch> matchMap = new TreeMap<>();
    private BitSet matchCheck;

    public MatchContextImpl(String fullInput){
        this.fullInput = fullInput;
        matchCheck = new BitSet(fullInput.length());
    }

    public String getOriginalText(){
        return fullInput;
    }

    public String getMatchableText(char redactedChar){
        String res = "";
        for(int i = 0; i < fullInput.length(); i++){
            if(matchCheck.get(i)){
                res += redactedChar;
            } else {
                res += fullInput.charAt(i);
            }
        }
        return res;
    }

    public boolean addMatch(int start, int end, InlineMatch match){
        int nextMatchIndex = matchCheck.nextSetBit(start);
        if( nextMatchIndex != -1 && nextMatchIndex < end){
            return false;
        }
        for(int i = start; i < end; i++){
            matchMap.put(i, match);
        }
        matchCheck.set(start, end);
        return true;
    }

    public String getFinalText(){
        // don't bother running if we have no matches
        if(matchMap.size() == 0){
            return fullInput;
        }
        String res = "";
        InlineMatch currentMatch = null;
        for(int i = 0; i < fullInput.length(); i++){
            if(matchCheck.get(i)){
                InlineMatch newMatch = matchMap.get(i);
                if(newMatch != currentMatch){
                    int charCount = newMatch.charLength();
                    res += String.join("", Collections.nCopies(charCount, String.valueOf('.')));
                    currentMatch = newMatch;
                }
            } else {
                res += fullInput.charAt(i);
                currentMatch = null;
            }
        }
        return res;
    }

    public Map<Integer, InlineMatch> getFinalMatches(){
        // don't bother running if we have no matches
        if(matchMap.size() == 0){
            return new HashMap<>();
        }
        int matchIndex = matchCheck.nextSetBit(0);
        Map<Integer, InlineMatch> squishedMatchMap = new TreeMap<>();
        InlineMatch currentMatch = null;
        for(int i = matchIndex; i < fullInput.length(); i++){
            if(matchCheck.get(i)){
                InlineMatch newMatch = matchMap.get(i);
                if(newMatch != currentMatch){
                    int charCount = newMatch.charLength();
                    squishedMatchMap.put(matchIndex, newMatch);
                    matchIndex += charCount;
                    currentMatch = newMatch;
                }
            } else {
                matchIndex++;
                currentMatch = null;
            }
        }
        return squishedMatchMap;
    }

    public Map<Integer, InlineMatch> getMatches(){
        int matchIndex = matchCheck.nextSetBit(0);
        Map<Integer, InlineMatch> squishedMatchMap = new TreeMap<>();
        InlineMatch currentMatch = null;
        while(matchIndex >= 0 && matchIndex < fullInput.length()){
            if(matchCheck.get(matchIndex)){
                InlineMatch newMatch = matchMap.get(matchIndex);
                if(newMatch != currentMatch){
                    squishedMatchMap.put(matchIndex, newMatch);
                    currentMatch = newMatch;
                }
                matchIndex++;
            } else {
                currentMatch = null;
                matchIndex = matchCheck.nextSetBit(matchIndex);
            }
        }
        return squishedMatchMap;
    }

    public Map<Integer, String> getUnmatchedSequences(){
        int seqStart = matchCheck.nextClearBit(0);
        Map<Integer, String> seqMap = new TreeMap<>();
        while(seqStart != -1){
            int seqEnd = matchCheck.nextSetBit(seqStart);
            String seq = fullInput.substring(seqStart, seqEnd == -1 ? fullInput.length() : seqEnd);
            seqMap.put(seqStart, seq);
            if(seqEnd == -1){
                return seqMap;
            }
            seqStart = matchCheck.nextClearBit(seqEnd);
        }
        return seqMap;
    }

}
