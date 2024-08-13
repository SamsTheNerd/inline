package com.samsthenerd.inline.api.client.renderers;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.data.ItemInlineData;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class InlineItemRenderer implements InlineRenderer<ItemInlineData>{

    public static final InlineItemRenderer INSTANCE = new InlineItemRenderer();

    public Identifier getId(){
        return new Identifier(Inline.MOD_ID, "item");
    }

    public static boolean debugEarlyReturn = true;

    public int render(ItemInlineData data, DrawContext context, int index, Style style, int codepoint, TextRenderingContext trContext){
        // only draw it once
        if(trContext.shadow()){
            return 8;
        }
        MatrixStack matrices = context.getMatrices();
        
        // context.drawItem(data.getStack(), 0, 0);
        // matrices.pop();

        ItemStack stack = data.getStack();
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;

        if (stack.isEmpty()) {
            return 8;
        }
        BakedModel bakedModel = client.getItemRenderer().getModel(stack, world, null, 0);
        boolean flat = !bakedModel.isSideLit();
        /*
         * here we do a bunch of garbage to make lighting work as nicely as possible in-game.
         *
         * the main issue is that DiffuseLighting.disableGuiDepthLighting() messes up the game's lighting but is needed
         * to make an item look Right when rendered in a flat UI.
         *
         * First we check that it's flat and that the layer type is normal (all UI text rendering seems to use this?)
         * Then we check that the position matrix at the top is flat.
         */
        if (flat && trContext.layerType() == TextRenderer.TextLayerType.NORMAL) {
            Vector4f straightVec = new Vector4f(0, 0, 1, 0);
            straightVec.mul(matrices.peek().getPositionMatrix());
            if(straightVec.x() == 0 && straightVec.y() == 0){
                DiffuseLighting.disableGuiDepthLighting();
            }
        }
        matrices.push();
        matrices.translate(4, 4, 0);
        try {
            matrices.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
            matrices.scale(8.0f, 8.0f, 8f);
            client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, matrices, context.getVertexConsumers(), trContext.light(), OverlayTexture.DEFAULT_UV, bakedModel);
            context.getVertexConsumers().draw();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Rendering item");
            CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
            crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
            crashReportSection.add("Item Damage", () -> String.valueOf(stack.getDamage()));
            crashReportSection.add("Item NBT", () -> String.valueOf(stack.getNbt()));
            crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
            throw new CrashException(crashReport);
        }
        matrices.pop();
        return 8;
    }

    public int charWidth(ItemInlineData data, Style style, int codepoint){
        return 8;
    }
}
