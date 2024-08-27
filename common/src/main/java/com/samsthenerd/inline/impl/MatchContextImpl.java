package com.samsthenerd.inline.impl;

import com.samsthenerd.inline.api.matching.InlineMatch;
import com.samsthenerd.inline.api.matching.MatchContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MatchContextImpl implements MatchContext{
    private final String fullInput;
    private final Text fullInputText;
    private final TreeMap<Integer, InlineMatch> matchMap = new TreeMap<>(); // orig index -> match for every match
    private final TreeMap<Integer, InlineMatch> singularMatchMap = new TreeMap<>(); // orig index -> match for the first index of every match.
    private final BitSet matchCheck; // a bitset representing which bits have been already matched

    // CACHED VALUES
    private Text finalStyledTextCached = null;
    private String finalTextCached = null;
    private Map<Integer, InlineMatch> finalMatchesCached = null;

    private boolean frozen = false;

    public MatchContextImpl(String fullInput){
        this(Text.of(fullInput));
    }

    public MatchContextImpl(Text textInput){
        this.fullInputText = textInput;
        this.fullInput = textInput.getString();
        matchCheck = new BitSet(fullInput.length());
    }

    public boolean isFrozen(){
        return frozen;
    }

    public void freeze(){
        frozen = true;
    }

    public String getOriginalText(){
        return fullInput;
    }

    public String getMatchableText(char redactedChar){
        StringBuilder resBuilder = new StringBuilder();
        for(int i = 0; i < fullInput.length(); i++){
            if(matchCheck.get(i)){
                resBuilder.append(redactedChar);
            } else {
                resBuilder.append(fullInput.charAt(i));
            }
        }
        return resBuilder.toString();
    }

    public boolean addMatch(int start, int end, InlineMatch match){
        if(frozen) return false;
        int nextMatchIndex = matchCheck.nextSetBit(start);
        if( nextMatchIndex != -1 && nextMatchIndex < end){
            return false;
        }
        for(int i = start; i < end; i++){
            matchMap.put(i, match);
        }
        // do some checks and add to singular map.
        if(singularMatchMap.get(start-1) != match){ // only put it in the singular map if the prev idx is not the same match
            singularMatchMap.put(start, match);
        }
        if(singularMatchMap.get(start+1) == match){
            singularMatchMap.remove(start+1); // make sure the one after it isn't the same.
        }
        matchCheck.set(start, end);
        finalStyledTextCached = null;
        finalTextCached = null;
        finalMatchesCached = null;
        return true;
    }

    public Text getFinalStyledText(){
        // don't bother running if we have no matches
        if(matchMap.isEmpty()){
            return fullInputText.copy();
        }
        if(finalStyledTextCached != null) return finalStyledTextCached.copy();
        MutableText res = Text.empty();
        // atomic as a mutable wrapper for lambda non-sense.
        AtomicReference<InlineMatch> currentMatch = new AtomicReference<>(null);
        AtomicInteger chunkIdx = new AtomicInteger(0);
        fullInputText.visit((Style sty, String seg) -> {
            for(int i = 0; i < seg.length(); i++){
                int absI = i + chunkIdx.get();
                if(matchCheck.get(absI)){
                    InlineMatch newMatch = matchMap.get(absI);
                    if(newMatch != currentMatch.get()){
                        // if we have a new match then accept the match and add to the final result.
                        newMatch.accept((vI, vSty, codePoint) -> {
                            res.append(Text.literal(Character.toString(codePoint)).setStyle(vSty));
                            return true; // sounds fine?
                        }, res.getString().length(), sty);
                        currentMatch.set(newMatch);
                    }
                } else {
                    res.append(Text.literal(seg.substring(absI, absI+1)).setStyle(sty));
                }
            }
            chunkIdx.addAndGet(seg.length());
            return Optional.empty();
        }, Style.EMPTY);
        finalStyledTextCached = res.copy();
        return res;
    }

    public String getFinalText(){
        // don't bother running if we have no matches
        if(matchMap.isEmpty()){
            return fullInput;
        }
        if(finalTextCached != null) return finalTextCached;
        StringBuilder res = new StringBuilder();
        InlineMatch currentMatch = null;
        // O(n) for fullInput size ?
        for(int i = 0; i < fullInput.length(); i++){
            if(matchCheck.get(i)){
                InlineMatch newMatch = matchMap.get(i);
                if(newMatch != currentMatch){
                    int charCount = newMatch.charLength();
                    res.append(String.join("", Collections.nCopies(charCount, String.valueOf('.'))));
                    currentMatch = newMatch;
                }
            } else {
                res.append(fullInput.charAt(i));
                currentMatch = null;
            }
        }
        finalTextCached = res.toString();
        return finalTextCached;
    }

    public Map<Integer, InlineMatch> getFinalMatches(){
        // don't bother running if we have no matches
        if(matchMap.isEmpty()){
            return new HashMap<>();
        }
        if(finalMatchesCached != null) return new TreeMap<>(finalMatchesCached);
        int matchIndex = matchCheck.nextSetBit(0);
        Map<Integer, InlineMatch> squishedMatchMap = new TreeMap<>();
        InlineMatch currentMatch = null;
        // also O(n) for fullInput length, we just need to convert the indices though?
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
        finalMatchesCached = squishedMatchMap;
        return squishedMatchMap;
    }

    public Map<Integer, InlineMatch> getMatches(){
        return new TreeMap<>(singularMatchMap);
    }

    public int origToFinal(int orig){
        // need to count how many chars were 'lost' in matching and how many were re-added from matching.
        int unmatchedIsh = orig; // largest number <= orig that is either not in a match or is the start of a match
        if(matchCheck.get(orig)){ // orig is in a match, so largest number must be the start of the match.
            unmatchedIsh = singularMatchMap.floorKey(orig);
        }
        int prevMatchChars = matchCheck.get(0, unmatchedIsh).cardinality(); // count how many matched chars there are *before* unmatchedIsh
        int numMatches = singularMatchMap.subMap(0, unmatchedIsh).size(); // get how many matches we have *before* unmatchedIsh
        return unmatchedIsh - prevMatchChars + numMatches;
    }

    public int finalToOrig(int fin){
        int fCounted = 0;
        int i = 0; // orig counter
        InlineMatch currentMatch = null;
        while(fCounted < fin){
            InlineMatch newMatch = matchMap.get(i);
            if(newMatch == null){
                currentMatch = null;
                fCounted++;
            } else if(newMatch != currentMatch){
                currentMatch = newMatch;
                fCounted++;
            }
            i++;
        }
        return i;
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

    public static class ChatMatchContextImpl extends MatchContextImpl implements ChatMatchContext{

        private final ServerPlayerEntity player;

        public ChatMatchContextImpl(ServerPlayerEntity player, Text originalMsg){
            super(originalMsg);
            this.player = player;
        }

        @Override
        public ServerPlayerEntity getChatSender(){
            return player;
        }
    }
}
