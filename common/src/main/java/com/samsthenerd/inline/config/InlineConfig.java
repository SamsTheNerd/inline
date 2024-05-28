package com.samsthenerd.inline.config;

import com.samsthenerd.inline.api.InlineClientAPI;
import com.samsthenerd.inline.api.InlineMatcher;
import com.samsthenerd.inline.api.MatcherInfo;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/*
 * Should only run on client
 */
public class InlineConfig {
    public static Screen getConfigScreen(Screen parent){
        // return AutoConfig.getConfigScreen(InlineAutoConfig.class, parent).get();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.inline.title"));
        
        ConfigCategory matcherConfig = builder.getOrCreateCategory(Text.translatable("config.inline.category.matchers"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        for(InlineMatcher matcher : InlineClientAPI.INSTANCE.getAllMatchers().values()){
            matcherConfig.addEntry(makeMatcherConfig(entryBuilder, matcher));
        }

        return builder.build();
    }

    private static AbstractConfigListEntry makeMatcherConfig(ConfigEntryBuilder entryBuilder, InlineMatcher matcher){
        MatcherInfo info = matcher.getInfo();
        // SubCategoryBuilder subBuilder = entryBuilder.startSubCategory(info.getTitle()).setExpanded(true);

        return entryBuilder.startBooleanToggle(info.getTitle().copy().append(Text.of(" ")).append(info.getExample()), true)
            .setYesNoTextSupplier((boolval) -> boolval ? 
                Text.translatable("addServer.resourcePack.enabled").setStyle(Style.EMPTY.withColor(Formatting.GREEN)) : 
                Text.translatable("addServer.resourcePack.disabled").setStyle(Style.EMPTY.withColor(Formatting.RED))
            )
            .build();

    }
}
