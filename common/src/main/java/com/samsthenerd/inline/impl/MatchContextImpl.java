package com.samsthenerd.inline.impl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.samsthenerd.inline.api.matching.InlineMatch;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.matching.MatchContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.logging.log4j.core.jmx.Server;

public class MatchContextImpl implements MatchContext{
    private final String fullInput;
    private final Text fullInputText;
    private final TreeMap<Integer, InlineMatch> matchMap = new TreeMap<>();
    private final BitSet matchCheck;

    public MatchContextImpl(String fullInput){
        this(Text.of(fullInput));
    }

    public MatchContextImpl(Text textInput){
        this.fullInputText = textInput;
        this.fullInput = textInput.getString();
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

    public Text getFinalStyledText(){
        // don't bother running if we have no matches
        if(matchMap.size() == 0){
            return fullInputText.copy();
        }
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
        return res;
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

    public static class ChatMatchContextImpl extends MatchContextImpl implements ChatMatchContext{

        private ServerPlayerEntity player;

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
