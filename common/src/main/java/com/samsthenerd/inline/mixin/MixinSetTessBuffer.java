package com.samsthenerd.inline.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.Tessellator;

@Mixin(Tessellator.class)
public interface MixinSetTessBuffer {

    @Accessor("INSTANCE")
    @Mutable
    public static void setInstance(Tessellator tes){
        throw new AssertionError();
    }
}
