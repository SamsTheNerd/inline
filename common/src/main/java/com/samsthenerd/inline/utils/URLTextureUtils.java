package com.samsthenerd.inline.utils;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
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
        // Inline.logPrint("Loading texture from URL: " + url);
        CompletableFuture.runAsync(() -> {
            try{
                URL textureUrl = new URL(url);
                InputStream stream = textureUrl.openStream();
                // Inline.logPrint("in thread maybe ?"); 
                try{
                    NativeImage baseImage = NativeImage.read(stream);
                    if(baseImage == null){
                        // Inline.logPrint("null baseImage: " + url.toString());
                        return;
                    }
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(baseImage);
                    // NativeImage baseImage = texture.getImage();
                    Runnable registerTextureRunnable = () -> {
                        Identifier actualTextureId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(textureId.toTranslationKey(), texture);
                        LOADED_TEXTURES.put(textureId, actualTextureId);
                        TEXTURE_DIMENSIONS.put(textureId, new Pair<>(baseImage.getWidth(), baseImage.getHeight()));
                    };
                    MinecraftClient.getInstance().execute(() -> {
                        Objects.requireNonNull(registerTextureRunnable);
                        RenderSystem.recordRenderCall(registerTextureRunnable::run);
                    });
                    
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
