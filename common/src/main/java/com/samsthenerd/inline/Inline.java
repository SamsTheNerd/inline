package com.samsthenerd.inline;

import java.util.UUID;
import java.util.regex.MatchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineMatchResult.DataMatch;
import com.samsthenerd.inline.api.InlineMatchResult.TextMatch;
import com.samsthenerd.inline.api.data.EntityInlineData;
import com.samsthenerd.inline.api.data.ItemInlineData;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import com.samsthenerd.inline.api.matchers.RegexMatcher;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.ItemStackContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

// this will probably be bumped out into its own mod Soon, but i want to get it working in this test environment first
public class Inline {

    public static final String MOD_ID = "inline";

    public static final Logger LOGGER = LoggerFactory.getLogger("inline");

	public static final void logPrint(String message){
		if(Platform.isDevelopmentEnvironment())
			LOGGER.info(message);
	}

    public static void onInitialize(){
        // nothing yet !
        addDefaultMatchers();
    }

    private static void addDefaultMatchers(){
        InlineAPI.INSTANCE.addMatcher(new Identifier(MOD_ID, "item"), new RegexMatcher.Simple("<item:([a-z:\\/_]+)>", (MatchResult mr) ->{
            Item item = Registries.ITEM.get(new Identifier(mr.group(1)));
            if(item == null) return null;
            ItemStack stack = new ItemStack(item);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ItemStackContent(stack));
            return new DataMatch(new ItemInlineData(stack), Style.EMPTY.withHoverEvent(he));
        }));

        InlineAPI.INSTANCE.addMatcher(new Identifier(MOD_ID, "entity"), new RegexMatcher.Simple("<entity:([a-z:\\/_]+)>", (MatchResult mr) ->{
            EntityType entType = Registries.ENTITY_TYPE.get(new Identifier(mr.group(1)));
            if(entType == null) return null;
            return new DataMatch(EntityInlineData.fromType(entType));
        }));

        InlineAPI.INSTANCE.addMatcher(new Identifier(MOD_ID, "link"), new RegexMatcher.Simple("\\[(.*)\\]\\((.*)\\)", (MatchResult mr) ->{
            String text = mr.group(1);
            String link = mr.group(2);
            ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, link);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(link));
            MutableText linkText = Text.literal(text + " 🔗");
            linkText.setStyle(Style.EMPTY.withClickEvent(ce).withHoverEvent(he).withUnderline(true).withColor(Formatting.BLUE));
            return new TextMatch(linkText);
        }));


        InlineAPI.INSTANCE.addMatcher(new Identifier(MOD_ID, "bolditalic"), new RegexMatcher.Simple("(?<ast>\\*{1,3})\\b([^*]+)(\\k<ast>)", (MatchResult mr) ->{
            String text = mr.group(2);
            int astCount = mr.group(1).length();
            MutableText linkText = Text.literal(text);
            linkText.setStyle(Style.EMPTY.withBold(astCount >= 2).withItalic(astCount % 2 == 1));
            return new TextMatch(linkText);
        }));

        InlineAPI.INSTANCE.addMatcher(new Identifier(MOD_ID, "modicon"), new RegexMatcher.Simple("<mod:([a-z:\\/_-]+)>", (MatchResult mr) -> {
            String modid = mr.group(1);
            try{
                Mod mod = Platform.getMod(modid);
                return new DataMatch(new ModIconData(modid), ModIconData.getTooltipStyle(modid));
            } catch (Exception e){
                Inline.LOGGER.error("error parsing modicon: " + modid);
                return null;
            }
        }));

        InlineAPI.INSTANCE.addMatcher(new Identifier(MOD_ID, "playerface"), new RegexMatcher.Simple("<face:([a-z:A-Z0-9\\/_-]+)>", (MatchResult mr) -> {
            String playerNameOrUUID = mr.group(1);
            GameProfile profile;
            try{
                profile = new GameProfile(UUID.fromString(playerNameOrUUID), null);
            } catch (IllegalArgumentException e){
                profile = new GameProfile(null, playerNameOrUUID);
            }
            return new DataMatch(new PlayerHeadData(profile));
        }));
    }
}
