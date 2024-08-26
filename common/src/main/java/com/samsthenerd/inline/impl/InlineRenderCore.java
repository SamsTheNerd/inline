package com.samsthenerd.inline.impl;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.mixin.core.MixinSetTessBuffer;
import com.samsthenerd.inline.utils.VCPImmediateButImLyingAboutIt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

public class InlineRenderCore {
    // returns if it handled stuff
    public static boolean textDrawerAcceptHandler(int index, Style style, int codepoint, RenderArgs args) {
        InlineStyle inlStyle = (InlineStyle) style;
        InlineData inlData = inlStyle.getInlineData();
        if(inlData == null){
            return false;
        }
        InlineRenderer renderer = InlineClientAPI.INSTANCE.getRenderer(inlData.getRendererId());
        if(renderer == null){
            return false;
        }

        boolean needsGlowHelp = inlStyle.getComponent(InlineStyle.GLOWY_MARKER_COMP) && !renderer.canBeTrustedWithOutlines();

        Tessellator heldTess = Tessellator.getInstance();
        MixinSetTessBuffer.setInstance(secondaryTess);

        VertexConsumerProvider.Immediate immToUse = null;

        // do this to clear whatever buffer is in there.
        if(args.provider() instanceof VertexConsumerProvider.Immediate imm){
            imm.draw();
            immToUse = imm;
        } else {
            // force the given vc into an immediate wrapper just so that we can still pass it through if needed.
            immToUse = VCPImmediateButImLyingAboutIt.of(args.provider());
        }

        DrawContext drawContext = new DrawContext(MinecraftClient.getInstance(), immToUse);

        MatrixStack matrices = drawContext.getMatrices();

        matrices.push();

        double sizeMod = style.getComponent(InlineStyle.SIZE_MODIFIER_COMP);

        matrices.multiplyPositionMatrix(args.matrix());
        matrices.multiplyPositionMatrix(new Matrix4f().scale(1f, 1f, 0.001f));
        matrices.translate(args.x(), args.y(), needsGlowHelp ? 0 : 500);

        // only handle sizing here if sizing exists, renderer won't handle it, and player config says it's ok
        double maxSizeMod = InlineClientAPI.INSTANCE.getConfig().maxChatSizeModifier();
        if(sizeMod > maxSizeMod && InlineRenderer.isFlat(matrices, args.layerType) && InlineRenderer.isChatty()) sizeMod = maxSizeMod;

        boolean needToHandleSize = sizeMod != 1.0 && !renderer.handleOwnSizing();

        if(needToHandleSize){
            double yOffset = (sizeMod - 1) * 4; // sizeMod - 1 gives how much goes "outside" the main 8px. scale by 8 and then take half, so x4.
            matrices.translate(0, -yOffset, 0);
            matrices.scale((float)sizeMod, (float)sizeMod, 1f);
        }

        int rendererARGB = ColorHelper.Argb.getArgb(
                Math.round((args.alpha() == 0 ? 1 : args.alpha()) * 255),
                Math.round(args.red() * 255),
                Math.round(args.green() * 255),
                Math.round(args.blue() * 255)
        );
        int usableColor = rendererARGB;
        if(style.getColor() != null){
            usableColor = ColorHelper.Argb.mixColor(rendererARGB, style.getColor().getRgb() | 0xFF_000000);
        }

        InlineRenderer.TextRenderingContext trContext = new InlineRenderer.TextRenderingContext(args.light(), args.shadow(), args.brightnessMultiplier(),
                args.red(), args.green(), args.blue(), args.alpha() == 0 ? 1 : args.alpha(), args.layerType(), args.provider(), inlStyle.getComponent(InlineStyle.GLOWY_MARKER_COMP),
                inlStyle.getComponent(InlineStyle.GLOWY_PARENT_COMP), usableColor);

        float[] prevColors = RenderSystem.getShaderColor();

        if(!renderer.handleOwnColor() || !renderer.handleOwnTransparency()){
            float[] colorToUse = new float[]{args.red(), args.green(), args.blue(), trContext.alpha()};
            colorToUse[0] = ColorHelper.Argb.getRed(usableColor)/255f;
            colorToUse[1] = ColorHelper.Argb.getGreen(usableColor)/255f;
            colorToUse[2] = ColorHelper.Argb.getBlue(usableColor)/255f;
            RenderSystem.setShaderColor(
                    renderer.handleOwnColor() ? prevColors[0] : colorToUse[0],
                    renderer.handleOwnColor() ? prevColors[1] : colorToUse[1],
                    renderer.handleOwnColor() ? prevColors[2] : colorToUse[2],
                    renderer.handleOwnTransparency() ? prevColors[3] : colorToUse[3]
            );
        }

        args.xUpdater().addAndGet(renderer.render(inlData, drawContext, index, style, codepoint, trContext) * (needToHandleSize ? (float)sizeMod : 1f));

        if(trContext.vertexConsumers() instanceof VertexConsumerProvider.Immediate imm){
            imm.draw();
        }

        if(!renderer.handleOwnColor() || !renderer.handleOwnTransparency()){
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        matrices.pop();

        MixinSetTessBuffer.setInstance(heldTess);

        return true;
    }

    private static final Tessellator secondaryTess = new Tessellator();

    public record RenderArgs(float x, float y, Matrix4f matrix, int light, boolean shadow, float brightnessMultiplier,
                                    float red, float green, float blue, float alpha, TextRenderer.TextLayerType layerType,
                                    VertexConsumerProvider provider, AtomicDouble xUpdater){}
}
