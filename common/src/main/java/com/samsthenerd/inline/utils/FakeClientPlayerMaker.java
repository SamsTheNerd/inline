package com.samsthenerd.inline.utils;

import com.samsthenerd.inline.mixin.feature.playerskins.MixinAccessPlayerModelParts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class FakeClientPlayerMaker {
    public static Entity getPlayerEntity(ProfileComponent profile) {
        PlayerEntity player = new OtherClientPlayerEntity(MinecraftClient.getInstance().world, profile.gameProfile()) {
            @Override
            public boolean shouldRenderName() {
                return false;
            }
        };
        player.prevCapeY = player.capeY = (player.getY() - 0.5);
        player.prevCapeX = player.capeX = player.getX();
        player.prevCapeZ = player.capeZ = player.getZ();
        player.getDataTracker().set(MixinAccessPlayerModelParts.getPlayerModelParts(), (byte) 0b11111111);
        return player;
    }
}
