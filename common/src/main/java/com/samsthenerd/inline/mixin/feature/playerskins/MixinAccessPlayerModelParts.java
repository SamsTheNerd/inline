package com.samsthenerd.inline.mixin.feature.playerskins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public interface MixinAccessPlayerModelParts {
    @Accessor("PLAYER_MODEL_PARTS")
    public static TrackedData<Byte> getPlayerModelParts(){
        throw new AssertionError();
    }
}
