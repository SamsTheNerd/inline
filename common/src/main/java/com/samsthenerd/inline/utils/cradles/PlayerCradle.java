package com.samsthenerd.inline.utils.cradles;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.utils.EntityCradle;
import com.samsthenerd.inline.utils.FakeClientPlayerMaker;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

/**
 * An entity cradle backed by a player GameProfile
 */
public class PlayerCradle extends EntityCradle {

    private static final HashMap<UUID, Entity> UUID_PLAYER_CACHE = new HashMap<>();
    private static final HashMap<String, Entity> NAME_PLAYER_CACHE = new HashMap<>();

    public static final Codec<GameProfile> GAME_PROFILE_CODEC = Codec.either(
            Codec.STRING.fieldOf("username").codec(), 
            Uuids.CODEC.fieldOf("uuid").codec()
        ).xmap(
            (nameOrId) -> nameOrId.map(
                name -> new GameProfile(null, name), 
                uuid -> new GameProfile(uuid, null)),
            
            (profile) -> {
                // TODO: maybe switch away from an Either codec.
                String name = profile.getName();
                if(name != null && !name.isEmpty()){
                    return Either.left(name);
                }
                return Either.right(profile.getId());
            }
        );

    private GameProfile profile;

    public PlayerCradle(GameProfile profile){
        this.profile = profile;
    }

    public GameProfile getProfile(){
        return profile;
    }

    public CradleType<?> getType(){
        return PlayerCradleType.INSTANCE;
    }

    @Override
    public String getId(){
        return profile.getId() == null ? profile.getName() : profile.getId().toString();
    }

    public Entity getEntity(World world){
        UUID playerId = profile.getId();
        if(playerId != null && UUID_PLAYER_CACHE.containsKey(playerId)){
            return UUID_PLAYER_CACHE.get(playerId);
        }
        String playerName = profile.getName();
        if(playerName != null && !playerName.equals("") && NAME_PLAYER_CACHE.containsKey(playerName)){
            return NAME_PLAYER_CACHE.get(playerName);
        }

        if(!world.isClient()){
            return null;
        }
        
        Pair<Entity, Boolean> playerRes = FakeClientPlayerMaker.getPlayerEntity(profile);
        if(playerRes.getRight() && playerId != null){
            UUID_PLAYER_CACHE.put(playerId, playerRes.getLeft());
        }
        if(playerRes.getRight() && playerName != null && !playerName.equals("")){
            NAME_PLAYER_CACHE.put(playerName, playerRes.getLeft());
        }
        return playerRes.getLeft();
    }

    private static class PlayerCradleType implements CradleType<PlayerCradle>{

        public static PlayerCradleType INSTANCE = EntityCradle.addCradleType(new PlayerCradleType());

        public Identifier getId(){
            return new Identifier(Inline.MOD_ID, "nbt");
        }

        public Codec<PlayerCradle> getCodec(){
            return GAME_PROFILE_CODEC.xmap(
                PlayerCradle::new,
                PlayerCradle::getProfile
            );
        }
    }
}
