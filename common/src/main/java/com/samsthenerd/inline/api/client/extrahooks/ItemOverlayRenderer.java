package com.samsthenerd.inline.api.client.extrahooks;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public interface ItemOverlayRenderer {
    /**
     * Renders over an item in a GUI.
     * @param stack itemstack to render over
     * @param drawContext provides a useable vertex consumer and a matrixstack
     *                    positioned at the top left of the item slot.
     */
    void render(ItemStack stack, DrawContext drawContext);

    /**
     * If this renderer will currently render anything with this stack.
     */
    default boolean isActive(ItemStack stack){
        return true;
    }
}
