package com.samsthenerd.inline.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import com.samsthenerd.inline.Inline;

import com.samsthenerd.inline.utils.ImgFormatParser.ImgParseResult;
import com.samsthenerd.inline.utils.URLTextureUtils.LoadingState.StrState;
import com.samsthenerd.inline.utils.URLTextureUtils.LoadingState.SuccessState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class URLTextureUtils {

    private static final Map<Identifier, LoadingState> LOADING_TEXTURE_STATES = Collections.synchronizedMap(new HashMap<>());

    public sealed interface LoadingState permits StrState, SuccessState {
        record StrState(String st) implements LoadingState{}
        LoadingState ERROR = new StrState("error");
        LoadingState IN_PROGRESS = new StrState("progress");
        record SuccessState(IntPair dims, SpriteUVLens lens, Identifier textId) implements  LoadingState{};
    }

    // informed by hellozyemlya on discord
    @Nullable
    public static Identifier loadTextureFromURL(String urlStr, Identifier textureId){
        LoadingState state = LOADING_TEXTURE_STATES.get(textureId);
        if(state == LoadingState.IN_PROGRESS) return null;
        if(state == LoadingState.ERROR) return MissingSprite.getMissingSpriteId();
        if(state instanceof SuccessState succSt){
            return succSt.textId();
        }
        if(state != null) return MissingSprite.getMissingSpriteId(); // this shouldn't happen but just to be safe
        LOADING_TEXTURE_STATES.put(textureId, LoadingState.IN_PROGRESS);
        CompletableFuture.runAsync(() -> {
            try {
                URL textureUrl = URI.create(urlStr).toURL();
                var conn = textureUrl.openConnection();
                InputStream stream = conn.getInputStream();

                String contentType = URLConnection.guessContentTypeFromStream(stream);
                if (contentType == null) contentType = conn.getContentType();

                ImgFormatParser parser = ImgFormatParser.getFormatParser(contentType);

                Either<ImgParseResult, String> imgResult = parser.tryParse(stream, contentType);

                if (imgResult.left().isPresent()) { // success
                    ImgParseResult res = imgResult.left().get();
                    MinecraftClient.getInstance().execute(() -> {
                        LOADING_TEXTURE_STATES.put(textureId,
                            new SuccessState(res.dims(), res.lens(), res.registerTextureCallback().apply(textureId))
                        );
                    });
                } else { // error
                    String err = imgResult.right().get();
                    Inline.LOGGER.error("Failed to parse image " + urlStr + " : " + err);
                    LOADING_TEXTURE_STATES.put(textureId, LoadingState.ERROR);
                }
            } catch (IOException e){
                Inline.LOGGER.error("Failed to parse image " + urlStr + " : " + e);
                LOADING_TEXTURE_STATES.put(textureId, LoadingState.ERROR);
            }
        });
        return null;
    }


    @Nullable
    public static Pair<IntPair, SpriteUVLens> getTextureInfo(Identifier textureId){
        LoadingState state = LOADING_TEXTURE_STATES.get(textureId);
        if(state == LoadingState.IN_PROGRESS) return null;
        if(state == LoadingState.ERROR) return ERROR_TEXT_INFO;
        if(state instanceof SuccessState succSt){
            return new Pair<>(succSt.dims, succSt.lens());
        }
        return null;
    }

    private static final Pair<IntPair, SpriteUVLens> ERROR_TEXT_INFO = new Pair<>(new IntPair(16,16), SpriteUVRegion.FULL.asLens());

    static {
        // static inits not allowed in interface.
        ImgFormatParser.loadBuiltinParsers();
    }
}
