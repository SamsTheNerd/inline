package com.samsthenerd.inline;

import java.util.Optional;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.extrahooks.ItemOverlayRenderer;
import com.samsthenerd.inline.api.data.*;
import com.samsthenerd.inline.api.matching.InlineMatch.DataMatch;
import com.samsthenerd.inline.api.matching.InlineMatch.TextMatch;
import com.samsthenerd.inline.api.matching.MatcherInfo;
import com.samsthenerd.inline.api.matching.RegexMatcher;
import com.samsthenerd.inline.api.matching.RegexMatcher.Standard;
import com.samsthenerd.inline.api.client.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineSpriteRenderer;
import com.samsthenerd.inline.api.client.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.impl.extrahooks.ItemOverlayManager;
import com.samsthenerd.inline.utils.Spritelike;
import com.samsthenerd.inline.utils.SpritelikeRenderers;
import com.samsthenerd.inline.utils.SpritelikeUtils;
import com.samsthenerd.inline.utils.URLSprite;
import com.samsthenerd.inline.xplat.IModMeta;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.*;
import net.minecraft.text.HoverEvent.ItemStackContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class InlineClient {
    public static void initClient(){

        // InlineAutoConfig.init();

        addDefaultRenderers();
        addDefaultMatchers();

        addExtraHookTests();
    }

    private static void addDefaultRenderers(){
        InlineClientAPI.INSTANCE.addRenderer(InlineItemRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineEntityRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineSpriteRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(PlayerHeadRenderer.INSTANCE);
    }

    private static void addDefaultMatchers(){
        Identifier itemMatcherID = new Identifier(Inline.MOD_ID, "item");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("item", Standard.IDENTIFIER_REGEX_INSENSITIVE, itemMatcherID,
        (String itemId) ->{
            Identifier itemActualId = new Identifier(itemId.toLowerCase());
            if(!Registries.ITEM.containsId(itemActualId)) return null;
            Item item = Registries.ITEM.get(itemActualId);
            ItemStack stack = new ItemStack(item);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ItemStackContent(stack));
            return new DataMatch(new ItemInlineData(stack), Style.EMPTY.withHoverEvent(he));
        }, MatcherInfo.fromId(itemMatcherID)));

        Identifier entityMatcherID = new Identifier(Inline.MOD_ID, "entity");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("entity", Standard.IDENTIFIER_REGEX_INSENSITIVE, entityMatcherID,
        (String entityTypeId) ->{
            Identifier entTypeActualId = new Identifier(entityTypeId.toLowerCase());
            if(!Registries.ENTITY_TYPE.containsId(entTypeActualId)) return null;
            EntityType entType = Registries.ENTITY_TYPE.get(entTypeActualId);
            EntityInlineData entData = EntityInlineData.fromType(entType);
            return new DataMatch(entData, Style.EMPTY.withHoverEvent(entData.getEntityDisplayHoverEvent()));
        }, MatcherInfo.fromId(entityMatcherID)));

        /*
        Identifier linkMatcherId = new Identifier(Inline.MOD_ID, "link");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Simple("\\[(.*)\\]\\((.*)\\)", linkMatcherId, (MatchResult mr) ->{
            String text = mr.group(1);
            String link = mr.group(2);
            ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, link);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(link));
            MutableText linkText = Text.literal(text + " 🔗");
            linkText.setStyle(Style.EMPTY.withClickEvent(ce).withHoverEvent(he).withUnderline(true).withColor(Formatting.BLUE));
            return new TextMatch(linkText);
        }, MatcherInfo.fromId(linkMatcherId)));
        */
//
//        Identifier imgMatcherId = new Identifier(Inline.MOD_ID, "imgtest");
//        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("img", "[^\\[\\]]+", imgMatcherId,
//            (String url) -> {
//                var urlId = url.chars()
//                    .mapToObj(ch -> (char)ch)
//                    .filter(c -> c != ':' && Identifier.isCharValid(c))
//                    .collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append)
//                    .toString();
//                return new DataMatch(new SpriteInlineData(new URLSprite(url, new Identifier(urlId))));
//            }, MatcherInfo.fromId(imgMatcherId)));

        // InlineClientAPI.INSTANCE.addMatcher(new Identifier(Inline.MOD_ID, "bolditalic"), new RegexMatcher.Simple("(?<ast>\\*{1,3})\\b([^*]+)(\\k<ast>)", (MatchResult mr) ->{
        //     String text = mr.group(2);
        //     int astCount = mr.group(1).length();
        //     MutableText linkText = Text.literal(text);
        //     linkText.setStyle(Style.EMPTY.withBold(astCount >= 2).withItalic(astCount % 2 == 1));
        //     return new TextMatch(linkText);
        // }));

        Identifier modMatcherId = new Identifier(Inline.MOD_ID, "modicon");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("mod", "[0-9A-Za-z._-]+", modMatcherId,
        (String modid) -> {
            String modidLowercase = modid.toLowerCase();
            Optional<IModMeta> maybeMod = IModMeta.getMod(modidLowercase);
            if(maybeMod.isEmpty()){
                return null;
            }
            // IModMeta mod = maybeMod.get();
            return new DataMatch(new ModIconData(modidLowercase), ModIconData.getTooltipStyle(modidLowercase));
        }, MatcherInfo.fromId(modMatcherId)));

        Identifier faceMatcherId = new Identifier(Inline.MOD_ID, "playerface");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("face", "[a-zA-Z0-9_]{1,16}", faceMatcherId, 
        (String playerNameOrUUID) -> {
            GameProfile profile = new GameProfile(null, playerNameOrUUID);
            PlayerHeadData headData = new PlayerHeadData(profile);
            return new DataMatch(headData, Style.EMPTY.withHoverEvent(headData.getEntityDisplayHoverEvent()));
        }, MatcherInfo.fromId(faceMatcherId)));
    }

    private static void addExtraHookTests(){
        // these shouldn't be in the mod probably :p but this is how to use them.
//        ItemOverlayManager.addRenderer(Items.COOKIE, new ItemOverlayRenderer() {
//            @Override
//            public void render(ItemStack stack, DrawContext drawContext) {
//                Spritelike cookiemonster = new URLSprite("https://easydrawingguides.com/wp-content/uploads/2019/01/Cookie-Monster-10.png",
//                    Identifier.of("cookiemonster", "cookiermonster"));
//                SpritelikeRenderers.getRenderer(cookiemonster).drawSprite(cookiemonster, drawContext, 0, 6, 100, 10, 10);
//            }
//        });
//        ItemOverlayManager.addRenderer(Items.CHEST, new ItemOverlayRenderer() {
//            @Override
//            public void render(ItemStack stack, DrawContext drawContext) {
//                Spritelike cookiemonster = new URLSprite("https://easydrawingguides.com/wp-content/uploads/2019/01/Cookie-Monster-10.png",
//                    Identifier.of("cookiemonster", "cookiermonster"));
//                SpritelikeRenderers.getRenderer(cookiemonster).drawSprite(cookiemonster, drawContext, 0, 6, 100, 10, 10);
//            }
//        });
////
//        ItemOverlayManager.addRenderer(Items.POTION, new ItemOverlayRenderer() {
//            @Override
//            public void render(ItemStack stack, DrawContext drawContext) {
//
//                var effects = PotionUtil.getPotionEffects(stack);
//                if(effects.isEmpty()) return;
//                var effect = effects.get(0);
//                var effectSprite = MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(effect.getEffectType());
//                Spritelike effectSpritelike = SpritelikeUtils.spritelikeFromSprite(effectSprite);
//
//                SpritelikeRenderers.getRenderer(effectSpritelike).drawSprite(effectSpritelike, drawContext, 7, 7, 100, 9, 9);
//            }
//        });
    }
}
