package com.samsthenerd.inline.tooltips.providers;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.tooltips.CustomTooltipManager.CustomTooltipProvider;
import com.samsthenerd.inline.tooltips.data.SpriteTooltipData;
import com.samsthenerd.inline.utils.Spritelike;
import com.samsthenerd.inline.xplat.IModMeta;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModDataTTProvider implements CustomTooltipProvider<IModMeta>{

    public static final ModDataTTProvider INSTANCE = new ModDataTTProvider();

    @Override
    public Identifier getId(){
        return Inline.id("moddata");
    }

    @Override
    @NotNull
    public List<Text> getTooltipText(IModMeta mod){
        List<Text> modInfo = new ArrayList<>();
        if(mod == null) return modInfo;
        MutableText title = Text.literal(mod.getName()).setStyle(Style.EMPTY.withBold(true));
        MutableText description = Text.literal(mod.getDescription().replace("\n", "")).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        modInfo.add(title);
        modInfo.add(description);
        return modInfo;
    }

    @Override
    @NotNull
    public Optional<TooltipData> getTooltipData(IModMeta mod){
        if(mod == null) return Optional.empty();

        Spritelike iconSprite = ModIconData.spriteFromModid(mod.getModId(), false);
        if(iconSprite == null) return Optional.empty();
        return Optional.of(new SpriteTooltipData(iconSprite, (w, h) -> 32));
    }

    @Override
    @NotNull
    public Codec<IModMeta> getCodec(){
        return Codec.STRING.xmap(
            modid -> IModMeta.getMod(modid).orElse(null),
            IModMeta::getModId
        );
    }
}
