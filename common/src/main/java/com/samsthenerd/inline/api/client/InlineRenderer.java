package com.samsthenerd.inline.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.client.renderers.*;
import com.samsthenerd.inline.api.data.ModIconData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.joml.Vector4f;

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
     * position for this character and with a {@link VertexConsumerProvider} for rendering to.
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
     *
     * @return
     */
    default GlowHandling getGlowPreference(D forData){ return new GlowHandling.Full(); }

    /**
     * Indicates if this renderer wants to handle size modifiers on its own.
     * The default handling should be fine for most cases as it simply scales the matrix stack before passing
     * to the renderer.
     */
    default boolean handleOwnSizing(D forData){return false;}

    /**
     * Indicates if this renderer will handle transparency/alpha.
     * Note that the default handling is done with {@link RenderSystem#setShaderColor}.
     */
    default boolean handleOwnTransparency(D forData){return false;}

    /*
     * Text by default is 7px tall, and then 1px of shadow
     */
    int DEFAULT_FONT_COLOR = 0xFFFFFF;
    int DEFAULT_SHADOW_COLOR = 0x3e3e3e;

    /**
     * A collection of values taken from the text renderer. 
     * 
     * Notably the argb color of the text (mostly useful for the transparency), 
     * whether or not it's the drop shadow, and the {@link VertexConsumerProvider} 
     * that came from the text renderer. Note that generally this vc provider will be the same
     * as the one given in the render call's {@link DrawContext}.
     * <p>
     * @param isGlowy indicates if this text *is* an outline.
     * @param outlineColor indicates if the text *has* an outline (that would be rendered separately) and what color it is.
     *                     -1 if no outline.
     * @param usableColor argb value for the color of the text. Takes into account the argb float values here as well as
     *                    the text's style.
     */
    record TextRenderingContext(int light, boolean shadow, float brightnessMultiplier, float red, float green, float blue,
                                float alpha, TextLayerType layerType, VertexConsumerProvider vertexConsumers,
                                boolean isGlowy, int outlineColor, int usableColor){}

    /**
     * A helper method for checking if a given rendering environment is for a flat UI, such as in chat, a tooltip,
     * inventory title, or other similar cases.
     * @param matrices the matrix stack given by the rendering env.
     * @param layerType text layer type for the given env. Can be found with {@link TextRenderingContext#layerType()}
     * @return if the render context, with the given matrix stack, is flat or not.
     */
    static boolean isFlat(MatrixStack matrices, TextLayerType layerType){
        if (layerType == TextRenderer.TextLayerType.NORMAL) {
            Vector4f straightVec = new Vector4f(0, 0, 1, 0);
            straightVec.mul(matrices.peek().getPositionMatrix());
            return straightVec.x() == 0 && straightVec.y() == 0;
        }
        return false;
    }

    /**
     * A small helper for checking if this renderer is being called from a chat renderer.
     * <p>
     * NOTE: since this has to check the call stack, it's maybe not the most efficient thing in the world.
     * Try to limit calls to it. Using {@link TextRenderingContext#isFlat} can
     * @return if this renderer is being called from a chat renderer.
     */
    static boolean isChatty(){
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s ->
                s.limit(20).anyMatch(frame ->
                        frame.getDeclaringClass().equals(ChatScreen.class) || frame.getDeclaringClass().equals(ChatHud.class))
        );
    }
}
