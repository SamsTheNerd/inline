package com.samsthenerd.inline.utils;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

// just a raw texture !
public class TextureSprite extends Spritelike{
    
    private Identifier id;
    private final IntPair textDims;
    private final SpriteUVLens lens;

    public TextureSprite(Identifier id, float minU, float minV, float maxU, float maxV, int textureWidth, int textureHeight){
        this(id, new IntPair(textureWidth, textureHeight), new SpriteUVRegion(minU, minV, maxU, maxV).asLens());
    }

    public TextureSprite(Identifier id, int textureWidth, int textureHeight){
        this(id, 0, 0, 1, 1, textureWidth, textureHeight);
    }

    public TextureSprite(Identifier id, IntPair textureDimensions, SpriteUVLens lens){
        this.id = id;
        this.textDims = textureDimensions;
        this.lens = lens;
    }

    public static TextureSprite fromPixels(Identifier id, int left, int top, int width, int height, int textWidth, int textHeight){
        return new TextureSprite(id, left / (float)textWidth, top / (float)textHeight, (left + width) / (float)textWidth, (top + height) / (float)textHeight, textWidth, textHeight);
    }

    public TextureSprite(Identifier id){
        this(id, 0, 0, 1, 1, 16, 16);
    }

    @Override
    public SpritelikeType getType(){
        return TextureSpriteType.INSTANCE;
    }

    public Identifier getTextureId(){
        return id;
    }

    @Override
    public SpriteUVRegion getUVs(long time) {
        return lens.genUVs(time);
    }


    public int getTextureWidth(){
        return textDims.width();
    }

    public int getTextureHeight(){
        return textDims.height();
    }

    public static class TextureSpriteType implements SpritelikeType{
        public static final TextureSpriteType INSTANCE = new TextureSpriteType();
        private static final MapCodec<TextureSprite> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(TextureSprite::getTextureId),
            Codec.INT.optionalFieldOf("textWidth", 16).forGetter(TextureSprite::getTextureWidth),
            Codec.INT.optionalFieldOf("textHeight", 16).forGetter(TextureSprite::getTextureHeight)
        ).apply(instance, TextureSprite::new));

        public MapCodec<TextureSprite> getCodec(){
            return CODEC;
        }

        public String getId(){
            return "texture";
        }
    }
}
