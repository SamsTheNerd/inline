package com.samsthenerd.inline.utils;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface EntityCradle {
    public CradleType<?> getType();

    @Nullable
    public Entity getEntity(World world);

    public static interface CradleType<C extends EntityCradle>{

        public Identifier getId();

        public Codec<C> getCodec();
    }

    public static final Map<Identifier, CradleType<? extends EntityCradle>> CRADLES = new HashMap<>();

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
