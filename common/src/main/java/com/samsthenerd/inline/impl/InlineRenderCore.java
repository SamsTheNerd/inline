package com.samsthenerd.inline.impl;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.client.renderers.GlowHandling;
import com.samsthenerd.inline.mixin.core.MixinSetTessBuffer;
import com.samsthenerd.inline.utils.ColorUtils;
import com.samsthenerd.inline.utils.SpritelikeRenderers;
import com.samsthenerd.inline.utils.TextureSprite;
import com.samsthenerd.inline.utils.VCPImmediateButImLyingAboutIt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

import java.util.function.IntUnaryOperator;

public class InlineRenderCore {

    private static SimpleFramebuffer GLOW_BUFF = new SimpleFramebuffer(128, 128, true, MinecraftClient.IS_SYSTEM_MAC);

    // TODO: break up this whole thing into
    public static boolean textDrawerAcceptHandler(int index, Style style, int codepoint, RenderArgs args) {
        InlineData inlData = style.getInlineData();
        if(inlData == null) return false;
        InlineRenderer renderer = InlineClientAPI.INSTANCE.getRenderer(inlData.getRendererId());
        if(renderer == null) return false;

        if(!(renderer.getGlowPreference() instanceof GlowHandling.None) && style.getComponent(InlineStyle.GLOWY_MARKER_COMP)){
            return true;
        }
        int glowColor = style.getComponent(InlineStyle.GLOWY_PARENT_COMP);
        boolean needsGlowChildren = glowColor != -1 && renderer.getGlowPreference() instanceof GlowHandling.Full;

        Tessellator heldTess = Tessellator.getInstance();
        MixinSetTessBuffer.setInstance(secondaryTess);

        VertexConsumerProvider.Immediate immToUse = args.provider() instanceof VertexConsumerProvider.Immediate imm
                ? imm : VCPImmediateButImLyingAboutIt.of(args.provider());
        immToUse.draw();

        DrawContext drawContext = new DrawContext(MinecraftClient.getInstance(), immToUse);
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        double sizeMod = style.getComponent(InlineStyle.SIZE_MODIFIER_COMP);

//        matrices.multiply(args.matrix().getUnnormalizedRotation(new Quaternionf()).normalize());
        matrices.multiplyPositionMatrix(args.matrix());
//        matrices.peek().getNormalMatrix().mul(new Matrix3f(args.matrix()));
        matrices.multiplyPositionMatrix(new Matrix4f().scale(1f, 1f, 0.001f));
//        matrices.scale(1f, 1f, 0.001f);
        matrices.translate(args.x(), args.y(), 0);

        // only handle sizing here if sizing exists, renderer won't handle it, and player config says it's ok
        double maxSizeMod = InlineClientAPI.INSTANCE.getConfig().maxChatSizeModifier();
        if(sizeMod > maxSizeMod && InlineRenderer.isFlat(matrices, args.layerType) && InlineRenderer.isChatty()) sizeMod = maxSizeMod;

        boolean needToHandleSize = sizeMod != 1.0 && !renderer.handleOwnSizing();

        double outlineScaleBack = 1;
        if(needToHandleSize){
            double yOffset = (sizeMod - 1) * 4; // sizeMod - 1 gives how much goes "outside" the main 8px. scale by 8 and then take half, so x4.
            matrices.translate(0, -yOffset, 0);
            matrices.scale((float)sizeMod, (float)sizeMod, 1f);
            outlineScaleBack = 1 / sizeMod;
        }

        float alphaToUse = (args.alpha() == 0 ? 1 : args.alpha());

        int rendererARGB = ColorHelper.Argb.getArgb(
                Math.round(alphaToUse* 255), Math.round(args.red() * 255),
                Math.round(args.green() * 255), Math.round(args.blue() * 255)
        );
        int usableColor = rendererARGB;
        if(style.getColor() != null){
            usableColor = ColorHelper.Argb.mixColor(rendererARGB, style.getColor().getRgb() | 0xFF_000000);
        }

        InlineRenderer.TextRenderingContext trContext = new InlineRenderer.TextRenderingContext(args.light(), args.shadow(), args.brightnessMultiplier(),
                args.red(), args.green(), args.blue(), args.alpha() == 0 ? 1 : args.alpha(), args.layerType(), args.provider(), style.getComponent(InlineStyle.GLOWY_MARKER_COMP),
                style.getComponent(InlineStyle.GLOWY_PARENT_COMP), usableColor);

        if(!renderer.handleOwnTransparency()){
            RenderSystem.setShaderColor(1, 1, 1, alphaToUse);
        }

        if(needsGlowChildren){
            int resScale = 8;
            GLOW_BUFF.setClearColor(0, 0, 0, 0);
            GLOW_BUFF.clear(false);
            MinecraftClient.getInstance().getFramebuffer().endWrite();
            MatrixStack mvStack = RenderSystem.getModelViewStack();
            Matrix4f backupProjMatrix = RenderSystem.getProjectionMatrix();
            mvStack.push();
            mvStack.loadIdentity();
            RenderSystem.applyModelViewMatrix();
            VertexSorter backupVertexSorter = RenderSystem.getVertexSorting();
            RenderSystem.backupProjectionMatrix();
            Matrix4f newProjMatrix = new Matrix4f();
            newProjMatrix.identity();
            newProjMatrix.setOrtho(0, 16*resScale, 0,16*resScale, 0, 100);
            RenderSystem.setProjectionMatrix(newProjMatrix, VertexSorter.BY_DISTANCE);
            GLOW_BUFF.beginWrite(true);
            DrawContext glowContext = new DrawContext(MinecraftClient.getInstance(), immToUse);
            MatrixStack glowStack = glowContext.getMatrices();

            glowStack.push();
            glowStack.translate(2*resScale, 4 * resScale, -50);
            glowStack.scale(resScale, resScale, 1f);
            glowStack.multiplyPositionMatrix(new Matrix4f().scale(1, 1, 0.01f));
            float xOffsetDiff = renderer.render(inlData, glowContext, index, style, codepoint, trContext) * (needToHandleSize ? (float)sizeMod : 1f);
//            args.xUpdater().addAndGet(xOffsetDiff);

            immToUse.draw();

            mvStack.pop();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(backupProjMatrix, backupVertexSorter);
            GLOW_BUFF.endWrite();

            try (NativeImage nativeImage = new NativeImage(16*resScale, 16*resScale, true)) {
                GLOW_BUFF.beginRead();
                nativeImage.loadFromTextureImage(0, false);
                GLOW_BUFF.endRead();
//                nativeImage.writeTo(MinecraftClient.getInstance().runDirectory.toPath().resolve("boop.png"));
//                nativeImage.apply(original -> ColorHelper.Argb.getAlpha(original) << 24 | 0x00_FFFFFF);
                NativeImage fullImage = new NativeImage(nativeImage.getWidth(), nativeImage.getHeight(), true);
                int outlineRange = (int)Math.round(resScale * outlineScaleBack);
                for(int px = 0; px < nativeImage.getWidth(); px++){
                    for(int py = 0; py < nativeImage.getHeight(); py++){
                        int pAlph = ColorHelper.Argb.getAlpha(nativeImage.getColor(px, py));
                        if(pAlph > 0){
                            // TODO: this iteration is bad and cringe and very inefficient probably
                            for (int j = -outlineRange; j <= outlineRange; ++j) {
                                if(px + j >= nativeImage.getWidth() || px + j < 0) continue;
                                for (int k = -outlineRange; k <= outlineRange; ++k) {
                                    if(py + k >= nativeImage.getHeight() || py + k < 0) continue;
                                    int prevAlpha = ColorHelper.Argb.getAlpha(fullImage.getColor(px + j, py+k));
                                    if(pAlph > 100 && pAlph > prevAlpha) fullImage.setColor(px + j, py+k, pAlph << 24 | 0x00_FFFFFF);
                                }
                            }
                        }
                    }
                }

                MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
                Identifier backTexId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(new Identifier(Inline.MOD_ID, "glowtextureback").toTranslationKey(), new NativeImageBackedTexture(fullImage));
                TextureSprite backSprite = new TextureSprite(backTexId);
                int brighterGlow = ColorUtils.ARGBtoHSB(glowColor)[2] > ColorUtils.ARGBtoHSB(usableColor)[2] ? glowColor : usableColor;
//                for (int j = -1; j <= 1; ++j) {
//                    for (int k = -1; k <= 1; ++k) {
//                        matrices.push();
//                        if (j == 0 && k == 0){
//                            matrices.translate(0, 0, 100);
////                            SpritelikeRenderers.getRenderer(frontSprite).drawSpriteWithLight(frontSprite, drawContext, 0, -4, 0, 16, 16, trContext.light(), 0xFF_FFFFFF);
//                        } else {
//                            matrices.translate(j * outlineScaleBack, k * outlineScaleBack, 0);
////                            SpritelikeRenderers.getRenderer(backSprite).drawSpriteWithLight(backSprite, drawContext, 0, -4, 0, 16, 16, trContext.light(), (glowColor & 0x00_FFFFFF) | (usableColor & 0xFF_000000));
//                        }
//                        matrices.pop();
//                    }
//                }
                SpritelikeRenderers.getRenderer(backSprite).drawSpriteWithLight(backSprite, drawContext, -2, -4, 0, 16, 16, trContext.light(), brighterGlow);
                MinecraftClient.getInstance().getTextureManager().destroyTexture(backTexId);
            } catch (Exception e){
                Inline.LOGGER.error(e.toString());
            }
        } else {
        }
        matrices.translate(0, 0, 10);
            args.xUpdater().addAndGet(renderer.render(inlData, drawContext, index, style, codepoint, trContext) * (needToHandleSize ? (float)sizeMod : 1f));


        immToUse.draw();

        if(!renderer.handleOwnTransparency()){
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        matrices.pop();

        MixinSetTessBuffer.setInstance(heldTess);

        return true;
    }

    private static IntUnaryOperator getGlowyFunc(int glowColor){
        float[] glowHSB = ColorUtils.ARGBtoHSB(glowColor);
        int glowAlpha = ColorHelper.Argb.getAlpha(glowColor);
        return (original) -> {
            int originalAlpha = ColorHelper.Abgr.getAlpha(original);
            if(originalAlpha == 0) return 0;
            float[] pixelHSB = ColorUtils.ABGRtoHSB(original);
            return ColorUtils.ARGBtoABGR(
                    ColorUtils.HSBtoRGB(
//                            glowHSB[1] > 0 ? glowHSB[0] : pixelHSB[0],
//                            (float)MathHelper.lerp(0, pixelHSB[1], glowHSB[1]), // saturation
//                            Math.min((float)MathHelper.lerp(0.1, pixelHSB[2], glowHSB[2]) + 0.2f, 1) // brightness
                            glowHSB[0], pixelHSB[1], pixelHSB[2]
            ) | ColorHelper.Argb.getArgb(Math.min(originalAlpha, glowAlpha), 0, 0, 0));
        };
    }

    private static final Tessellator secondaryTess = new Tessellator();

    public record RenderArgs(float x, float y, Matrix4f matrix, int light, boolean shadow, float brightnessMultiplier,
                                    float red, float green, float blue, float alpha, TextRenderer.TextLayerType layerType,
                                    VertexConsumerProvider provider, AtomicDouble xUpdater){}
}
