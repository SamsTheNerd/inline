package com.samsthenerd.inline.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.samsthenerd.inline.api.InlineClientAPI;
import com.samsthenerd.inline.api.InlineClientConfig;
import com.samsthenerd.inline.api.InlineMatch;
import com.samsthenerd.inline.api.InlineMatcher;
import com.samsthenerd.inline.api.MatchContext;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;

@Mixin(TextVisitFactory.class)
public class MixinInlineParsing {

    @Inject(method="visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
    at=@At(value="INVOKE", target="java/lang/String.charAt (I)C", shift=At.Shift.BEFORE, ordinal=0), cancellable = true)
    private static void handleMatchesInLoop(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor, 
    CallbackInfoReturnable<Boolean> cir, @Local(ordinal=2) LocalIntRef jref, @Local(ordinal=2) LocalRef<Style> currentStyleRef,
    @Share("matches") LocalRef<Map<Integer, InlineMatch>> matchesRef){
        
        InlineMatch match = matchesRef.get().get(jref.get());
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
        @Share("matches") LocalRef<Map<Integer, InlineMatch>> matchesRef){
        
        MatchContext matchContext = MatchContext.forInput(text);

        InlineClientConfig config = InlineClientAPI.INSTANCE.getConfig();

        // run all the matchers
        for(InlineMatcher matcher : InlineClientAPI.INSTANCE.getAllMatchers()){
            if(!config.isMatcherEnabled(matcher.getId())) continue;
            matcher.match(matchContext);
        }

        textRef.set(matchContext.getFinalText());
        matchesRef.set(matchContext.getFinalMatches());
    }
}

