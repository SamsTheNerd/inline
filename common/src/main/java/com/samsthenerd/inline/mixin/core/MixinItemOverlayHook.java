package com.samsthenerd.inline.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.inline.impl.extrahooks.ItemOverlayManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
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
    public void renderDetailTextureBakedModel(ItemRenderer renderer, BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices,
                                    VertexConsumer vertices, Operation<Void> original, ItemStack stackEnclosing, ModelTransformationMode renderMode,
                                    boolean leftHanded, MatrixStack matricesEnclosing, VertexConsumerProvider vcp){
        original.call(renderer, model, stack, light, overlay, matrices, vertices);
        ItemOverlayManager.renderDetailTexture(stack, matrices, renderMode, vcp, light, overlay, leftHanded);
    }

    @WrapOperation(
        method="renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
        at=@At(value="INVOKE", target="Lnet/minecraft/client/render/item/BuiltinModelItemRenderer;render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
    )
    public void renderDetailTextureBuiltInModel(BuiltinModelItemRenderer renderer, ItemStack stack, ModelTransformationMode renderMode,
                                                MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay,
                                                Operation<Void> original, ItemStack stackEnclosing, ModelTransformationMode renderModeEncl,
                                                boolean leftHanded, MatrixStack matricesEnclosing, VertexConsumerProvider vcpEncl) {
        original.call(renderer, stack, renderMode, matrices, vcp, light, overlay);
        matricesEnclosing.pop();
        matricesEnclosing.push();
        matrices.translate(-0.5F, -0.5F, -0.5F);
        ItemOverlayManager.renderDetailTexture(stack, matricesEnclosing, renderMode, vcp, light, overlay, leftHanded);
    }
}
