package com.samsthenerd.inline.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.GlowHandling;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.mixin.core.MixinSetTessBuffer;
import com.samsthenerd.inline.utils.*;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
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
import net.minecraft.util.Pair;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

public class InlineRenderCore {

    private static SimpleFramebuffer GLOW_BUFF = new SimpleFramebuffer(128, 128, true, MinecraftClient.IS_SYSTEM_MAC);

    public static boolean textDrawerAcceptHandler(int index, Style style, int codepoint, RenderArgs args) {
        InlineData inlData = style.getInlineData();
        if(inlData == null) return false;
        InlineRenderer renderer = InlineClientAPI.INSTANCE.getRenderer(inlData.getRendererId());
        if(renderer == null) return false;

        if(!(renderer.getGlowPreference(inlData) instanceof GlowHandling.None) && style.getComponent(InlineStyle.GLOWY_MARKER_COMP)){
            return true;
        }
        int glowColor = style.getComponent(InlineStyle.GLOWY_PARENT_COMP);
        boolean needsGlowChildren = glowColor != -1 && renderer.getGlowPreference(inlData) instanceof GlowHandling.Full;

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
        // there's almost certainly a better way to do this, but we're just flipping the y and z axes
        matrices.peek().getNormalMatrix().mul(new Matrix3f(1, 0, 0, 0, 0, 1, 0, 1, 0));
        matrices.multiplyPositionMatrix(new Matrix4f().scale(1f, 1f, 0.001f));
        matrices.translate(args.x(), args.y(), 0);

        // only handle sizing here if sizing exists, renderer won't handle it, and player config says it's ok
        double maxSizeMod = InlineClientAPI.INSTANCE.getConfig().maxChatSizeModifier();
        if(sizeMod > maxSizeMod && com.samsthenerd.inline.api.client.InlineRenderer.isFlat(matrices, args.layerType) && com.samsthenerd.inline.api.client.InlineRenderer.isChatty()) sizeMod = maxSizeMod;

        boolean needToHandleSize = sizeMod != 1.0 && !renderer.handleOwnSizing(inlData);

        if(needToHandleSize){
            double yOffset = (sizeMod - 1) * 4; // sizeMod - 1 gives how much goes "outside" the main 8px. scale by 8 and then take half, so x4.
            matrices.translate(0, -yOffset, 0);
            matrices.scale((float)sizeMod, (float)sizeMod, 1f);
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

        if(!renderer.handleOwnTransparency(inlData)){
            RenderSystem.setShaderColor(1, 1, 1, alphaToUse);
        }

        if(needsGlowChildren){
            Pair<Spritelike, Runnable> texResult = getGlowTextureSprite(inlData, renderer, immToUse, sizeMod, index, style, codepoint, trContext);
            Spritelike backSprite = texResult.getLeft();
            int brighterGlow = ColorUtils.ARGBtoHSB(glowColor)[2] > ColorUtils.ARGBtoHSB(usableColor)[2] ? glowColor : usableColor;
            SpritelikeRenderers.getRenderer(backSprite).drawSpriteWithLight(backSprite, drawContext, -2, -4, 0, 16, 16, trContext.light(), brighterGlow);
            texResult.getRight().run(); // cleanup if needed
        } else {
        }
        matrices.translate(0, 0, 10);
            args.xUpdater().addAndGet(renderer.render(inlData, drawContext, index, style, codepoint, trContext) * (needToHandleSize ? (float)sizeMod : 1f));


        immToUse.draw();

        if(!renderer.handleOwnTransparency(inlData)){
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        matrices.pop();

        MixinSetTessBuffer.setInstance(heldTess);

        return true;
    }

    private static final Cache<String, Spritelike> GLOW_TEXTURE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100) // should be a decent size ?
            .expireAfterAccess(Duration.ofMinutes(5))
            .removalListener((notif) -> {
                if(notif.getValue() instanceof TextureSprite tSprite){
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(tSprite.getTextureId());
                }
            })
            .build();

    private static Pair<Spritelike, Runnable> getGlowTextureSprite(InlineData inlData, InlineRenderer renderer, VertexConsumerProvider.Immediate immToUse,
                                                                   double sizeMod, int index, Style style, int codepoint, InlineRenderer.TextRenderingContext trContext){

        double outlineScaleBack = 1 / sizeMod;
        boolean needToHandleSize = sizeMod != 1.0 && !renderer.handleOwnSizing(inlData);

        // this should never happen, i'm just casting
        if(!(renderer.getGlowPreference(inlData) instanceof GlowHandling.Full fullHandling)) return null;

        String texCacheId = null;
        if(fullHandling.cacheId != null){
            texCacheId = "inlineglowtexture" + renderer.getId().toTranslationKey() + "." + fullHandling.cacheId + sizeMod; // should sizemod even be here ?
            Spritelike texSpriteMaybe = GLOW_TEXTURE_CACHE.getIfPresent(texCacheId);
            if(texSpriteMaybe != null){
                return new Pair<>(texSpriteMaybe, () -> {});
            }
        }

        int resScale = 8;
        GLOW_BUFF.setClearColor(0, 0, 0, 0);
        GLOW_BUFF.clear(false);
        MinecraftClient.getInstance().getFramebuffer().endWrite();
        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        Matrix4f backupProjMatrix = RenderSystem.getProjectionMatrix();
        mvStack.pushMatrix();
        mvStack.identity();
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

        mvStack.popMatrix();
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
            // would
            Queue<Integer> pixelQueue = new LinkedList<>(); // bfs guarantees that we only hit each one once
            Int2IntMap seenPixels = new Int2IntOpenHashMap(); // x + y * width -> distance from an original
            // do an initial sweep for starting ones
            int imgWidth = nativeImage.getWidth();
            int imgHeight = nativeImage.getHeight();
            for(int px = 0; px < imgWidth; px++){
                for(int py = 0; py < imgHeight; py++){
                    if(ColorHelper.Argb.getAlpha(nativeImage.getColor(px, py)) == 0) continue;
                    int thisPos = px + py *imgWidth;
                    seenPixels.put(thisPos, 0);
                    pixelQueue.add(thisPos);
                }
            }
            nativeImage.close();
            while(!pixelQueue.isEmpty()){
                int cPix = pixelQueue.poll(); // get current pixel to process;
                int cX = cPix % imgWidth;
                int cY = cPix / imgWidth;
                fullImage.setColor(cX, cY, 0xFF_FFFFFF);
                if(seenPixels.get(cPix) >= outlineRange) continue; // exit out if we don't need to add neighbors
                for(int i = -1; i <= 1; i++){
                    if( cX + i < 0 || cX + i >= imgWidth) continue;
                    for(int j = -1; j <= 1; j++){
                        if( cY + j < 0 || cY + j >= imgHeight) continue;
                        int nbrPos = cPix + i + j * imgWidth;
                        if(!seenPixels.containsKey(nbrPos)){
                            seenPixels.put(nbrPos, seenPixels.get(cPix)+1);
                            pixelQueue.add(nbrPos);
                        }
                    }
                }
            }

            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            if(texCacheId != null){
                Identifier backTexId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(
                        texCacheId,
                        new NativeImageBackedTexture(fullImage));
                TextureSprite tSprite = new TextureSprite(backTexId);
                GLOW_TEXTURE_CACHE.put(texCacheId, tSprite);
                return new Pair<>(tSprite, () -> {});
            } else {
                Identifier backTexId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(Inline.id("glowtextureback").toTranslationKey(), new NativeImageBackedTexture(fullImage));
                return new Pair<>(new TextureSprite(backTexId), () -> {
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(backTexId);
                });
            }
        } catch (Exception e){
            Inline.LOGGER.error(e.toString());
        }
        return null;
    }

    private static final Tessellator secondaryTess = new Tessellator();

    public record RenderArgs(float x, float y, Matrix4f matrix, int light, boolean shadow, float brightnessMultiplier,
                                    float red, float green, float blue, float alpha, TextRenderer.TextLayerType layerType,
                                    VertexConsumerProvider provider, AtomicDouble xUpdater){}
}
