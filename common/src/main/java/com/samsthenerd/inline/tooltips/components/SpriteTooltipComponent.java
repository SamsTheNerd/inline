package com.samsthenerd.inline.tooltips.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.tooltips.data.SpriteTooltipData;
import com.samsthenerd.inline.utils.SpriteUVRegion;
import com.samsthenerd.inline.utils.Spritelike;
import com.samsthenerd.inline.utils.SpritelikeRenderers;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;

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
        if(textureId == null || textureId.equals(Identifier.of(""))){
            return;
        }
        MatrixStack ps = context.getMatrices();
        ps.push();
        ps.translate(mouseX, mouseY, 500);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        
        SpritelikeRenderers.getRenderer(sprite).drawSprite(sprite, context, 0, 0, 0, getWidth(font), getRenderHeight());

        ps.pop();
    }

    @Override
    public int getWidth(TextRenderer pFont) {
        SpriteUVRegion uvs = sprite.getUVs();
        return widthProvider.apply(
            (int) (uvs.uWidth() * sprite.getTextureWidth()),
            (int) (uvs.vHeight() * sprite.getTextureHeight())
        );
    }

    private int getRenderHeight(){
        SpriteUVRegion uvs = sprite.getUVs();
        int realWidth = widthProvider.apply(
            (int) (uvs.uWidth() * sprite.getTextureWidth()),
            (int) (uvs.vHeight() * sprite.getTextureHeight())
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
