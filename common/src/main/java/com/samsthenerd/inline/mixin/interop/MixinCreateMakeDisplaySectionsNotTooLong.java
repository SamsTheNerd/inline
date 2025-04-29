package com.samsthenerd.inline.mixin.interop;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.matching.MatchContext;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Slice;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

@Pseudo
@Mixin(targets="com.simibubi.create.content.trains.display.FlapDisplayRenderer", remap = false)
public class MixinCreateMakeDisplaySectionsNotTooLong {

    @Unique
    private static MethodHandle textGetter;

    static {
        try{
            textGetter = MethodHandles.lookup().findGetter(
                    Class.forName("com.simibubi.create.content.trains.display.FlapDisplaySection"), "text", String.class
            );
        } catch(Exception e){ /*no create, that's ok*/ }
    }

    @WrapOperation(
//            method="Lcom/simibubi/create/content/trains/display/FlapDisplayRenderer;renderSafe(Lcom/simibubi/create/content/trains/display/FlapDisplayBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            method="renderSafe",
            at=@At(value="INVOKE", target="Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal=0, remap = true),
            slice=@Slice(
                    from=@At(value="INVOKE", target="Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", remap = true)
            ), remap = false
    )
    private void makeTranslateNotMove(MatrixStack stack, float x, float y, float z, Operation<Void> translateOp, @Share("takeitbacknowyall") LocalFloatRef backRef){
        translateOp.call(stack,x - backRef.get(), y, z);
    }

    @WrapOperation(
//            method="Lcom/simibubi/create/content/trains/display/FlapDisplayRenderer;renderSafe(Lcom/simibubi/create/content/trains/display/FlapDisplayBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            method="renderSafe",
            at=@At(value="INVOKE", target="Lcom/simibubi/create/content/trains/display/FlapDisplayRenderer$FlapDisplayRenderOutput;nextSection(Lcom/simibubi/create/content/trains/display/FlapDisplaySection;)V"),
            remap = false
    )
    private void catchSection(@Coerce Object renderOutput, @Coerce Object nextSection, Operation<Void> actualOp, @Share("takeitbacknowyall") LocalFloatRef backRef){
        if(InlineClientAPI.INSTANCE.getConfig().shouldDoCreateMixins()) {
            try {
                // we can assume that sectionText is properly trimmed
                String sectionText = (String) (textGetter.invoke(nextSection));
                MatchContext matchContext = InlineClientAPI.INSTANCE.getMatched(sectionText.trim());

                int squishedLength = matchContext.getFinalText().length(); // how long the parsed text is. Shorter than origLength if we have matches.
                int lenNeededForUnparsed = matchContext.finalToOrig(squishedLength + 1) - 1; // this is how many chars the unparsed takes up
                backRef.set(7 * (lenNeededForUnparsed - squishedLength));
            } catch (Throwable e) {
                // oopsies,
            }
        }
        actualOp.call(renderOutput, nextSection);
    }

}
