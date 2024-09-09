package com.samsthenerd.inline.api.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.client.GlowHandling;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.data.SpriteInlineData;
import com.samsthenerd.inline.utils.SpritelikeRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

public class InlineSpriteRenderer implements InlineRenderer<SpriteInlineData>{

    public static final InlineSpriteRenderer INSTANCE = new InlineSpriteRenderer();

    public Identifier getId(){
        return new Identifier(Inline.MOD_ID, "spritelike");
    
    }

    public int render(SpriteInlineData data, DrawContext context, int index, Style style, int codepoint, TextRenderingContext trContext){
        if(data == null || data.sprite == null) return 0;
        float height = data.sprite.getSpriteHeight();
        if(height == 0){
            return 0;
        }
        float width = data.sprite.getSpriteWidth();
        float whRatio = (width / (float)height);
        MatrixStack matrices = context.getMatrices();
        matrices.translate(0, 0, 1);
        RenderSystem.enableDepthTest();
        SpritelikeRenderers.getRenderer(data.sprite).drawSpriteWithLight(data.sprite, context, 0, 0, 0, 8 * width / height,8, trContext.light(), 0xFFFFFFFF);
        return (int)Math.ceil(whRatio * 8);
    }

    public int charWidth(SpriteInlineData data, Style style, int codepoint){
        if(data == null || data.sprite == null) return 0;
        int height = data.sprite.getSpriteHeight();
        if(height == 0){
            return 0;
        }
        return (int)Math.ceil(8.0 * data.sprite.getSpriteWidth() / (float)height);
    }

    // spritelikes aren't really built to handle animated sprites so it's fine.
    @Override
    public GlowHandling getGlowPreference(SpriteInlineData forData){
        return new GlowHandling.Full(forData.sprite.getTextureId().toTranslationKey() + Integer.toHexString(forData.sprite.hashCode()).toLowerCase());
    }
}
