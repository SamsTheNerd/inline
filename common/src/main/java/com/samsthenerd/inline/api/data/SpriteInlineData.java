package com.samsthenerd.inline.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.utils.Spritelike;

import com.samsthenerd.inline.utils.TextureSprite;
import net.minecraft.util.Identifier;

/**
 * data class for rendering spritelikes.
 *
 * For many use cases it may be a better idea to extend this class
 * to use a more case-specific codec or make new data and renderer classes
 * that call out to the sprite renderer. See ModIconData for an example of the
 * first method and PlayerHeadData/PlayerHeadRenderer for the second.
 */
public class SpriteInlineData implements InlineData<SpriteInlineData>{
    public SpriteDataType getType(){
        return SpriteDataType.INSTANCE;
    }

    @Override
    public Identifier getRendererId(){
        return new Identifier(Inline.MOD_ID, "spritelike");
    }

    public final Spritelike sprite;
    public final boolean shouldTint; // whether or not it should tint to match text color.

    public SpriteInlineData(Spritelike sprite, boolean shouldTint){
        this.sprite = sprite;
        this.shouldTint = shouldTint;
    }

    public SpriteInlineData(Spritelike sprite){
        this(sprite, false);
    }

    public static class SpriteDataType implements InlineDataType<SpriteInlineData> {
        public static SpriteDataType INSTANCE = new SpriteDataType();

        @Override
        public Identifier getId(){
            return new Identifier(Inline.MOD_ID, "spritelike");
        }

        @Override
        public Codec<SpriteInlineData> getCodec(){
            return RecordCodecBuilder.create(instance -> instance.group(
                Spritelike.CODEC.fieldOf("sprite").forGetter(data -> data.sprite),
                Codec.BOOL.optionalFieldOf("shouldTint", false).forGetter(data -> data.shouldTint)
            ).apply(instance, SpriteInlineData::new));
        }
    }
}
