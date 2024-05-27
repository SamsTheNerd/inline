package com.samsthenerd.inline.api;

import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;


public interface InlineRenderer<D extends InlineData> {

    public Identifier getId();

    public int render(D data, DrawContext context, int index, Style style, int codepoint, TextRenderingContext trContext);

    public int charWidth(D data, Style style, int codepoint);

    // Inline will try to handle outlines such as on signs and whatnot for you. return true here if you want to handle that yourself
    default public boolean canBeTrustedWithOutlines() {
        return false;
    }

    /*
     * Text by default is 7px tall, and then 1px of shadow
     */
    public static final int DEFAULT_FONT_COLOR = 0xFFFFFF;
    public static final int DEFAULT_SHADOW_COLOR = 0x3e3e3e;

    public static class TextRenderingContext{
        public int light;
        public boolean shadow;
        public float brightnessMultiplier;
        public float red;
        public float green;
        public float blue;
        public float alpha;
        public TextLayerType layerType;
        public VertexConsumerProvider vertexConsumers;

        public TextRenderingContext(int light, boolean shadow, float brightnessMultiplier, float red, float green, float blue, float alpha, TextLayerType layerType, VertexConsumerProvider vertexConsumers){
            this.light = light;
            this.shadow = shadow;
            this.brightnessMultiplier = brightnessMultiplier;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.layerType = layerType;
            this.vertexConsumers = vertexConsumers;
        }
    }
}
