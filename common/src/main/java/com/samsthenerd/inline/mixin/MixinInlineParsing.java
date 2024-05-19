package com.samsthenerd.inline.mixin;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineMatchResult;
import com.samsthenerd.inline.api.InlineMatchResult.Match;
import com.samsthenerd.inline.api.InlineMatcher;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Pair;

@Mixin(TextVisitFactory.class)
public class MixinInlineParsing {

    @Inject(method="visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
    at=@At(value="INVOKE", target="java/lang/String.charAt (I)C", shift=At.Shift.BEFORE, ordinal=0), cancellable = true)
    private static void handleMatchesInLoop(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor, 
    CallbackInfoReturnable<Boolean> cir, @Local(ordinal=2) LocalIntRef jref, @Local(ordinal=2) LocalRef<Style> currentStyleRef,
    @Share("matches") LocalRef<Map<Integer, Match>> matchesRef){
        
        Match match = matchesRef.get().get(jref.get());
        while(match != null){
            jref.set(jref.get() + match.accept(visitor, jref.get(), currentStyleRef.get()));
            if(jref.get() >= text.length()){
                cir.setReturnValue(true);
            }
            match = matchesRef.get().get(jref.get());
        }
    }

    @Inject(method="visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
    at=@At("HEAD"))
    private static void handleInlineMatchers(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor, 
        CallbackInfoReturnable<Boolean> cir, @Local(ordinal=0, argsOnly = true) LocalRef<String> textRef,
        @Share("matches") LocalRef<Map<Integer, Match>> matchesRef){
        
        List<Pair<Integer, String>> unmatchedTexts = new ArrayList<>();
        unmatchedTexts.add(new Pair<>(0, text));
        Map<Integer, Match> matches = new HashMap<>();

        BitSet matchMask = new BitSet(text.length());

        // run all the matchers
        for(InlineMatcher matcher : InlineAPI.INSTANCE.getAllMatchers()){
            List<Pair<Integer, String>> newUnmatchedTexts = new ArrayList<>();
            for(int i = 0; i < unmatchedTexts.size(); i++){
                Pair<Integer, String> pair = unmatchedTexts.get(i);
                int sequenceOffset = pair.getLeft();
                String sequenceText = pair.getRight();
                InlineMatchResult result = matcher.match(sequenceText);

                BitSet matchedChars = new BitSet(sequenceText.length());

                for(Pair<Pair<Integer, Integer>, Match> match : result.getMatches()){
                    int start = match.getLeft().getLeft();
                    int end = match.getLeft().getRight();
                    Match matchData = match.getRight();
                    
                    if(matchedChars.nextSetBit(start) != -1 && matchedChars.nextSetBit(start) < end){
                        // conflict, choose to just ignore it
                        // Inline.logPrint("Conflict in inline matchers: " + matcher.getClass().getName() + " at position [" + start + ", " + end + "]");
                        continue;
                    }

                    matchedChars.set(start, end);
                    matchMask.set(sequenceOffset + start, sequenceOffset + end);
                    matches.put(sequenceOffset + start, matchData);
                }
                
                // split this segment into remaining unmatched segments
                String currentMatch = "";
                for(int j = 0; j < sequenceText.length(); j++){
                    if(matchedChars.get(j)){
                        if(currentMatch.length() > 0){
                            newUnmatchedTexts.add(new Pair<>(sequenceOffset + j - currentMatch.length(), currentMatch));
                            currentMatch = "";
                        }
                    } else {
                        currentMatch += sequenceText.charAt(j);
                    }
                }
            }
            if(!newUnmatchedTexts.isEmpty())
                unmatchedTexts = newUnmatchedTexts;
        }

        Map<Integer, Match> reindexedMatches = new HashMap<>();

        String finalText = "";
        for(int i = 0; i < text.length(); i++){
            if(!matchMask.get(i)){
                finalText += text.charAt(i);
            }
            if(matches.get(i) != null){
                Match match = matches.get(i);
                reindexedMatches.put(finalText.length(), match);
                int charCount = match.charLength();
                finalText += String.join("", Collections.nCopies(charCount, String.valueOf('.')));
            }
        }
        textRef.set(finalText);
        matchesRef.set(reindexedMatches);
    }
}

