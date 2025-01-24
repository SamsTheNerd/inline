package com.samsthenerd.inline;

import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineSpriteRenderer;
import com.samsthenerd.inline.api.client.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.api.data.EntityInlineData;
import com.samsthenerd.inline.api.data.ItemInlineData;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import com.samsthenerd.inline.api.matching.InlineMatch.DataMatch;
import com.samsthenerd.inline.api.matching.MatcherInfo;
import com.samsthenerd.inline.api.matching.RegexMatcher;
import com.samsthenerd.inline.api.matching.RegexMatcher.Standard;
import com.samsthenerd.inline.utils.cradles.GameProfileish;
import com.samsthenerd.inline.xplat.IModMeta;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.ItemStackContent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class InlineClient {
    public static void initClient(){

        // InlineAutoConfig.init();

        addDefaultRenderers();
        addDefaultMatchers();
    }

    private static void addDefaultRenderers(){
        InlineClientAPI.INSTANCE.addRenderer(InlineItemRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineEntityRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineSpriteRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(PlayerHeadRenderer.INSTANCE);
    }

    private static void addDefaultMatchers(){
        Identifier itemMatcherID = Inline.id( "item");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("item", Standard.IDENTIFIER_REGEX_INSENSITIVE, itemMatcherID,
        (String itemId) ->{
            Identifier itemActualId = Identifier.of(itemId.toLowerCase());
            if(!Registries.ITEM.containsId(itemActualId)) return null;
            Item item = Registries.ITEM.get(itemActualId);
            ItemStack stack = new ItemStack(item);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ItemStackContent(stack));
            return new DataMatch(new ItemInlineData(stack), Style.EMPTY.withHoverEvent(he));
        }, MatcherInfo.fromId(itemMatcherID)));

        Identifier entityMatcherID = Inline.id( "entity");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("entity", Standard.IDENTIFIER_REGEX_INSENSITIVE, entityMatcherID,
        (String entityTypeId) ->{
            Identifier entTypeActualId = Identifier.of(entityTypeId.toLowerCase());
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

        // InlineClientAPI.INSTANCE.addMatcher(new Identifier(Inline.MOD_ID, "bolditalic"), new RegexMatcher.Simple("(?<ast>\\*{1,3})\\b([^*]+)(\\k<ast>)", (MatchResult mr) ->{
        //     String text = mr.group(2);
        //     int astCount = mr.group(1).length();
        //     MutableText linkText = Text.literal(text);
        //     linkText.setStyle(Style.EMPTY.withBold(astCount >= 2).withItalic(astCount % 2 == 1));
        //     return new TextMatch(linkText);
        // }));

        Identifier modMatcherId = Inline.id( "modicon");
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

        Identifier faceMatcherId = Inline.id( "playerface");
        InlineClientAPI.INSTANCE.addMatcher(new RegexMatcher.Standard("face", "[a-zA-Z0-9_]{1,16}", faceMatcherId, 
        (String playerName) -> {
            PlayerHeadData headData = new PlayerHeadData(new GameProfileish(playerName));
            return new DataMatch(headData, Style.EMPTY.withHoverEvent(headData.getEntityDisplayHoverEvent()));
        }, MatcherInfo.fromId(faceMatcherId)));
    }
}
