package com.samsthenerd.inline.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.utils.TextureSprite.TextureSpriteType;
import com.samsthenerd.inline.utils.URLSprite.UrlSpriteType;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

/**
 * A wrapper around various texture sources. 
 * <p>
 * Spritelike is server safe, on the client it renders with a SpritelikeRenderer.
 * IMPORTANT NOTE: spritelike serialization is a bit spotty, if you need to send it
 * to the client you should consider instead sending something more specific that can
 * then reconstruct a spritelike. See SpriteInlineData for some more related notes.
 * <p>
 * You shouldn't need to make new Spritelike types.
 * 
 * @see URLSprite
 * @see TextureSprite
 */
public abstract class Spritelike {

    public abstract SpritelikeType getType();

    @Nullable
    public abstract Identifier getTextureId();

    public abstract SpriteUVRegion getUVs(long time);

    public SpriteUVRegion getUVs(){
        return getUVs(SpriteUVLens.getSysTime());
    }

    // these are mostly just here for the w:h ratio
    public abstract int getTextureWidth();
    public abstract int getTextureHeight();

    public static Spritelike fromJson(JsonElement json){
        return Spritelike.CODEC.parse(JsonOps.INSTANCE, (json))
            .resultOrPartial(error -> {})
	        .orElse(null);
    }

    public static Spritelike fromNbt(NbtElement nbt){
        return Spritelike.CODEC.parse(NbtOps.INSTANCE, (nbt))
            .resultOrPartial(error -> {})
	        .orElse(null);
    }

    private static final Map<String, SpritelikeType> TYPES = new HashMap<>();

    static{
        registerType(UrlSpriteType.INSTANCE);
        registerType(TextureSpriteType.INSTANCE);
    }

    public static void registerType(SpritelikeType type){
        TYPES.put(type.getId(), type);
    }

    private static final Codec<SpritelikeType> TYPE_CODEC = Codec.STRING.comapFlatMap(id -> {
        SpritelikeType type = TYPES.get(id);
        if(type == null){
            return DataResult.error(()->{return "Unknown spritelike type: " + id;});
        }
        return DataResult.success(type);
    }, SpritelikeType::getId);

    public static final Codec<Spritelike> CODEC = TYPE_CODEC.dispatch("type", Spritelike::getType, SpritelikeType::getCodec);

    public interface SpritelikeType{
        public Codec<? extends Spritelike> getCodec();

        public String getId();

        public static SpritelikeType of(String id, Codec<Spritelike> codec){
            return new Simple(id, codec);
        }

        public static class Simple implements SpritelikeType{
            private final String id;
            private final Codec<Spritelike> codec;

            public Simple(String id, Codec<Spritelike> codec){
                this.id = id;
                this.codec = codec;
            }

            @Override
            public Codec<Spritelike> getCodec(){
                return codec;
            }

            @Override
            public String getId(){
                return id;
            }
        }
    }


}
