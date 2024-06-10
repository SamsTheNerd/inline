package com.samsthenerd.inline;

import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.InlineMatch.DataMatch;
import com.samsthenerd.inline.api.client.MatcherInfo;
import com.samsthenerd.inline.api.client.matchers.RegexMatcher;
import com.samsthenerd.inline.api.client.matchers.RegexMatcher.Standard;
import com.samsthenerd.inline.api.client.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.client.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.api.client.renderers.SpriteInlineRenderer;
import com.samsthenerd.inline.api.data.EntityInlineData;
import com.samsthenerd.inline.api.data.ItemInlineData;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import com.samsthenerd.inline.xplat.IModMeta;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.ItemStackContent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

public class InlineClient {
    public static void initClient(){

        // InlineAutoConfig.init();

        addDefaultRenderers();
        addDefaultMatchers();
    }

    private static void addDefaultRenderers(){
        InlineClientAPI.INSTANCE.addRenderer(InlineItemRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineEntityRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(SpriteInlineRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(PlayerHeadRenderer.INSTANCE);
    }

    private static void addDefaultMatchers(){
        Identifier itemMatcherID = new Identifier(Inline.MOD_ID, "item");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("item", Standard.IDENTIFIER_REGEX, itemMatcherID, 
        (String itemId) ->{
            Item item = Registries.ITEM.get(new Identifier(itemId));
            if(item == null) return null;
            ItemStack stack = new ItemStack(item);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ItemStackContent(stack));
            return new DataMatch(new ItemInlineData(stack), Style.EMPTY.withHoverEvent(he));
        }, MatcherInfo.fromId(itemMatcherID)));

        Identifier entityMatcherID = new Identifier(Inline.MOD_ID, "entity");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("entity", Standard.IDENTIFIER_REGEX, entityMatcherID, 
        (String entityTypeId) ->{
            EntityType entType = Registries.ENTITY_TYPE.get(new Identifier(entityTypeId));
            if(entType == null) return null;
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

        // InlineClientAPI.INSTANCE.addMatcher(new Identifier(Inline.MOD_ID, "bolditalic"), new RegexMatcher.Simple("(?<ast>\\*{1,3})\\b([^*]+)(\\k<ast>)", (MatchResult mr) ->{
        //     String text = mr.group(2);
        //     int astCount = mr.group(1).length();
        //     MutableText linkText = Text.literal(text);
        //     linkText.setStyle(Style.EMPTY.withBold(astCount >= 2).withItalic(astCount % 2 == 1));
        //     return new TextMatch(linkText);
        // }));

        Identifier modMatcherId = new Identifier(Inline.MOD_ID, "modicon");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("mod", "[0-9a-z._-]+", modMatcherId, 
        (String modid) -> {
            Optional<IModMeta> maybeMod = IModMeta.getMod(modid);
            if(maybeMod.isEmpty()){
                return null;
            }
            // IModMeta mod = maybeMod.get();
            return new DataMatch(new ModIconData(modid), ModIconData.getTooltipStyle(modid));
        }, MatcherInfo.fromId(modMatcherId)));

        Identifier faceMatcherId = new Identifier(Inline.MOD_ID, "playerface");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("face", "[a-zA-Z0-9_]{1,16}", faceMatcherId, 
        (String playerNameOrUUID) -> {
            GameProfile profile;
            try{
                profile = new GameProfile(UUID.fromString(playerNameOrUUID), null);
            } catch (IllegalArgumentException e){
                profile = new GameProfile(null, playerNameOrUUID);
            }
            PlayerHeadData headData = new PlayerHeadData(profile);
            return new DataMatch(headData, Style.EMPTY.withHoverEvent(headData.getEntityDisplayHoverEvent()));
        }, MatcherInfo.fromId(faceMatcherId)));
    }
}
