package com.samsthenerd.inline.config;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.matching.MatcherInfo;

import com.samsthenerd.inline.xplat.IModMeta;
import com.samsthenerd.inline.xplat.IXPlatAbstractions;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/*
 * Should only run on client
 */
public class InlineConfigHandler {
    public static Screen getConfigScreen(Screen parent){
        // return AutoConfig.getConfigScreen(InlineAutoConfig.class, parent).get();

        InlineClientConfigImpl.getInstance().reloadFromFile(); // make sure we're up to date

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.inline.title"));
        
        ConfigCategory matcherConfig = builder.getOrCreateCategory(Text.translatable("config.inline.category.matchers"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        for(InlineMatcher matcher : InlineClientAPI.INSTANCE.getAllMatchers()){
            matcherConfig.addEntry(makeMatcherConfig(entryBuilder, matcher));
        }

        ConfigCategory extraFeatures = builder.getOrCreateCategory(Text.translatable("config.inline.category.extras"));

        BooleanListEntry modIconEntry = entryBuilder.startBooleanToggle(Text.translatable("config.inline.extras.modicon"), 
            InlineClientConfigImpl.getInstance().shouldRenderModIcons())
            .setTooltip(Text.translatable("config.inline.extras.modicon.desc"))
            .setDefaultValue(true)
            .setYesNoTextSupplier((boolval) -> boolval ? 
                Text.translatable("addServer.resourcePack.enabled").setStyle(Style.EMPTY.withColor(Formatting.GREEN)) : 
                Text.translatable("addServer.resourcePack.disabled").setStyle(Style.EMPTY.withColor(Formatting.RED))
            )
            .setSaveConsumer((enabled) -> InlineClientConfigImpl.getInstance().setShouldRenderModIcons(enabled))
            .build();

        extraFeatures.addEntry(modIconEntry);

        DoubleListEntry chatCapField = entryBuilder.startDoubleField(Text.translatable("config.inline.extras.chatcap"),
                InlineClientConfigImpl.getInstance().maxChatSizeModifier())
                .setTooltip(Text.translatable("config.inline.extras.chatcap.desc"))
                .setDefaultValue(1.5).setMin(1.0).setMax(2.0)
                .setSaveConsumer((newCap) -> InlineClientConfigImpl.getInstance().setChatScaleCap(newCap))
                .build();
        extraFeatures.addEntry(chatCapField);

        if(IModMeta.getMod("create").isPresent()){
            BooleanListEntry createToggleEntry = entryBuilder.startBooleanToggle(Text.translatable("config.inline.extras.createinterop"),
                            InlineClientConfigImpl.getInstance().shouldDoCreateMixins())
                    .setTooltip(Text.translatable("config.inline.extras.createinterop.desc"))
                    .setDefaultValue(true)
                    .setYesNoTextSupplier((boolval) -> boolval ?
                            Text.translatable("addServer.resourcePack.enabled").setStyle(Style.EMPTY.withColor(Formatting.GREEN)) :
                            Text.translatable("addServer.resourcePack.disabled").setStyle(Style.EMPTY.withColor(Formatting.RED))
                    )
                    .setSaveConsumer((enabled) -> InlineClientConfigImpl.getInstance().setShouldDoCreateMixins(enabled))
                    .build();
            extraFeatures.addEntry(createToggleEntry);
        }

        builder.setSavingRunnable(() -> InlineClientConfigImpl.getInstance().save());

        return builder.build();
    }

    private static AbstractConfigListEntry makeMatcherConfig(ConfigEntryBuilder entryBuilder, InlineMatcher matcher){
        MatcherInfo info = matcher.getInfo();
        // SubCategoryBuilder subBuilder = entryBuilder.startSubCategory(info.getTitle()).setExpanded(true);

        boolean matcherEnabled = InlineClientConfigImpl.getInstance().isMatcherEnabled(matcher.getId());
        return entryBuilder.startBooleanToggle(info.getTitle(matcherEnabled), matcherEnabled)
            .setTooltip(info.getDescription())
            .setDefaultValue(true)
            .setYesNoTextSupplier((boolval) -> boolval ? 
                Text.translatable("addServer.resourcePack.enabled").setStyle(Style.EMPTY.withColor(Formatting.GREEN)) : 
                Text.translatable("addServer.resourcePack.disabled").setStyle(Style.EMPTY.withColor(Formatting.RED))
            )
            .setSaveConsumer((enabled) -> InlineClientConfigImpl.getInstance().someableMatcher(matcher.getId(), enabled))
            .build();

    }
}
