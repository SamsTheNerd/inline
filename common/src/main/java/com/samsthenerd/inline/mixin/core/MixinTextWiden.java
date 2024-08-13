package com.samsthenerd.inline.mixin.core;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.InlineRenderer;
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
        InlineRenderer ilRenderer = InlineClientAPI.INSTANCE.getRenderer(inlData.getRendererId());
        if(ilRenderer != null){
            double sizeMod = style.getComponent(InlineStyle.SIZE_MODIFIER_COMP);
            int cWidth = (int)(ilRenderer.charWidth(inlData, style, codepoint) * (ilRenderer.handleOwnSizing() ? 1 : (float)sizeMod));
            cir.setReturnValue((float)cWidth);
        }
    }


    @SuppressWarnings("unchecked")
    @WrapOperation(method="method_37297(Lnet/minecraft/client/font/TextRenderer$Drawer;[FIFIIILnet/minecraft/text/Style;I)Z",
    at=@At(value="INVOKE", target="net/minecraft/client/font/Glyph.getAdvance (Z)F"))
    private float MakeTextGlowWider(Glyph glyph, boolean bold, Operation<Float> original, @Coerce Object drawer, float[] fs, int i, float f, int j, int k, int index, Style style, int codepoint){
        InlineData inlData = style.getInlineData();
        if(inlData == null){
            return original.call(glyph, bold);
        }
        InlineRenderer ilRenderer = InlineClientAPI.INSTANCE.getRenderer(inlData.getRendererId());
        if(ilRenderer != null){
            double sizeMod = style.getComponent(InlineStyle.SIZE_MODIFIER_COMP);
            int cWidth = (int)(ilRenderer.charWidth(inlData, style, codepoint) * (ilRenderer.handleOwnSizing() ? 1 : (float)sizeMod));
            return cWidth;
        }
        return original.call(glyph, bold);
    }

    // this makes the
    @WrapOperation(method="method_37297(Lnet/minecraft/client/font/TextRenderer$Drawer;[FIFIIILnet/minecraft/text/Style;I)Z",
    at=@At(value="INVOKE", target="net/minecraft/text/Style.withColor (I)Lnet/minecraft/text/Style;"))
    private Style MarkTextGlowy(Style originalStyle, int color, Operation<Style> original){
        return original.call(originalStyle.withComponent(InlineStyle.GLOWY_MARKER_COMP,true), color);
    }

    @WrapOperation(
            method="drawWithOutline(Lnet/minecraft/text/OrderedText;FFIILorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at=@At(value="INVOKE", target="Lnet/minecraft/text/OrderedText;accept(Lnet/minecraft/text/CharacterVisitor;)Z")
    )
    private boolean MarkTextParentGlowy(OrderedText originalText, CharacterVisitor visitor, Operation<Boolean> originalOp,
                                            // parent method params so we can get outlineColor
                                            OrderedText text, float x, float y, int color, int outlineColor, Matrix4f matrix, VertexConsumerProvider vertexConsumers, int light){
        CharacterVisitor markedVisitor = (int index, Style style, int codePoint) ->
                visitor.accept(index, style.withComponent(InlineStyle.GLOWY_PARENT_COMP, outlineColor), codePoint);
        return originalOp.call(originalText, markedVisitor);
    }

}
