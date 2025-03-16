package com.samsthenerd.inline.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.impl.extrahooks.ItemOverlayManager;
import com.samsthenerd.inline.utils.VCPImmediateButImLyingAboutIt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public class MixinItemOverlayHook {
    @WrapOperation(
        method="renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
        at=@At(value="INVOKE", target="net/minecraft/client/render/item/ItemRenderer.renderBakedItemModel (Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V")
    )
    public void renderDetailTexture(ItemRenderer renderer, BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices,
                                    VertexConsumer vertices, Operation<Void> original, ItemStack stackEnclosing, ModelTransformationMode renderMode,
                                    boolean leftHanded, MatrixStack matricesEnclosing, VertexConsumerProvider vertexConsumers){
        original.call(renderer, model, stack, light, overlay, matrices, vertices);
        if(renderMode != ModelTransformationMode.GUI) return;
        var renderers = ItemOverlayManager.getRenderers(stack.getItem());
        for(var overlayRenderer : renderers) {

            if(!overlayRenderer.isActive(stack)) continue;

            boolean overItem = true; // idk about that one but sure
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
            ctxMat.translate(0, -16, 0);
//            // there's almost certainly a better way to do this, but we're just flipping the y and z axes
//            ctxMat.peek().getNormalMatrix().mul(new Matrix3f(1, 0, 0, 0, 0, 1, 0, 1, 0));
            overlayRenderer.render(stack, drawCtx);

            ctxMat.pop();
        }
    }
}
