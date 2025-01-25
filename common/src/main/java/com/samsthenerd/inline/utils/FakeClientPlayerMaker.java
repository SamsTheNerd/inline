package com.samsthenerd.inline.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.samsthenerd.inline.mixin.feature.playerskins.MixinAccessPlayerModelParts;
import com.samsthenerd.inline.mixin.feature.playerskins.MixinClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ApiServices;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

public class FakeClientPlayerMaker {
    private static final HashMap<UUID, Entity> UUID_PLAYER_CACHE = new HashMap<>();
    private static final HashMap<String, Entity> NAME_PLAYER_CACHE = new HashMap<>();

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

    private static final Map<UUID, Optional<GameProfile>> UUID_PROFILE_CACHE = new HashMap<>();
    private static final Map<String, Optional<GameProfile>> NAME_PROFILE_CACHE = new HashMap<>();

    @NotNull
    public static GameProfile getProfileFromCache(ProfileComponent profile) {
        // Try to find the better profile in our caches
        if (profile.id().isPresent()) {
            Optional<GameProfile> maybeProf = UUID_PROFILE_CACHE.get(profile.id().get());
            if (maybeProf != null) {
                return maybeProf.orElse(profile.gameProfile());
            }
        }

        if (profile.name().isPresent()) {
            Optional<GameProfile> maybeProf = NAME_PROFILE_CACHE.get(profile.name().get().toLowerCase());
            if (maybeProf != null) {
                return maybeProf.orElse(profile.gameProfile());
            }
        }
        // Can't find, try to fetch it

        // Set these to empty optionals so we don't repeatedly fetch a ton
        if (profile.id().isPresent()) {
            UUID_PROFILE_CACHE.put(profile.id().get(), Optional.empty());
        }

        if (profile.name().isPresent()) {
            NAME_PROFILE_CACHE.put(profile.name().get().toLowerCase(), Optional.empty());
        }

        profile.getFuture().thenAcceptAsync(betterComponent -> {
            // Update the cache with the new data
            UUID_PROFILE_CACHE.put(betterComponent.gameProfile().getId(), Optional.of(betterComponent.gameProfile()));
            NAME_PROFILE_CACHE.put(betterComponent.gameProfile().getName().toLowerCase(), Optional.of(betterComponent.gameProfile()));
        });

        return profile.gameProfile();
    }

    private static void acceptBetterProf(GameProfile betterProf) {
//        if(!betterProf.isComplete()){
//            // I can't consistently reproduce this issue to debug it, let's just log it and call it a day.
//            Inline.LOGGER.warn(
//                    "Could not complete profile, either username does not belong to a player or some other error occured (can try restarting the game and clearing usercache.json). Incomplete Profile: "
//                    + betterProf
//            );
//        }
//        if(betterProf.getId() != null){
//            UUID_PROFILE_CACHE.put(betterProf.getId(), Optional.of(betterProf));
//        }
//        if(betterProf.getName() != null && !betterProf.getName().equals("")){
//            NAME_PROFILE_CACHE.put(betterProf.getName().toLowerCase(), Optional.of(betterProf));
//        }
    }
}
