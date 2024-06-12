package com.samsthenerd.inline.utils;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.samsthenerd.inline.utils.cradles.EntTypeCradle;
import com.samsthenerd.inline.utils.cradles.NbtCradle;
import com.samsthenerd.inline.utils.cradles.PlayerCradle;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * A fancy entity supplier with serialization.
 * <p>
 * EntityCradle is generally server safe, although calls to {@link EntityCradle#getEntity(World)}
 * aren't guaranteed to be.
 * 
 * @see EntTypeCradle
 * @see NbtCradle
 * @see PlayerCradle 
 */
public interface EntityCradle {
    public CradleType<?> getType();

    /**
     * Supplies an entity wrapped by the cradle. 
     * This isn't guaranteed to be server-safe.
     * <p>
     * Implementations should try to cache their entity if possible.
     * @param world
     * @return an entity based on this cradle
     */
    @Nullable
    public Entity getEntity(World world);

    public static interface CradleType<C extends EntityCradle>{

        public Identifier getId();

        public Codec<C> getCodec();
    }

    public static final Map<Identifier, CradleType<? extends EntityCradle>> CRADLES = new HashMap<>();

    public static <T extends CradleType> T addCradleType(T cradleType){
        CRADLES.put(cradleType.getId(), cradleType);
        return cradleType;
    }

    public static final Codec<CradleType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(
        (id) -> {
            if(CRADLES.containsKey(id)){
                return DataResult.success(CRADLES.get(id));
            } else {
                return DataResult.error(() -> "No entity cradle type: " + id.toString());
            }
        }, (CradleType<?> type) -> {
            return type.getId();
        });

    public static final Codec<EntityCradle> CRADLE_CODEC = TYPE_CODEC.dispatch("type",
        EntityCradle::getType,
        CradleType::getCodec
    );
}
