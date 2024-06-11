package com.samsthenerd.inline.api.client.renderers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import com.samsthenerd.inline.api.data.SpriteInlineData;
import com.samsthenerd.inline.mixin.feature.playerskins.MixinClientAccessor;
import com.samsthenerd.inline.mixin.feature.playerskins.MixinClientHeadChecker;
import com.samsthenerd.inline.utils.TextureSprite;

import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public class PlayerHeadRenderer implements InlineRenderer<PlayerHeadData>{

    public static final PlayerHeadRenderer INSTANCE = new PlayerHeadRenderer();

    @Override
    public Identifier getId(){
        return new Identifier(Inline.MOD_ID, "playerhead");
    }

    public Identifier textureFromHeadData(PlayerHeadData data){
        GameProfile prof = getBetterProfile(data.profile);
        Identifier skinTextId;
        if(prof == null){
            // get a steve head i guess
            skinTextId = DefaultSkinHelper.getTexture(Uuids.getUuidFromProfile(data.profile));
        } else {
            skinTextId = MinecraftClient.getInstance().getSkinProvider().loadSkin(prof);
        }
        return skinTextId;
    }

    public SpriteInlineData getFace(PlayerHeadData data){
        TextureSprite faceSprite = TextureSprite.fromPixels(textureFromHeadData(data),
            8, 8, 8, 8, 64, 64);
        return new SpriteInlineData(faceSprite);
    }

    public SpriteInlineData getOuter(PlayerHeadData data){
        TextureSprite faceSprite = TextureSprite.fromPixels(textureFromHeadData(data),
            40, 8, 8, 8, 64, 64);
        return new SpriteInlineData(faceSprite);
    }

    @Override
    public int render(PlayerHeadData data, DrawContext context, int index, Style style, int codepoint, TextRenderingContext trContext){
        SpriteInlineData faceSpriteData = getFace(data);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.25, 0.25, 0);
        matrices.scale(8/(float)8.5, 8/(float)8.5,1);
        InlineSpriteRenderer.INSTANCE.render(faceSpriteData, context, index, style, codepoint, trContext);
        matrices.pop();
        SpriteInlineData outerSpriteData = getOuter(data);
        matrices.push();
        matrices.translate(0, 0, 10);
        int res = InlineSpriteRenderer.INSTANCE.render(outerSpriteData, context, index, style, codepoint, trContext);
        matrices.pop();
        return res;
    }

    @Override
    public int charWidth(PlayerHeadData data, Style style, int codepoint){
        return 8;
    }


    public static final Map<UUID, Optional<GameProfile>> UUID_PROFILE_CACHE = new HashMap<>();
    public static final Map<String, Optional<GameProfile>> NAME_PROFILE_CACHE = new HashMap<>();

    @Nullable
    public static GameProfile getBetterProfile(GameProfile weakProf){
        // try to find the better profile in our caches
        if(weakProf.getId() != null){
            Optional<GameProfile> maybeProf = UUID_PROFILE_CACHE.get(weakProf.getId());
            if(maybeProf != null){
                if(maybeProf.isPresent() && maybeProf.get().getId() == null){
                    Inline.logPrint("null id in UUID cache: " + maybeProf.get().toString());
                }
                return maybeProf.orElse(null);
            }
        }
        if(weakProf.getName() != null && !weakProf.getName().equals("")){
            Optional<GameProfile> maybeProf = NAME_PROFILE_CACHE.get(weakProf.getName().toLowerCase());
            if(maybeProf != null){
                if(maybeProf.isPresent() && maybeProf.get().getId() == null){
                    Inline.logPrint("null id in name cache: " + maybeProf.get().toString());
                }
                return maybeProf.orElse(null);
            }
        }
        // can't find, try to fetch it
        // MinecraftClient.getInstance().getSessionService().fillProfileProperties(weakProf, false);

        // set these to empty optionals so we don't repeatedly fetch a ton
        if(weakProf.getId() != null)
            UUID_PROFILE_CACHE.put(weakProf.getId(), Optional.empty());
        if(weakProf.getName() != null && !weakProf.getName().equals(""))
            NAME_PROFILE_CACHE.put(weakProf.getName().toLowerCase(), Optional.empty());

        MinecraftClient client = MinecraftClient.getInstance();
        if(MixinClientHeadChecker.getSessionService() == null){
            ApiServices apiServices = ApiServices.create(((MixinClientAccessor)client).getAuthenticationService(), client.runDirectory);
            apiServices.userCache().setExecutor(client);
            SkullBlockEntity.setServices(apiServices, client);
        }

        SkullBlockEntity.loadProperties(weakProf, betterProf -> {
            Inline.logPrint(betterProf.toString());
            Property property = Iterables.getFirst(betterProf.getProperties().get(PlayerSkinProvider.TEXTURES), null);
            if(property == null){
                Inline.logPrint("null texture");
            } else {
                Inline.logPrint("Texture: "+ property.getValue());

            }
            if(betterProf.getId() != null){
                UUID_PROFILE_CACHE.put(betterProf.getId(), Optional.of(betterProf));
            }
            if(betterProf.getName() != null && !betterProf.getName().equals("")){
                NAME_PROFILE_CACHE.put(betterProf.getName().toLowerCase(), Optional.of(betterProf));
            }
        });
        return null;
    }
}
