package com.samsthenerd.inline.utils;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.util.Either;
import com.samsthenerd.inline.mixin.core.NativeImageAccessor;
import com.samsthenerd.inline.utils.SpriteUVLens.AnimUVLens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface ImgFormatParser {

    // registerTextureCallback is called on render thread and is (spriteishId -> mcTextureId)
    record ImgParseResult(IntPair dims, SpriteUVLens lens, Function<Identifier, Identifier> registerTextureCallback){
        ImgParseResult(IntPair dims, SpriteUVLens lens, NativeImage img){
            this(dims, lens, (origId) -> {
                NativeImageBackedTexture texture = new NativeImageBackedTexture(img);
                MinecraftClient.getInstance().getTextureManager()
                    .registerTexture(origId, texture);
                return origId;
            });
        }
    }

    Either<ImgParseResult, String> tryParse(InputStream inStream, String contentType);

    // feel free to add your own!
    Map<String, ImgFormatParser> PARSERS = new HashMap<>();

    static void loadBuiltinParsers(){
        PARSERS.put("image/png", ImgFormatParser::parsePNG);
        PARSERS.put("image/gif", ImgFormatParser::parseGIF);
        PARSERS.put("image/jpeg", ImgFormatParser::parseImgSTBI);
    }

    // the stbi parser covers a lot! we'll just use that as a default
    static ImgFormatParser getFormatParser(String contentType){
        if(contentType == null) return ImgFormatParser::parseImgSTBI;
        return PARSERS.getOrDefault(contentType, ImgFormatParser::parseImgSTBI);
    }

    static Either<ImgParseResult, String> parsePNG(InputStream inStream, String contentType){
        try {
            NativeImage img = NativeImage.read(inStream);
            IntPair dims = new IntPair(img.getWidth(), img.getHeight());
            return Either.left(new ImgParseResult(dims, SpriteUVRegion.FULL.asLens(), img));
        } catch (IOException e){
            return Either.right(e.toString());
        }
    }

    static Either<ImgParseResult, String> parseGIF(InputStream inStream, String contentType){
        NativeImage image;
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer buf = TextureUtil.readResource(inStream).rewind();
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
                return Either.right("Could not load image as gif: " + STBImage.stbi_failure_reason());
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

            var delays = new int[framesBuf.get(0)];
            delayBuf.getIntBuffer(framesBuf.get(0)).get(delays);
            ImgParseResult imgRes = new ImgParseResult(
                new IntPair(wBuf.get(0), hBuf.get(0) * framesBuf.get(0)),
                new AnimUVLens(1.0/framesBuf.get(0), true, delays),
                image
            );
            return Either.left(imgRes);
        } catch (Exception e){
            return Either.right(e.toString());
        }
    }

    static Either<ImgParseResult, String> parseImgSTBI(InputStream inStream, String contentType) {
        NativeImage image;
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer buf = TextureUtil.readResource(inStream).rewind();
            IntBuffer wBuf = memoryStack.mallocInt(1);
            IntBuffer hBuf = memoryStack.mallocInt(1);
            IntBuffer channelsBuf = memoryStack.mallocInt(1);
            ByteBuffer imageBuf = STBImage.stbi_load_from_memory(
                buf,
                wBuf,
                hBuf,
                channelsBuf,
                4
            );
            if (imageBuf == null) {
                return Either.right("Could not load image here: " + STBImage.stbi_failure_reason());
            }

            image = new NativeImage(
                wBuf.get(0),
                hBuf.get(0),
                true
            );
            MemoryUtil.memCopy(
                MemoryUtil.memAddress(imageBuf),
                ((NativeImageAccessor) (Object) image).getPointer(),
                (long) wBuf.get(0) * hBuf.get(0) * 4
            );

            ImgParseResult imgRes = new ImgParseResult(
                new IntPair(wBuf.get(0), hBuf.get(0)),
                SpriteUVRegion.FULL.asLens(), image
            );

            return Either.left(imgRes);
        } catch (Exception e){
            return Either.right(e.toString());
        }
    }

    // if svg is actually wanted in the future it needs to be implemented externally. see v1.2.1 code for reference.
}
