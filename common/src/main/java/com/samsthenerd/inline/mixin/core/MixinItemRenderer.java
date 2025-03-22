package com.samsthenerd.inline.mixin.core;

import com.samsthenerd.inline.impl.TextItemRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MatrixUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer implements TextItemRenderer {
    @Shadow @Final private static ModelIdentifier TRIDENT;
    @Shadow @Final private static ModelIdentifier SPYGLASS;
    @Shadow @Final private ItemModels models;
    @Shadow @Final private BuiltinModelItemRenderer builtinModelItemRenderer;

    @Shadow protected abstract void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices);

    @Shadow
    private static boolean usesDynamicDisplay(ItemStack stack) {
        return false;
    }

    @Override
    public void inline$renderTextItem(
      ItemStack stack,
      ModelTransformationMode renderMode,
      boolean leftHanded,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay,
      BakedModel model
    ) {
        if (!stack.isEmpty()) {
            matrices.push();
            boolean bl = renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED;
            if (bl) {
                if (stack.isOf(Items.TRIDENT)) {
                    model = this.models.getModelManager().getModel(TRIDENT);
                } else if (stack.isOf(Items.SPYGLASS)) {
                    model = this.models.getModelManager().getModel(SPYGLASS);
                }
            }

            model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
            matrices.translate(-0.5F, -0.5F, -0.5F);
            if (!model.isBuiltin() && (!stack.isOf(Items.TRIDENT) || bl)) {
                boolean bl2;
                if (renderMode != ModelTransformationMode.GUI && !renderMode.isFirstPerson() && stack.getItem() instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    bl2 = !(block instanceof TranslucentBlock) && !(block instanceof StainedGlassPaneBlock);
                } else {
                    bl2 = true;
                }

                RenderLayer renderLayer = RenderLayer.getText(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
                VertexConsumer vertexConsumer;
                if (usesDynamicDisplay(stack) && stack.hasGlint()) {
                    MatrixStack.Entry entry = matrices.peek().copy();
                    if (renderMode == ModelTransformationMode.GUI) {
                        MatrixUtil.scale(entry.getPositionMatrix(), 0.5F);
                    } else if (renderMode.isFirstPerson()) {
                        MatrixUtil.scale(entry.getPositionMatrix(), 0.75F);
                    }

                    vertexConsumer = ItemRenderer.getDynamicDisplayGlintConsumer(vertexConsumers, renderLayer, entry);
                } else if (bl2) {
                    vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
                } else {
                    vertexConsumer = ItemRenderer.getItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
                }

                this.renderBakedItemModel(model, stack, light, overlay, matrices, vertexConsumer);
            } else {
                this.builtinModelItemRenderer.render(stack, renderMode, matrices, vertexConsumers, light, overlay);
            }

            matrices.pop();
        }
    }
}
