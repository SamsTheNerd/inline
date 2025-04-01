package com.samsthenerd.inline;

import com.samsthenerd.inline.api.data.ItemInlineData;
import com.samsthenerd.inline.api.matching.InlineMatch;
import com.samsthenerd.inline.api.matching.MatcherInfo;
import com.samsthenerd.inline.api.matching.RegexMatcher;
import com.samsthenerd.inline.api.matching.RegexMatcher.Standard;
import com.samsthenerd.inline.xplat.XPlatInstances;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.data.EntityInlineData.EntityDataType;
import com.samsthenerd.inline.api.data.ItemInlineData.ItemDataType;
import com.samsthenerd.inline.api.data.ModIconData.ModIconDataType;
import com.samsthenerd.inline.api.data.PlayerHeadData.PlayerHeadDataType;
import com.samsthenerd.inline.api.data.SpriteInlineData.SpriteDataType;

import java.util.Map;

// this will probably be bumped out into its own mod Soon, but i want to get it working in this test environment first
public class Inline {

    public static final String MOD_ID = "inline";

    public static final Logger LOGGER = LoggerFactory.getLogger("inline");

    public static XPlatInstances getXPlats(){
        return xPlats;
    }

	public static final void logPrint(String message){
        // if(IXPlatAbstractions.getInstance().isDevEnv())
			LOGGER.info(message);
	}

    private static XPlatInstances xPlats;

    public static void onInitialize(XPlatInstances xPlats){
        // nothing yet !
        Inline.xPlats = xPlats;
        registerDataTypes();
        addChatMatchers();
    }

    private static void registerDataTypes(){
        InlineAPI.INSTANCE.addDataType(EntityDataType.INSTANCE);
        InlineAPI.INSTANCE.addDataType(ItemDataType.INSTANCE);
        InlineAPI.INSTANCE.addDataType(ModIconDataType.INSTANCE);
        InlineAPI.INSTANCE.addDataType(PlayerHeadDataType.INSTANCE);
        InlineAPI.INSTANCE.addDataType(SpriteDataType.INSTANCE);
    }

    private static final Map<String, String> EQUIPMENT_ALIASES = Map.of(
        "hand", "mainhand", "chestplate", "chest", "leggings", "legs", "helmet", "head",
        "boots", "feet", "shoes", "feet"
    );
    private static void addChatMatchers(){
        Identifier showOffID = Identifier.of(Inline.MOD_ID, "showoff");
        RegexMatcher showOffMatcher = new RegexMatcher.ChatStandard("show", Standard.IDENTIFIER_REGEX_INSENSITIVE, showOffID,
                (whatToShow, ctx) -> {
                    ItemStack stack = null;
                    whatToShow = EQUIPMENT_ALIASES.getOrDefault(whatToShow, whatToShow);
                    try{
                        stack = ctx.getChatSender().getEquippedStack(EquipmentSlot.byName(whatToShow));
                    } catch (Exception e){}
                    if(stack == null || stack.isEmpty()){
                        stack = new ItemStack(Items.BARRIER);
                        stack.setCustomName(Text.translatable("ui.inline.nothingtoshowoff"));
                    }
                    HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack));
                    return new InlineMatch.DataMatch(new ItemInlineData(stack), Style.EMPTY.withHoverEvent(he));
                },
                MatcherInfo.fromId(showOffID)
        );
        InlineAPI.INSTANCE.addChatMatcher(showOffMatcher);
    }
}
