package com.samsthenerd.inline.mixin.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
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

    private static final Tessellator secondaryTess = new Tessellator();


    @Inject(method = "accept(ILnet/minecraft/text/Style;I)Z", at = @At("HEAD"), cancellable = true)
	private void PatStyDrawerAccept(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        InlineStyle inlStyle = (InlineStyle) style;
        InlineData inlData = inlStyle.getInlineData();
        if(inlData == null){
            return;
        }
        InlineRenderer renderer = InlineClientAPI.INSTANCE.getRenderer(inlData.getRendererId());
        if(renderer == null){
            return;
        }

        boolean needsGlowHelp = inlStyle.getComponent(InlineStyle.GLOWY_MARKER_COMP) && !renderer.canBeTrustedWithOutlines();

        Tessellator heldTess = Tessellator.getInstance();

        MixinSetTessBuffer.setInstance(secondaryTess);

        DrawContext drawContext = new DrawContext(MinecraftClient.getInstance(), VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer()));
        
        MatrixStack matrices = drawContext.getMatrices();

        matrices.push();

        double sizeMod = style.getComponent(InlineStyle.SIZE_MODIFIER_COMP);

        matrices.multiplyPositionMatrix(matrix);
        matrices.multiplyPositionMatrix(new Matrix4f().scale(1f, 1f, 0.001f));
        matrices.translate(x, y, needsGlowHelp ? 0 : 500);
        if(!renderer.handleOwnSizing()){
            double yOffset = (sizeMod - 1) * 4; // sizeMod - 1 gives how much goes "outside" the main 8px. scale by 8 and then take half, so x4.
            matrices.translate(0, -yOffset, 0);
            matrices.scale((float)sizeMod, (float)sizeMod, 1f);
        }

        TextRenderingContext trContext = new InlineRenderer.TextRenderingContext(light, shadow, brightnessMultiplier, 
            red, green, blue, alpha == 0 ? 1 : alpha, layerType, vertexConsumers, inlStyle.getComponent(InlineStyle.GLOWY_MARKER_COMP));


        // do this to clear whatever buffer is in there.
        if(trContext.vertexConsumers instanceof VertexConsumerProvider.Immediate imm){
            imm.draw();
        }

        float[] prevColors = RenderSystem.getShaderColor();

        if(!renderer.handleOwnColor() || !renderer.handleOwnTransparency()){
            float[] colorToUse = new float[]{red, green, blue, trContext.alpha};
            if(style.getColor() != null){
                colorToUse[0] = ColorHelper.Argb.getRed(style.getColor().getRgb())/255f;
                colorToUse[1] = ColorHelper.Argb.getGreen(style.getColor().getRgb())/255f;
                colorToUse[2] = ColorHelper.Argb.getBlue(style.getColor().getRgb())/255f;
            }
            RenderSystem.setShaderColor(
                    renderer.handleOwnColor() ? prevColors[0] : colorToUse[0],
                    renderer.handleOwnColor() ? prevColors[1] : colorToUse[1],
                    renderer.handleOwnColor() ? prevColors[2] : colorToUse[2],
                    renderer.handleOwnTransparency() ? prevColors[3] : colorToUse[3]
            );
        }

        x += renderer.render(inlData, drawContext, index, style, codepoint, trContext) * (renderer.handleOwnSizing() ? 1 : (float)sizeMod);

        if(trContext.vertexConsumers instanceof VertexConsumerProvider.Immediate imm){
            imm.draw();
        }

        if(!renderer.handleOwnColor() || !renderer.handleOwnTransparency()){
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        matrices.pop();

        MixinSetTessBuffer.setInstance(heldTess);

        cir.setReturnValue(true);
    }
}

