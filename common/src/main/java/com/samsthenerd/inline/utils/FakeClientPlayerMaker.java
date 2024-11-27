package com.samsthenerd.inline.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.samsthenerd.inline.mixin.feature.playerskins.MixinAccessPlayerModelParts;
import com.samsthenerd.inline.mixin.feature.playerskins.MixinClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Pair;
import net.minecraft.util.UserCache;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

public class FakeClientPlayerMaker {
    public static Pair<Entity, Boolean> getPlayerEntity(GameProfile profile){
        GameProfile betterProfile = getBetterProfile(profile);
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

    private static final Map<UUID, Optional<GameProfile>> UUID_PROFILE_CACHE = new HashMap<>();
    private static final Map<String, Optional<GameProfile>> NAME_PROFILE_CACHE = new HashMap<>();

    private static ApiServices apiServices;
    private static MinecraftSessionService sessionService;
    private static UserCache userCache;
    private static Executor executor;

    static {
        MinecraftClient client = MinecraftClient.getInstance();
        apiServices = ApiServices.create(((MixinClientAccessor)client).getAuthenticationService(), client.runDirectory);
        sessionService = apiServices.sessionService();
        apiServices.userCache().setExecutor(client);
        userCache = apiServices.userCache();
        executor = client;
    }

    @Nullable
    public static GameProfile getBetterProfile(GameProfile weakProf){
        // try to find the better profile in our caches
        if(weakProf.getId() != null){
            Optional<GameProfile> maybeProf = UUID_PROFILE_CACHE.get(weakProf.getId());
            if(maybeProf != null){
                return maybeProf.orElse(null);
            }
        }
        if(weakProf.getName() != null && !weakProf.getName().equals("")){
            Optional<GameProfile> maybeProf = NAME_PROFILE_CACHE.get(weakProf.getName().toLowerCase());
            if(maybeProf != null){
                return maybeProf.orElse(null);
            }
        }
        // can't find, try to fetch it

        // set these to empty optionals so we don't repeatedly fetch a ton
        if(weakProf.getId() != null)
            UUID_PROFILE_CACHE.put(weakProf.getId(), Optional.empty());
        if(weakProf.getName() != null && !weakProf.getName().equals(""))
            NAME_PROFILE_CACHE.put(weakProf.getName().toLowerCase(), Optional.empty());


        // TODO :FIX
//        if(MixinClientHeadChecker.getSessionService() == null){
//            SkullBlockEntity.setServices(apiServices, executor);
//        }
//        UserCache.setUseRemote(true);
//        SkullBlockEntity.loadProperties(weakProf, FakeClientPlayerMaker::acceptBetterProf);

        return null;
    }

    private static void acceptBetterProf(GameProfile betterProf){
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
