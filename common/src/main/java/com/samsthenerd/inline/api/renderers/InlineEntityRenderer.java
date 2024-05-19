package com.samsthenerd.inline.api.renderers;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineRenderer;
import com.samsthenerd.inline.api.data.EntityInlineData;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class InlineEntityRenderer implements InlineRenderer<EntityInlineData>{

    public static final InlineEntityRenderer INSTANCE = new InlineEntityRenderer();

    public Identifier getId(){
        return new Identifier(Inline.MOD_ID, "entity");
    }

    public int render(EntityInlineData data, DrawContext context, int index, Style style, int codepoint, TextRenderingContext trContext){
        // only draw it once
        Entity ent = data.getEntity(MinecraftClient.getInstance().world);
        if(ent == null) return 0;
        float width = ent.getWidth();
        float height = ent.getHeight();
        int cDist = (int)Math.ceil(width * 8 / height);
        if(trContext.shadow){
            return cDist;
        }
        EntityRenderer renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(ent);
        MatrixStack matrices = context.getMatrices();
        matrices.translate(0, 8, 0);
        matrices.scale(8 / height, -8 / height, 8 / height);
        MinecraftClient.getInstance().getTickDelta();
        MinecraftClient client = MinecraftClient.getInstance();
        float tickDelta = client.getTickDelta();
        // float rotation = 90f * (Util.getMeasuringTimeMs() / 1000f + data.getUniqueOffset());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(15f));
        renderer.render(ent, 0, 0, matrices, trContext.vertexConsumers, trContext.light);
        return cDist;
    }

    public int charWidth(EntityInlineData data, Style style, int codepoint){
        Entity ent = data.getEntity(MinecraftClient.getInstance().world);
        if(ent == null) return 0;
        float width = ent.getWidth();
        float height = ent.getHeight();
        return (int)Math.ceil(width * 8 / height);
    }
}
