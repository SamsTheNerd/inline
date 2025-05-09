package com.samsthenerd.inline.tooltips.components;

import com.samsthenerd.inline.tooltips.data.EntityDisplayTTData;
import com.samsthenerd.inline.utils.EntityCradle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;

import java.util.function.BiFunction;

public class EntityDisplayTTComp implements TooltipComponent {
    public static final float DEFAULT_RENDER_SIZE = 96f;

    private EntityCradle cradle;
    private BiFunction<Integer, Integer, Integer> widthProvider;

    public EntityDisplayTTComp(EntityDisplayTTData tt) {
        this.cradle = tt.cradle;
        this.widthProvider = tt.widthProvider;
    }

    @Override
    public void drawItems(TextRenderer font, int mouseX, int mouseY, DrawContext context) {
        // reload it just incase it failed the first time or whatever ?
        Entity ent = cradle.getEntity(MinecraftClient.getInstance().world);
        if(ent == null) return;

        Box bounds = ent.getBoundingBox().expand(0, 0.05, 0);

        double height = bounds.getLengthY();

        float rot = 15f;

        EntityRenderer renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(ent);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        int rHeight = getRenderHeight();
        int ttWidth = getWidth(font);
        matrices.translate(mouseX + ttWidth/2.0, mouseY, 500);
        float scaleFactor = (float)(rHeight/height);
        matrices.scale(scaleFactor, -scaleFactor, scaleFactor);
        matrices.translate(0, -height, 0);
        MinecraftClient client = MinecraftClient.getInstance();
        // float rotation = 90f * (Util.getMeasuringTimeMs() / 1000f + data.getUniqueOffset());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));
        renderer.render(ent, 0, 0, matrices, context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        matrices.pop();
    }

    @Override
    public int getWidth(TextRenderer pFont) {
        Entity ent = cradle.getEntity(MinecraftClient.getInstance().world);
        if(ent == null) return 0;

        // Box bounds = ent.getBoundingBox().expand(0.15, 0.1, 0.15);
        Box bounds = ent.getBoundingBox().expand(0, 0.05, 0);

        double width = bounds.getLengthX();
        double depth = bounds.getLengthZ();
        double height = bounds.getLengthY();

        float rot = 15f;
        double radRot = Math.toRadians(rot % 90);
        double pWidth = (width * Math.cos(radRot)) + (depth * Math.sin(radRot));

        return (int) (widthProvider.apply(
            (int) (pWidth*100),
            (int) (height*100)
        )) + 16;
    }

    private int getRenderHeight(){
        Entity ent = cradle.getEntity(MinecraftClient.getInstance().world);
        if(ent == null) return 0;

        Box bounds = ent.getBoundingBox().expand(0, 0.05, 0);

        double width = bounds.getLengthX();
        double depth = bounds.getLengthZ();
        double height = bounds.getLengthY();

        float rot = 15f;
        double radRot = Math.toRadians(rot % 90);
        double pWidth = (width * Math.cos(radRot)) + (depth * Math.sin(radRot));

        int realWidth = widthProvider.apply(
            (int) (pWidth*100),
            (int) (height*100)
        );
        if(pWidth == 0){
            return 0;
        }
        return (int)(realWidth * (height / pWidth));
    }

    @Override
    public int getHeight() {
        return getRenderHeight() + 4; 
    }
}
