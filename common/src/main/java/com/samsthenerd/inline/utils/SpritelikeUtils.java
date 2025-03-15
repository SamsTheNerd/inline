package com.samsthenerd.inline.utils;

import net.minecraft.client.texture.Sprite;

public class SpritelikeUtils {
    public static Spritelike spritelikeFromSprite(Sprite sprite){
        return new TextureSprite(sprite.getAtlasId(),
            sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(),
            sprite.getContents().getWidth(), sprite.getContents().getHeight()
        );
    }
}
