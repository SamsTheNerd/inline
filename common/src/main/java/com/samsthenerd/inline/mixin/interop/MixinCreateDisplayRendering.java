package com.samsthenerd.inline.mixin.interop;

import com.google.common.util.concurrent.AtomicDouble;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.impl.InlineRenderCore;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="com.simibubi.create.content.trains.display.FlapDisplayRenderer$FlapDisplayRenderOutput", remap = false)
public class MixinCreateDisplayRendering {
    @Final @Shadow VertexConsumerProvider bufferSource;
    @Final @Shadow float r, g, b, a;
    @Final @Shadow Matrix4f pose;
    @Final @Shadow int light;

    @Shadow float x;
    @Shadow private int lineIndex;

    @Inject(
//        method = "accept(ILnet/minecraft/text/Style;I)Z"
        method = "accept"
        , at = @At("HEAD"), cancellable = true, remap = false)
    private void InlineCreateRenderDrawerAccept(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        if(!InlineClientAPI.INSTANCE.getConfig().shouldDoCreateMixins()){
            return;
        }
        AtomicDouble xUpdater = new AtomicDouble(x);
        InlineRenderCore.RenderArgs args = new InlineRenderCore.RenderArgs(x, 0, pose, light, false, 1f,
                r, g, b, a, TextRenderer.TextLayerType.NORMAL, bufferSource, xUpdater);
        if(InlineRenderCore.textDrawerAcceptHandler(index, style, codepoint, args)){
            this.x+=((xUpdater.floatValue() - this.x));
            cir.setReturnValue(true);
        }
    }
}
