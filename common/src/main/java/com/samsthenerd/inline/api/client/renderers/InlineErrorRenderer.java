package com.samsthenerd.inline.api.client.renderers;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.data.ItemInlineData;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

public class InlineErrorRenderer implements InlineRenderer{

    public static final InlineErrorRenderer INSTANCE = new InlineErrorRenderer();
    public static final ItemInlineData ERROR_DATA = new ItemInlineData(new ItemStack(Items.BARRIER));

    @Override
    public Identifier getId(){
        return new Identifier(Inline.MOD_ID, "error");
    }

    @Override
    public int render(InlineData data, DrawContext context, int index, Style style, int codepoint, TextRenderingContext trContext){
        return InlineItemRenderer.INSTANCE.render(ERROR_DATA, context, index, style, codepoint, trContext);
    }

    @Override
    public int charWidth(InlineData data, Style style, int codepoint){
        return InlineItemRenderer.INSTANCE.charWidth(ERROR_DATA, style, codepoint);
    }

    @Override
    public GlowHandling getGlowPreference(InlineData data) { return InlineItemRenderer.INSTANCE.getGlowPreference(ERROR_DATA); }
}
