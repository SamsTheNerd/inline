package com.samsthenerd.inline.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineSpriteRenderer;
import com.samsthenerd.inline.api.client.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.api.data.ModIconData;

import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

/**
 * Renders in place of text based on the InlineData attached to the text.
 * <p>
 * You can either directly implement this interface or build off of an existing
 * renderer either by inheritance or composition. If you make a new renderer, you'll 
 * want to register it with {@link InlineClientAPI#addRenderer}.
 * <p>
 * Inline comes with the following built-in core renderers:
 * <ul>
 *      <li> {@link InlineSpriteRenderer}: renders a texture, can be a local resource or from a url </li>
 *      <li> {@link InlineItemRenderer}: renders an itemstack </li>
 *      <li> {@link InlineEntityRenderer}: renders an entity </li>
 * </ul>
 * <p>
 * Check out {@link PlayerHeadRenderer} for an example of extending by composition and 
 * {@link ModIconData} for an example of re-using an existing renderer by extending the data class.
 */
public interface InlineRenderer<D extends InlineData<D>> {

    /**
     * Gets this renderer's ID. Used primarily by the InlineData to specify
     * which renderer to use for it.
     * @return the id
     */
    public Identifier getId();

    /**
     * Renders in place of a single codepoint/character based on the data given.
     * @param data the data to render.
     * @param context a {@link DrawContext} with a {@link MatrixStack} set to the correct
     * position for this character and a fresh {@link Tessellator}.
     * @param index the index of this character in the overall string.
     * @param style the style attached to the text.
     * @param codepoint the unicode codepoint for this character.
     * @param trContext a collection of values taken from the text renderer. 
     * @return the width that this render takes up. more or less corresponds to pixels in the default font.
     */
    public int render(D data, DrawContext context, int index, Style style, int codepoint, TextRenderingContext trContext);

    /**
     * Gets the width of the render without doing the rendering.
     * @param data the data to render.
     * @param style the style attached to the text.
     * @param codepoint the unicode codepoint for this character.
     * @return the width that this render takes up. more or less corresponds to pixels in the default font.
     */
    public int charWidth(D data, Style style, int codepoint);

    /**
     * Gets whether this renderer handles the glow ink on sign outline
     * effect or not.
     * The default handling primarily affects z-values.
     */
    default boolean canBeTrustedWithOutlines() {
        return false;
    }

    /**
     * Indicates if this renderer wants to handle size modifiers on its own.
     * The default handling should be fine for most cases as it simply scales the matrix stack before passing
     * to the renderer.
     */
    default boolean handleOwnSizing(){return false;}

    /**
     * Indicates if this renderer will handle transparency/alpha.
     * Note that the default handling is done with {@link RenderSystem#setShaderColor}.
     */
    default boolean handleOwnTransparency(){return false;}

    /**
     * Indicates if this renderer will handle rgb color.
     * Note that the default handling is done with {@link RenderSystem#setShaderColor}.
     */
    default boolean handleOwnColor(){return true; }

    /*
     * Text by default is 7px tall, and then 1px of shadow
     */
    public static final int DEFAULT_FONT_COLOR = 0xFFFFFF;
    public static final int DEFAULT_SHADOW_COLOR = 0x3e3e3e;

    /**
     * A collection of values taken from the text renderer. 
     * 
     * Notably the argb color of the text (mostly useful for the transparency), 
     * whether or not it's the drop shadow, and the {@link VertexConsumerProvider} 
     * that came from the text renderer.
     */
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
        // whether or not this render is part of the sign glow outline.
        public boolean isGlowy;

        public TextRenderingContext(int light, boolean shadow, float brightnessMultiplier, float red, float green, float blue, float alpha, TextLayerType layerType, VertexConsumerProvider vertexConsumers, boolean isGlowy){
            this.light = light;
            this.shadow = shadow;
            this.brightnessMultiplier = brightnessMultiplier;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.layerType = layerType;
            this.vertexConsumers = vertexConsumers;
            this.isGlowy = isGlowy;
        }
    }
}
