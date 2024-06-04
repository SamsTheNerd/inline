package com.samsthenerd.inline.tooltips.components;

import java.util.function.BiFunction;

import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.tooltips.data.SpriteTooltipData;
import com.samsthenerd.inline.utils.Spritelike;
import com.samsthenerd.inline.utils.SpritelikeRenderers;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SpriteTooltipComponent implements TooltipComponent {
    public static final float DEFAULT_RENDER_SIZE = 96f;

    private Spritelike sprite;
    private BiFunction<Integer, Integer, Integer> widthProvider;

    public SpriteTooltipComponent(SpriteTooltipData tt) {
        this.sprite = tt.sprite;
        this.widthProvider = tt.widthProvider;
    }

    @Override
    public void drawItems(TextRenderer font, int mouseX, int mouseY, DrawContext context) {
        // reload it just incase it failed the first time or whatever ?
        Identifier textureId = sprite.getTextureId();
        if(textureId == null || textureId.equals(new Identifier(""))){
            return;
        }
        MatrixStack ps = context.getMatrices();
        ps.push();
        ps.translate(mouseX, mouseY, 500);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        
        SpritelikeRenderers.getRenderer(sprite).drawSprite(sprite, context, 0, 0, 0, getWidth(font), getRenderHeight());


        // float scale = ((float)getWidth(font)) / sprite.getSpriteWidth();
        // ps.scale(scale, scale, 1f);
        // context.drawTexture(textureId, 0, 0, 0, 0, 0, texture.getWidth(), texture.getHeight(), texture.getWidth(), texture.getHeight());

        ps.pop();
    }

    @Override
    public int getWidth(TextRenderer pFont) {
        return widthProvider.apply(
            (int) ((sprite.getMaxU()-sprite.getMinU()) * sprite.getTextureWidth()),
            (int) ((sprite.getMaxV()-sprite.getMinV()) * sprite.getTextureHeight())
        );
    }

    private int getRenderHeight(){
        int realWidth = widthProvider.apply(
            (int) ((sprite.getMaxU()-sprite.getMinU()) * sprite.getTextureWidth()),
            (int) ((sprite.getMaxV()-sprite.getMinV()) * sprite.getTextureHeight())
        );
        if(realWidth == 0 || sprite.getTextureWidth() == 0){
            return 0;
        }
        return (int)(realWidth * ((double)sprite.getTextureHeight())/sprite.getTextureWidth());
    }

    @Override
    public int getHeight() {
        return getRenderHeight() + 4;
    }
}
