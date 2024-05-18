package com.samsthenerd.inline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.MatchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.api.InlineMatchResult.DataMatch;
import com.samsthenerd.inline.api.InlineMatchResult.TextMatch;
import com.samsthenerd.inline.api.InlineMatcher;
import com.samsthenerd.inline.api.InlineRenderer;
import com.samsthenerd.inline.api.data.EntityInlineData;
import com.samsthenerd.inline.api.data.ItemInlineData;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import com.samsthenerd.inline.api.matchers.RegexMatcher;
import com.samsthenerd.inline.api.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.api.renderers.SpriteInlineRenderer;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
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
    private static final Map<Identifier, InlineRenderer<?>> RENDERERS = new HashMap<>();
    private static final Map<Identifier, InlineMatcher> MATCHERS = new HashMap<>();

    public static final String MOD_ID = "inline";

    public static final Logger LOGGER = LoggerFactory.getLogger("inline");

	public static final void logPrint(String message){
		if(Platform.isDevelopmentEnvironment())
			LOGGER.info(message);
	}

    public static void onInitialize(){
        // nothing yet !
    }

    static {
        addMatcher(new Identifier(MOD_ID, "item"), new RegexMatcher.Simple("<item:([a-z:\\/_]+)>", (MatchResult mr) ->{
            Item item = Registries.ITEM.get(new Identifier(mr.group(1)));
            if(item == null) return null;
            ItemStack stack = new ItemStack(item);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ItemStackContent(stack));
            return new DataMatch(new ItemInlineData(stack), Style.EMPTY.withHoverEvent(he));
        }));

        addMatcher(new Identifier(MOD_ID, "entity"), new RegexMatcher.Simple("<entity:([a-z:\\/_]+)>", (MatchResult mr) ->{
            EntityType entType = Registries.ENTITY_TYPE.get(new Identifier(mr.group(1)));
            if(entType == null) return null;
            Entity ent = entType.create(MinecraftClient.getInstance().world); // TODO: don't ship this
            return new DataMatch(new EntityInlineData(ent));
        }));

        addMatcher(new Identifier(MOD_ID, "link"), new RegexMatcher.Simple("\\[(.*)\\]\\((.*)\\)", (MatchResult mr) ->{
            String text = mr.group(1);
            String link = mr.group(2);
            ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, link);
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(link));
            MutableText linkText = Text.literal(text + " 🔗");
            linkText.setStyle(Style.EMPTY.withClickEvent(ce).withHoverEvent(he).withUnderline(true).withColor(Formatting.BLUE));
            return new TextMatch(linkText);
        }));


        addMatcher(new Identifier(MOD_ID, "bolditalic"), new RegexMatcher.Simple("(?<ast>\\*{1,3})\\b([^*]+)(\\k<ast>)", (MatchResult mr) ->{
            String text = mr.group(2);
            int astCount = mr.group(1).length();
            MutableText linkText = Text.literal(text);
            linkText.setStyle(Style.EMPTY.withBold(astCount >= 2).withItalic(astCount % 2 == 1));
            return new TextMatch(linkText);
        }));

        addMatcher(new Identifier(MOD_ID, "modicon"), new RegexMatcher.Simple("<mod:([a-z:\\/_-]+)>", (MatchResult mr) -> {
            String modid = mr.group(1);
            try{
                Mod mod = Platform.getMod(modid);
                return new DataMatch(new ModIconData(modid), ModIconData.getTooltipStyle(modid));
            } catch (Exception e){
                Inline.LOGGER.error("error parsing modicon: " + modid);
                return null;
            }
        }));

        addMatcher(new Identifier(MOD_ID, "playerface"), new RegexMatcher.Simple("<face:([a-z:A-Z0-9\\/_-]+)>", (MatchResult mr) -> {
            String playerNameOrUUID = mr.group(1);
            GameProfile profile;
            try{
                profile = new GameProfile(UUID.fromString(playerNameOrUUID), null);
            } catch (IllegalArgumentException e){
                profile = new GameProfile(null, playerNameOrUUID);
            }
            return new DataMatch(new PlayerHeadData(profile));
        }));

        addRenderer(InlineItemRenderer.INSTANCE);
        addRenderer(InlineEntityRenderer.INSTANCE);
        addRenderer(SpriteInlineRenderer.INSTANCE);
        addRenderer(PlayerHeadRenderer.INSTANCE);
    }

    public static void addMatcher(Identifier id, InlineMatcher matcher){
        MATCHERS.put(id, matcher);
    }

    public static void addRenderer(InlineRenderer<?> renderer){
        if(RENDERERS.containsKey(renderer.getId())){
            Inline.LOGGER.error("renderer with id " + renderer.getId().toString() + " already exists");
            return;
        }
        RENDERERS.put(renderer.getId(), renderer);
    }

    public static Set<InlineMatcher> getMatchers(){
        return new HashSet<>(MATCHERS.values());
    }

    public static InlineMatcher getMatcher(Identifier id){
        return MATCHERS.get(id);
    }

    public static InlineRenderer getRenderer(Identifier id){
        if(RENDERERS.get(id) == null) {
            Inline.logPrint("couldn't find renderer: " + id.toString());
            Inline.logPrint("available renderers: ");
            for(Identifier i : RENDERERS.keySet()){
                Inline.logPrint("\t-" + i.toString());
            }
        }
        return RENDERERS.get(id);
    }
}
