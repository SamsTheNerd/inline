package com.samsthenerd.inline.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.authlib.minecraft.MinecraftSessionService;

import net.minecraft.block.entity.SkullBlockEntity;

@Mixin(SkullBlockEntity.class)
public interface MixinClientHeadChecker {
    @Accessor("sessionService")
    public static MinecraftSessionService getSessionService(){
        throw new AssertionError();
    }
}
