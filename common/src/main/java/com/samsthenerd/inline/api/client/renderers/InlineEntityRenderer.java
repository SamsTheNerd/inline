package com.samsthenerd.inline.api.client.renderers;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.client.InlineRenderer;
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

        float rot = 15f;
        double radRot = Math.toRadians(rot % 90);
        double pWidth = width * (Math.cos(radRot)+Math.sin(radRot));

        int cDist = (int)Math.ceil(pWidth * 8 / height) + 1;
        if(trContext.shadow()){
            return cDist;
        }
        EntityRenderer renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(ent);
        MatrixStack matrices = context.getMatrices();
        matrices.translate(cDist/2.0, 8, 0);
        matrices.scale(8 / height, -8 / height, 8 / height);
        MinecraftClient.getInstance().getTickDelta();
        MinecraftClient client = MinecraftClient.getInstance();
        float tickDelta = client.getTickDelta();
        // float rotation = 90f * (Util.getMeasuringTimeMs() / 1000f + data.getUniqueOffset());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));
        renderer.render(ent, 0, 0, matrices, context.getVertexConsumers(), trContext.light());
        return cDist;
    }

    public int charWidth(EntityInlineData data, Style style, int codepoint){
        Entity ent = data.getEntity(MinecraftClient.getInstance().world);
        if(ent == null) return 0;
        float width = ent.getWidth();
        float height = ent.getHeight();

        float rot = 15f;
        double radRot = Math.toRadians(rot % 90);
        double pWidth = width * (Math.cos(radRot)+Math.sin(radRot));

        return (int)Math.ceil(pWidth * 8 / height) + 1;
    }
}
