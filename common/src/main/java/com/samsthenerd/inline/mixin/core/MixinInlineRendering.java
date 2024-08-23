package com.samsthenerd.inline.mixin.core;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.impl.InlineRenderCore;
import com.samsthenerd.inline.utils.VCPImmediateButImLyingAboutIt;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.client.InlineRenderer.TextRenderingContext;
import com.samsthenerd.inline.impl.InlineStyle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;

@Mixin( targets = "net.minecraft.client.font.TextRenderer$Drawer")
public class MixinInlineRendering {
    @Shadow
	float x;
	@Shadow
	float y;
    @Shadow
    private Matrix4f matrix;

    @Shadow
    @Final
    private int light;

    @Shadow
    @Final
    private boolean shadow;
    @Shadow
    @Final
    private float brightnessMultiplier;
    @Shadow
    @Final
    private float red;
    @Shadow
    @Final
    private float green;
    @Shadow
    @Final
    private float blue;
    @Shadow
    @Final
    private float alpha;

    @Shadow
    @Final
    private TextLayerType layerType;

    @Shadow
    @Final
    VertexConsumerProvider vertexConsumers;


    @Inject(method = "accept(ILnet/minecraft/text/Style;I)Z", at = @At("HEAD"), cancellable = true)
	private void InlineRenderDrawerAccept(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        AtomicDouble xUpdater = new AtomicDouble(x);
        InlineRenderCore.RenderArgs args = new InlineRenderCore.RenderArgs(x, y, matrix, light, shadow, brightnessMultiplier,
                red, green, blue, alpha, layerType, vertexConsumers, xUpdater);
        if(InlineRenderCore.textDrawerAcceptHandler(index, style, codepoint, args)){
            this.x = xUpdater.floatValue();
            cir.setReturnValue(true);
        }
    }
}

