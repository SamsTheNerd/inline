package com.samsthenerd.inline.utils.cradles;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.utils.EntityCradle;
import com.samsthenerd.inline.utils.FakeClientPlayerMaker;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.function.Function;

/**
 * An entity cradle backed by a player GameProfile
 */
public class PlayerCradle extends EntityCradle {

    private final ProfileComponent profile;

    public PlayerCradle(ProfileComponent profile){
        this.profile = profile;
    }

    public ProfileComponent getProfile(){
        return profile;
    }

    public CradleType<?> getType(){
        return PlayerCradleType.INSTANCE;
    }

    @Override
    public String getId(){
        return profile.id().map(Object::toString).orElseGet(() -> profile.name().get());
    }

    public Entity getEntity(World world){
        return FakeClientPlayerMaker.getPlayerEntity(profile);
    }

    private static class PlayerCradleType implements CradleType<PlayerCradle>{

        public static PlayerCradleType INSTANCE = EntityCradle.addCradleType(new PlayerCradleType());

        public Identifier getId(){
            return Inline.id( "nbt");
        }

        public Codec<PlayerCradle> getCodec(){
            return ProfileComponent.CODEC.xmap(
                PlayerCradle::new,
                PlayerCradle::getProfile
            );
        }
    }
}
