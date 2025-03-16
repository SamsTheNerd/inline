package com.samsthenerd.inline.impl.extrahooks;

import com.samsthenerd.inline.api.client.extrahooks.ItemOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemOverlayManager {
    private static final Map<Item, List<ItemOverlayRenderer>> RENDERERS = new HashMap<>();

    public static List<ItemOverlayRenderer> getRenderers(Item item){
        return RENDERERS.getOrDefault(item, List.of());
    }

    public static void addRenderer(Item item, ItemOverlayRenderer renderer){
        RENDERERS.computeIfAbsent(item, it -> new ArrayList<>()).add(renderer);
    }
}
