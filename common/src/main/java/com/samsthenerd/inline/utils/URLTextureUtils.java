package com.samsthenerd.inline.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.Inline;

import com.samsthenerd.inline.mixin.core.NativeImageAccessor;
import com.samsthenerd.inline.utils.SpriteUVLens.AnimUVLens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class URLTextureUtils {

    private static final Map<Identifier, Identifier> LOADED_TEXTURES = Collections.synchronizedMap(new HashMap<>());
    // same key as the loaded textures
    private static final Map<Identifier, Pair<IntPair, SpriteUVLens>> TEXTURE_INFO = Collections.synchronizedMap(new HashMap<>());

    // this is also errored ones
    private static final Set<Identifier> IN_PROGRESS_TEXTURES = Collections.synchronizedSet(new HashSet<>());

    // informed by hellozyemlya on discord
    @Nullable
    public static Identifier loadTextureFromURL(String url, Identifier textureId){
        Identifier maybeTexture = LOADED_TEXTURES.get(textureId);
        if(maybeTexture != null){
            return maybeTexture;
        }
        if(IN_PROGRESS_TEXTURES.contains(textureId)) return null;
        IN_PROGRESS_TEXTURES.add(textureId);
        // Inline.logPrint("Loading texture from URL: " + url);
        CompletableFuture.runAsync(() -> {
            try{
                URL textureUrl = new URL(url);
                var conn = textureUrl.openConnection();
                String contentType = conn.getContentType();
                if("image/png".equals(contentType)){
                    InputStream stream = conn.getInputStream();
                    // Inline.logPrint("in thread maybe ?");
                    NativeImage baseImage = NativeImage.read(stream);
                    MinecraftClient.getInstance().execute(() -> {
                        NativeImageBackedTexture texture = new NativeImageBackedTexture(baseImage);

                        MinecraftClient.getInstance().getTextureManager()
                          .registerTexture(textureId, texture);
                        LOADED_TEXTURES.put(textureId, textureId);
                        TEXTURE_INFO.put(textureId, new Pair<>(
                          new IntPair(baseImage.getWidth(), baseImage.getHeight()),
                          SpriteUVRegion.FULL.asLens()
                        ));
                        IN_PROGRESS_TEXTURES.remove(textureId);
                    });
                } else if("image/gif".equals(contentType)){
                    var byBuf = TextureUtil.readResource(conn.getInputStream());
//                    var byBuf = ByteBuffer.wrap(conn.getInputStream().readAllBytes());
                    byBuf.rewind();
                    readGif(textureId, byBuf);
                } else {
                    Inline.LOGGER.error("Unknown image type at url: " + url);
                }

            } catch (Exception e){
                Inline.LOGGER.error("Failed to load texture from URL: " + url + "\n:" + e);
            }
            });
        return null;
    }

    @Nullable
    public static Pair<IntPair, SpriteUVLens> getTextureInfo(Identifier textureId){
        return TEXTURE_INFO.get(textureId);
    }


    // modified from Skye's emojiless mod.
    public static Pair<IntPair, SpriteUVLens> readGif(Identifier loc, ByteBuffer buf) throws IOException{
        NativeImage image;
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            PointerBuffer delayBuf = memoryStack.mallocPointer(1);
            IntBuffer wBuf = memoryStack.mallocInt(1);
            IntBuffer hBuf = memoryStack.mallocInt(1);
            IntBuffer framesBuf = memoryStack.mallocInt(1);
            IntBuffer channelsBuf = memoryStack.mallocInt(1);
            ByteBuffer imageBuf = STBImage.stbi_load_gif_from_memory(
                buf,
                delayBuf,
                wBuf,
                hBuf,
                framesBuf,
                channelsBuf,
                4
            );
            if (imageBuf == null) {
                throw new IOException("Could not load image here: " + STBImage.stbi_failure_reason());
            }

            image = new NativeImage(
                wBuf.get(0),
                hBuf.get(0) * framesBuf.get(0),
                true
            );
            MemoryUtil.memCopy(
                MemoryUtil.memAddress(imageBuf),
                ((NativeImageAccessor) (Object) image).getPointer(),
                (long) wBuf.get(0) * hBuf.get(0) * framesBuf.get(0) * 4
            );
//
//
//            var cutImageBuff = imageBuf.slice(0, wBuf.get(0) * hBuf.get(0) * framesBuf.get(0) * 4);
//            image = NativeImage.read(cutImageBuff);
////
            var tex = new NativeImageBackedTexture(image);

            var delays = new int[framesBuf.get(0)];
            delayBuf.getIntBuffer(framesBuf.get(0)).get(delays);
            Pair<IntPair, SpriteUVLens> textInfo = new Pair<>(
                new IntPair(wBuf.get(0), hBuf.get(0) * framesBuf.get(0)),
                new AnimUVLens(1.0/framesBuf.get(0), true, delays)
            );

            MinecraftClient.getInstance().execute(() -> {
//                var actualId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(loc.toTranslationKey(), tex);
                MinecraftClient.getInstance().getTextureManager().registerTexture(loc, tex);
                LOADED_TEXTURES.put(loc, loc);
                TEXTURE_INFO.put(loc, textInfo);
                IN_PROGRESS_TEXTURES.remove(loc);
            });
            return textInfo;
        }
    }
}
