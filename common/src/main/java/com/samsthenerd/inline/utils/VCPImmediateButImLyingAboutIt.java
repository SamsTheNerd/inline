package com.samsthenerd.inline.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;

import java.util.*;

/**
 * This is a stupid stupid stupid thing to let our DrawContext have an immediate even if the actual vc provider is
 * not an immediate. It exists to support compat with mods that render text to non-standard vertex consumers (like
 * glowcase for example).
 */
public class VCPImmediateButImLyingAboutIt extends VertexConsumerProvider.Immediate {
    private VertexConsumerProvider provider;

    /**
     * Make sure that we're not making a fake immediate when we have a real immediate we could be using.
     */
    public static Immediate of(VertexConsumerProvider provider){
        if(provider instanceof Immediate imm){
            return imm;
        }
        return new VCPImmediateButImLyingAboutIt(provider);
    }

    private VCPImmediateButImLyingAboutIt(VertexConsumerProvider provider){
        super(Tessellator.getInstance().getBuffer(), Map.of());
        this.provider = provider;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer renderLayer) {
        return provider.getBuffer(renderLayer);
    }

    @Override
    public void drawCurrentLayer() {
        // nop
    }

    @Override
    public void draw() {
        // nop
    }

    @Override
    public void draw(RenderLayer layer) {
        // nop
    }
}
