package com.samsthenerd.inline.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.utils.Spritelike.SpritelikeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class SpritelikeRenderers {
    private static final Map<SpritelikeType, SpritelikeRenderer> RENDERER_MAP = new HashMap<>();

    private static final SpritelikeRenderer DEFAULT_RENDERER = new SpritelikeRenderer();

    public static void registerRenderer(SpritelikeType type, SpritelikeRenderer renderer){
        RENDERER_MAP.put(type, renderer);
    }

    public static SpritelikeRenderer getRenderer(SpritelikeType type){
        if(RENDERER_MAP.containsKey(type)){
            return RENDERER_MAP.get(type);
        }
        return DEFAULT_RENDERER;
    }

    public static SpritelikeRenderer getRenderer(Spritelike sprite){
        return getRenderer(sprite.getType());
    }

    public static class SpritelikeRenderer{
        public void drawSpriteWithEntityTranslucent(Spritelike sprite, DrawContext ctx, float x, float y, float z, float width, float height, int light, int argb) {
            Identifier texture = sprite.getTextureId();
            if(texture == null) return;
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            Matrix4f matrix4f = ctx.getMatrices().peek().getPositionMatrix();
            RenderLayer renderLayer = RenderLayer.getEntityTranslucent(texture);
            VertexConsumer vc = ctx.getVertexConsumers().getBuffer(renderLayer);
            SpriteUVRegion uvs = sprite.getUVs();
            vc.vertex(matrix4f, x, y, z)
              .color(argb)
              .texture((float)uvs.minU(), (float)uvs.minV())
              .overlay(OverlayTexture.DEFAULT_UV)
              .light(light)
              .normal(ctx.getMatrices().peek(), 0, 0, 1f)
            ;
            vc.vertex(matrix4f, x, y+height, z)
              .color(argb)
              .texture((float)uvs.minU(), (float)uvs.maxV())
              .overlay(OverlayTexture.DEFAULT_UV)
              .light(light)
              .normal(ctx.getMatrices().peek(), 0, 0, 1f)
            ;
            vc.vertex(matrix4f, x+width, y+height, z)
              .color(argb)
              .texture((float)uvs.maxU(), (float)uvs.maxV())
              .overlay(OverlayTexture.DEFAULT_UV)
              .light(light)
              .normal(ctx.getMatrices().peek(), 0, 0, 1f)
            ;
            vc.vertex(matrix4f, x+width, y, z)
              .color(argb)
              .texture((float)uvs.maxU(), (float)uvs.minV())
              .overlay(OverlayTexture.DEFAULT_UV)
              .light(light)
              .normal(ctx.getMatrices().peek(), 0, 0, 1f)
            ;

            ctx.getVertexConsumers().draw(renderLayer);
        }

        public void drawSpriteWithLight(Spritelike sprite, DrawContext ctx, float x, float y, float z, float width, float height, int light, int argb) {
            Identifier texture = sprite.getTextureId();
            if(texture == null) return;
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            Matrix4f matrix4f = ctx.getMatrices().peek().getPositionMatrix();
            RenderLayer renderLayer = RenderLayer.getText(texture);
            VertexConsumer vc = ctx.getVertexConsumers().getBuffer(renderLayer);
            SpriteUVRegion uvs = sprite.getUVs();
            vc.vertex(matrix4f, x, y, z)
              .color(argb)
              .texture((float)uvs.minU(), (float)uvs.minV())
              .light(light)
            ;
            vc.vertex(matrix4f, x, y+height, z)
              .color(argb)
              .texture((float)uvs.minU(), (float)uvs.maxV())
              .light(light)
            ;
            vc.vertex(matrix4f, x+width, y+height, z)
              .color(argb)
              .texture((float)uvs.maxU(), (float)uvs.maxV())
              .light(light)
            ;
            vc.vertex(matrix4f, x+width, y, z)
              .color(argb)
              .texture((float)uvs.maxU(), (float)uvs.minV())
              .light(light)
            ;

            ctx.getVertexConsumers().draw(renderLayer);
        }
        
        public void drawSprite(Spritelike sprite, DrawContext ctx, float x, float y, float z, float width, float height){
            Identifier texture = sprite.getTextureId();
            if(texture == null) return;
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            Matrix4f matrix4f = ctx.getMatrices().peek().getPositionMatrix();
            SpriteUVRegion uvs = sprite.getUVs();
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferBuilder.vertex(matrix4f, x, y, z)
                .texture((float) uvs.minU(), (float) uvs.minV());
            bufferBuilder.vertex(matrix4f, x, y+height, z)
                .texture((float) uvs.minU(), (float) uvs.maxV());
            bufferBuilder.vertex(matrix4f, x+width, y+height, z)
                .texture((float) uvs.maxU(), (float) uvs.maxV());
            bufferBuilder.vertex(matrix4f, x+width, y, z)
                .texture((float) uvs.maxU(), (float) uvs.minV());

            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        }
    }
}
