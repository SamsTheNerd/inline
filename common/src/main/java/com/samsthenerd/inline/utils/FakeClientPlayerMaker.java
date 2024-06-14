package com.samsthenerd.inline.utils;

import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.api.client.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.mixin.feature.playerskins.MixinAccessPlayerModelParts;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

public class FakeClientPlayerMaker {
    public static Pair<Entity, Boolean> getPlayerEntity(GameProfile profile){
        GameProfile betterProfile = PlayerHeadRenderer.getBetterProfile(profile);
        boolean isActuallyBetter = false;
        GameProfile profileToUse = profile;
        if(betterProfile != null){
            profileToUse = betterProfile;
            isActuallyBetter = true;
        }
        PlayerEntity player = new OtherClientPlayerEntity(MinecraftClient.getInstance().world, profileToUse){
            @Override
            public boolean shouldRenderName() {
                return false;
            }
        };
        player.prevCapeY = player.capeY = (player.getY() - 0.5);
        player.prevCapeX = player.capeX = player.getX();
        player.prevCapeZ = player.capeZ = player.getZ();
        player.getDataTracker().set(MixinAccessPlayerModelParts.getPlayerModelParts(), (byte)0b11111111);
        return new Pair<>(
            player,
            isActuallyBetter
        );
    }
}
