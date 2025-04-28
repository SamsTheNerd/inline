package com.samsthenerd.inline.impl.extrahooks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.api.client.extrahooks.ItemOverlayRenderer;
import com.samsthenerd.inline.utils.VCPImmediateButImLyingAboutIt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

import java.util.*;

public class ItemOverlayManager {
    private static final Map<Item, List<ItemOverlayRenderer>> ITEM_RENDERERS = new HashMap<>();
    private static final Set<ItemOverlayRenderer> GLOBAL_RENDERERS = new HashSet<>();

    public static List<ItemOverlayRenderer> getRenderers(Item item){
        var renderers = new ArrayList<>(ITEM_RENDERERS.getOrDefault(item, List.of()));
        renderers.addAll(GLOBAL_RENDERERS);
        return renderers;
    }

    public static void addRenderer(Item item, ItemOverlayRenderer renderer){
        ITEM_RENDERERS.computeIfAbsent(item, it -> new ArrayList<>()).add(renderer);
    }

    public static void addRenderer(ItemOverlayRenderer renderer){
        GLOBAL_RENDERERS.add(renderer);
    }

    public static void removeRenderer(ItemOverlayRenderer renderer){
        GLOBAL_RENDERERS.remove(renderer);
    }

    public static void renderDetailTexture(ItemStack stack, MatrixStack matrices,
                                           ModelTransformationMode renderMode, VertexConsumerProvider vertexConsumers,
                                           int light, int overlay, boolean leftHanded){
        if(renderMode != ModelTransformationMode.GUI) return;
        var renderers = ItemOverlayManager.getRenderers(stack.getItem());
        for(var overlayRenderer : renderers) {

            if(!overlayRenderer.isActive(stack)) continue;

            boolean overItem = overlayRenderer.renderInFront(stack);
            VertexConsumerProvider.Immediate immVC;
            if (overItem && vertexConsumers instanceof VertexConsumerProvider.Immediate immediateVCs) {
                RenderSystem.disableDepthTest();
                immediateVCs.draw();
                RenderSystem.enableDepthTest();
                immVC = immediateVCs;
            } else {
                immVC = VCPImmediateButImLyingAboutIt.of(vertexConsumers);
            }

            DrawContext drawCtx = new DrawContext(MinecraftClient.getInstance(), immVC);
            MatrixStack ctxMat = drawCtx.getMatrices();
            ctxMat.push();
            ctxMat.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
            ctxMat.scale(1f/16, -1f/16, 1f/16);
            ctxMat.translate(0, -16, overItem ? 10 : -100);
            overlayRenderer.render(stack, drawCtx);
            ctxMat.pop();
        }
    }
}
