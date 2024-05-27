package com.samsthenerd.inline.utils;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.samsthenerd.inline.Inline;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class URLTextureUtils {

    private static final Map<Identifier, Identifier> LOADED_TEXTURES = Collections.synchronizedMap(new HashMap<>());
    // same key as the loaded textures
    private static final Map<Identifier, Pair<Integer, Integer>> TEXTURE_DIMENSIONS = Collections.synchronizedMap(new HashMap<>());

    // informed by hellozyemlya on discord
    public static Identifier loadTextureFromURL(String url, Identifier textureId){
        Identifier maybeTexture = LOADED_TEXTURES.get(textureId);
        if(maybeTexture != null){
            return maybeTexture;
        }
        Inline.logPrint("Loading texture from URL: " + url);
        CompletableFuture.runAsync(() -> {
            try{
                URL textureUrl = new URL(url);
                InputStream stream = textureUrl.openStream();
                Inline.logPrint("in thread maybe ?");
                try{
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(NativeImage.read(stream));
                    NativeImage baseImage = texture.getImage();
                    Identifier actualTextureId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(textureId.toTranslationKey(), texture);
                    LOADED_TEXTURES.put(textureId, actualTextureId);
                    TEXTURE_DIMENSIONS.put(textureId, new Pair<>(baseImage.getWidth(), baseImage.getHeight()));
                } catch (Exception e){
                    Inline.LOGGER.error("Failed to load texture from URL: " + url + "\n:" + e);
                }
            } catch (Exception e){
                Inline.LOGGER.error("Failed to load texture from URL: " + url + "\n:" + e);
            }
            });
        return new Identifier("");
    }

    @Nullable
    public static Pair<Integer, Integer> getTextureDimensions(Identifier textureId){
        return TEXTURE_DIMENSIONS.get(textureId);
    }
}
