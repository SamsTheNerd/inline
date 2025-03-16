package com.samsthenerd.inline.mixin.interop;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.matching.MatchContext;
import com.samsthenerd.inline.utils.mixin.RequireMods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@RequireMods("create")
@Mixin(targets="com.simibubi.create.content.trains.display.FlapDisplaySection")
public class MixinCreateMakeBoardsRightLength {

    @Shadow
    boolean[] spinning;
    @Shadow
    boolean singleFlap;
    @Shadow
    float size;

    @Unique
    private int origSpinningLength = -1;

    @Unique
    private float originalSize = -1f;

    @WrapOperation(
            method="Lcom/simibubi/create/content/trains/display/FlapDisplaySection;refresh(Z)V",
            at=@At(value="INVOKE", target="Ljava/lang/Math;min(II)I")
    )
    private int getInlineBasedTextLength(int spinningLength, int origLength, Operation<Integer> minOp, @Local LocalRef<String> textRef){
        if(!InlineClientAPI.INSTANCE.getConfig().shouldDoCreateMixins()){
            // if we've already done our little mixin before then undo our spinning array size thing.
            if(origSpinningLength != -1 && spinning.length != origSpinningLength){
                spinning = new boolean[singleFlap ? 1 : origSpinningLength];
                origSpinningLength = -1; // just avoid future checks i guess.
            }
            return minOp.call(spinningLength, origLength);
        }
        // spinning length is how many chars we have to work with
        // origLength is from newText.length() - if we have a match this will be quite long, perhaps longer than spinningLength even if it's only '1' char
        MatchContext matchContext = InlineClientAPI.INSTANCE.getMatched(textRef.get().trim());

        if(origSpinningLength == -1){
            origSpinningLength = spinningLength;
            originalSize = size;
        }

        if(matchContext.getMatches().isEmpty()) return minOp.call(spinningLength, origLength); // no matches just call original and leave
        int squishedLength = matchContext.getFinalText().length(); // how long the parsed text is. Shorter than origLength if we have matches.
        int origMin = minOp.call(origSpinningLength, squishedLength); // this is the "actual" length that we want to fit to.
        int lenNeededForUnparsed = matchContext.finalToOrig(origMin+1)-1; // this is how many chars we actually have
        boolean[] oldSpinning = this.spinning;
        this.spinning = new boolean[singleFlap ? 1 : Math.max(origSpinningLength, lenNeededForUnparsed)];
        System.arraycopy(oldSpinning, 0, this.spinning, 0, Math.min(oldSpinning.length, this.spinning.length));
        return lenNeededForUnparsed;
    }

    @WrapOperation(
            method="Lcom/simibubi/create/content/trains/display/FlapDisplaySection;refresh(Z)V",
            at=@At(value="INVOKE", target="Ljava/lang/String;charAt(I)C")
    )
    private char getSafeCharAt(String maybeOldText, int idx, Operation<Character> opCharAt){
        if(idx >= maybeOldText.length()){
            return '℗'; // yeah that's probably obscure enough to not be used in game..
        }
        return opCharAt.call(maybeOldText, idx);
    }
}
