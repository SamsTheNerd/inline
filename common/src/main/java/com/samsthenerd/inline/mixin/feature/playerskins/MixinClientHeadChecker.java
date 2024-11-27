package com.samsthenerd.inline.mixin.feature.playerskins;

import net.minecraft.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SkullBlockEntity.class)
public interface MixinClientHeadChecker {
//    @Accessor("sessionService")
//    public static MinecraftSessionService getSessionService(){
//        throw new AssertionError();
//    }
}
