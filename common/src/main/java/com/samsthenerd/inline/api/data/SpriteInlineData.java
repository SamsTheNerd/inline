package com.samsthenerd.inline.api.data;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.utils.Spritelike;

import net.minecraft.util.Identifier;

public class SpriteInlineData implements InlineData<SpriteInlineData>{
    public SpriteDataType getType(){
        return SpriteDataType.INSTANCE;
    }

    @Override
    public Identifier getRendererId(){
        return new Identifier(Inline.MOD_ID, "spritelike");
    }

    public final Spritelike sprite;

    public SpriteInlineData(Spritelike sprite){
        this.sprite = sprite;
    }

    public static class SpriteDataType implements InlineDataType<SpriteInlineData> {
        public static SpriteDataType INSTANCE = new SpriteDataType();

        @Override
        public Identifier getId(){
            return new Identifier(Inline.MOD_ID, "spritelike");
        }

        @Override
        public Codec<SpriteInlineData> getCodec(){
            return Spritelike.CODEC.xmap(
                SpriteInlineData::new,
                data -> data.sprite
            );
        }
    }
}
