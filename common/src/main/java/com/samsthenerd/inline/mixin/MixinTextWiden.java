package com.samsthenerd.inline.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.InlineRenderer;
import com.samsthenerd.inline.impl.InlineStyle;

import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;

@Mixin(TextRenderer.class)
public class MixinTextWiden {
    @Inject(method = "method_27516(ILnet/minecraft/text/Style;)F", at = @At("HEAD"), cancellable = true)
    private void TextHandlerOverrideForPattern(int codepoint, Style style, CallbackInfoReturnable<Float> cir){
        InlineStyle inlStyle = (InlineStyle)style;
        // if(inlStyle.isHidden()){
        //     cir.setReturnValue(0f);
        //     return;
        // }
        InlineData inlData = inlStyle.getInlineData();
        if(inlData == null){
            return;
        }
        InlineRenderer ilRenderer = InlineAPI.INSTANCE.getRenderer(inlData.getRendererId());
        if(ilRenderer != null){
            int cWidth = ilRenderer.charWidth(inlData, style, codepoint);
            cir.setReturnValue((float)cWidth);
        }
    }


    @WrapOperation(method="method_37297(Lnet/minecraft/client/font/TextRenderer$Drawer;[FIFIIILnet/minecraft/text/Style;I)Z",
    at=@At(value="INVOKE", target="net/minecraft/client/font/Glyph.getAdvance (Z)F"))
    private float MakeTextGlowWider(Glyph glyph, boolean bold, Operation<Float> original, @Coerce Object drawer, float[] fs, int i, float f, int j, int k, int index, Style style, int codepoint){
        InlineStyle inlStyle = (InlineStyle)style;
        InlineData inlData = inlStyle.getInlineData();
        if(inlData == null){
            return original.call(glyph, bold);
        }
        InlineRenderer ilRenderer = InlineAPI.INSTANCE.getRenderer(inlData.getRendererId());
        if(ilRenderer != null){
            int cWidth = ilRenderer.charWidth(inlData, style, codepoint);
            return cWidth;
        }
        return original.call(glyph, bold);
    }

    @WrapOperation(method="method_37297(Lnet/minecraft/client/font/TextRenderer$Drawer;[FIFIIILnet/minecraft/text/Style;I)Z",
    at=@At(value="INVOKE", target="net/minecraft/text/Style.withColor (I)Lnet/minecraft/text/Style;"))
    private Style MarkTextGlowy(Style originalStyle, int color, Operation<Style> original){
        InlineStyle inlStyle = (InlineStyle)originalStyle;
        return original.call(inlStyle.withGlowyMarker(true), color);
    }
}
