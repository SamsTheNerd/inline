package com.samsthenerd.inline.utils.cradles;

import java.util.HashMap;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.utils.EntityCradle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * An entity cradle backed by an EntityType identifier.
 */
public class EntTypeCradle<E extends Entity> implements EntityCradle {

    private static final HashMap<Identifier, Entity> ENTITY_CACHE = new HashMap<>();

    private EntityType<E> type;

    public EntTypeCradle(EntityType<E> type){
        this.type = type;
    }

    public static Optional<EntTypeCradle> fromTypeId(Identifier id){
        return EntityType.get(id.toString()).map(EntTypeCradle::new);
    }

    public EntityType<E> getEntType(){
        return type;
    }

    public CradleType<?> getType(){
        return EntTypeCradleType.INSTANCE;
    }

    public Entity getEntity(World world){
        Identifier typeId = EntityType.getId(type);
        if(ENTITY_CACHE.containsKey(typeId)){
            return ENTITY_CACHE.get(typeId);
        }

        try{
            Entity newEnt = type.create(world);
            ENTITY_CACHE.put(typeId, newEnt);
            return newEnt;
        } catch (Exception e){
            return null;
        }

    }

    private static class EntTypeCradleType implements CradleType<EntTypeCradle>{

        public static EntTypeCradleType INSTANCE = EntityCradle.addCradleType(new EntTypeCradleType());

        public Identifier getId(){
            return new Identifier(Inline.MOD_ID, "enttype");
        }

        public Codec<EntTypeCradle> getCodec(){
            return Identifier.CODEC.comapFlatMap(
                (id) -> EntTypeCradle.fromTypeId(id).map(DataResult::success).orElse(DataResult.error(() -> "no entity type: " + id.toString())),
                (cradle) -> EntityType.getId(cradle.getEntType())
            );
        }
    }
}
