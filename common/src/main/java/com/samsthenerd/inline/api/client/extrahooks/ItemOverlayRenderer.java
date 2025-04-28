package com.samsthenerd.inline.api.client.extrahooks;

import com.samsthenerd.inline.impl.extrahooks.ItemOverlayManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

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

    /**
     * If this renderer should render in front of or behind the item.
     */
    default boolean renderInFront(ItemStack stack){ return true; }

    /**
     * Registers an overlay renderer for a specific item.
     */
    static void addRenderer(Item item, ItemOverlayRenderer renderer){
        ItemOverlayManager.addRenderer(item, renderer);
    }

    /**
     * Registers an overlay renderer that may be applied to any item.
     * This should be used sparingly as it will be checked for every GUI item
     * rendered! isActive() should be used to filter here as much as possible.
     */
    static void addRenderer(ItemOverlayRenderer renderer){
        ItemOverlayManager.addRenderer(renderer);
    }

    /**
     * Removes the given overlay renderer. This is intended for configurable
     * renderers to avoid running unnecessarily.
     */
    static void removeRenderer(ItemOverlayRenderer renderer){
        ItemOverlayManager.removeRenderer(renderer);
    }
}

