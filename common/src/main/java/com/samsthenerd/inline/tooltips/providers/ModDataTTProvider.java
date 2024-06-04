package com.samsthenerd.inline.tooltips.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.tooltips.CustomTooltipManager.CustomTooltipProvider;
import com.samsthenerd.inline.tooltips.data.SpriteTooltipData;
import com.samsthenerd.inline.utils.Spritelike;
import com.samsthenerd.inline.xplat.IModMeta;

import net.minecraft.client.item.TooltipData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ModDataTTProvider implements CustomTooltipProvider<IModMeta>{

    public static final ModDataTTProvider INSTANCE = new ModDataTTProvider();

    public Identifier getId(){
        return new Identifier(Inline.MOD_ID, "moddata");
    }

    @NotNull
    public List<Text> getTooltipText(NbtCompound tag){
        List<Text> modInfo = new ArrayList<>();
        IModMeta mod = fromTag(tag);
        if(mod == null) return modInfo;
        MutableText title = Text.literal(mod.getName()).setStyle(Style.EMPTY.withBold(true));
        MutableText description = Text.literal(mod.getDescription()).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        modInfo.add(title);
        modInfo.add(description);
        return modInfo;
    }

    @NotNull
    public Optional<TooltipData> getTooltipData(NbtCompound tag){
        IModMeta mod = fromTag(tag);
        if(mod == null) return Optional.empty();

        Spritelike iconSprite = ModIconData.spriteFromModid(mod.getModId(), false);
        if(iconSprite == null) return Optional.empty();
        return Optional.of(new SpriteTooltipData(iconSprite, (w, h) -> 32));
    }

    @Nullable
    private IModMeta fromTag(NbtCompound tag){
        try{
            String modid = tag.getString("modid");
            return IModMeta.getMod(modid).orElse(null);
        } catch (Exception e){
            return null;
        }
    }

    public NbtCompound getTag(IModMeta mod){
        NbtCompound tag = new NbtCompound();
        tag.putString("modid", mod.getModId());
        return tag;
    }
}
