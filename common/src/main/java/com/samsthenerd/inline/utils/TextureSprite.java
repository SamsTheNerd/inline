package com.samsthenerd.inline.utils;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

// just a raw texture !
public class TextureSprite extends Spritelike{
    
    private Identifier id;
    private float minU;
    private float minV;
    private float maxU;
    private float maxV;
    private int textWidth;
    private int textHeight;

    // TODO: figure out if we should clean this up to be fully int pixel based? - deal with that later BUT BEFORE RELEASE
    public TextureSprite(Identifier id, float minU, float minV, float maxU, float maxV, int textWidth, int textHeight){
        this.id = id;
        this.minU = minU;
        this.minV = minV;
        this.maxU = maxU;
        this.maxV = maxV;
        this.textWidth = textWidth;
        this.textHeight = textHeight;
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

    // should these take some timing input to allow for animated sprites ?
    public float getMinU(){
        return this.minU;
    }
    public float getMinV(){
        return this.minV;
    }
    public float getMaxU(){
        return this.maxU;
    }
    public float getMaxV(){
        return this.maxV;
    }

    public int getTextureWidth(){
        return textWidth;
    }

    public int getTextureHeight(){
        return textHeight;
    }

    public static class TextureSpriteType implements SpritelikeType{
        public static final TextureSpriteType INSTANCE = new TextureSpriteType();
        private static final Codec<TextureSprite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(TextureSprite::getTextureId),
            Codec.FLOAT.optionalFieldOf("minU", 0f).forGetter(TextureSprite::getMinU),
            Codec.FLOAT.optionalFieldOf("minV", 0f).forGetter(TextureSprite::getMinV),
            Codec.FLOAT.optionalFieldOf("maxU", 1f).forGetter(TextureSprite::getMaxU),
            Codec.FLOAT.optionalFieldOf("maxV", 1f).forGetter(TextureSprite::getMaxV),
            Codec.INT.optionalFieldOf("textWidth", 16).forGetter(TextureSprite::getTextureWidth),
            Codec.INT.optionalFieldOf("textHeight", 16).forGetter(TextureSprite::getTextureHeight)
        ).apply(instance, TextureSprite::new));

        public Codec<TextureSprite> getCodec(){
            return CODEC;
        }

        public String getId(){
            return "texture";
        }
    }
}
