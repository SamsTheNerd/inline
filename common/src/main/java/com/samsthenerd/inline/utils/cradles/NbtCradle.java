package com.samsthenerd.inline.utils.cradles;

import java.util.HashMap;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.utils.EntityCradle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class NbtCradle implements EntityCradle {

    private static final HashMap<NbtCompound, Entity> ENTITY_CACHE = new HashMap<>();

    private Entity ownCachedEntity; // to avoid having to hash the nbt compound every time
    private NbtCompound entityTag;

    public NbtCradle(Entity entity){
        entityTag = new NbtCompound();
        entity.saveNbt(entityTag);
        ownCachedEntity = entity;
        ENTITY_CACHE.put(entityTag, entity);
    }

    public NbtCradle(NbtCompound tag){
        entityTag = tag;
    }

    public NbtCompound getTag(){
        return entityTag;
    }

    public CradleType<?> getType(){
        return NbtCradleType.INSTANCE;
    }

    public Entity getEntity(World world){
        if(ownCachedEntity != null){
            return ownCachedEntity;
        }
        if(ENTITY_CACHE.containsKey(entityTag)){
            ownCachedEntity = ENTITY_CACHE.get(entityTag);
            return ownCachedEntity;
        }

        ownCachedEntity = EntityType.getEntityFromNbt(entityTag, world).orElse(null);
        ENTITY_CACHE.put(entityTag, ownCachedEntity);
        return ownCachedEntity;
    }

    public static class NbtCradleType implements CradleType<NbtCradle>{

        public static NbtCradleType INSTANCE = new NbtCradleType();

        public Identifier getId(){
            return new Identifier(Inline.MOD_ID, "nbt");
        }

        public Codec<NbtCradle> getCodec(){
            return NbtCompound.CODEC.xmap(NbtCradle::new, NbtCradle::getTag);
        }
    }
}
