package com.samsthenerd.inline.tooltips.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.tooltips.CustomTooltipManager.CustomTooltipProvider;
import com.samsthenerd.inline.tooltips.data.EntityDisplayTTData;
import com.samsthenerd.inline.utils.EntityCradle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EntityTTProvider implements CustomTooltipProvider<EntityCradle> {
    public static final EntityTTProvider INSTANCE = new EntityTTProvider();

    public Identifier getId(){
        return new Identifier(Inline.MOD_ID, "entitydisplay");
    }

    @NotNull
    public List<Text> getTooltipText(NbtCompound tag){
        List<Text> ttText = new ArrayList<>();
        EntityCradle cradle = fromTag(tag);
        Entity ent = cradle.getEntity(MinecraftClient.getInstance().world);
        if(ent != null) ttText.add(ent.getName());
        return ttText;
    }

    @NotNull
    public Optional<TooltipData> getTooltipData(NbtCompound tag){
        EntityCradle cradle = fromTag(tag);
        return Optional.of(new EntityDisplayTTData(cradle, (w,h) -> h == 0 ? 0 : w * 96 /h ));
    }

    @Nullable
    private EntityCradle fromTag(NbtCompound tag){
        NbtElement cradleElem = tag.get("cradle");
        return EntityCradle.CRADLE_CODEC.parse(NbtOps.INSTANCE, cradleElem).getOrThrow(false, Inline.LOGGER::error);
    }

    public NbtCompound getTag(EntityCradle cradle){
        NbtElement cradleElem = EntityCradle.CRADLE_CODEC.encodeStart(NbtOps.INSTANCE, cradle).getOrThrow(false, Inline.LOGGER::error);
        NbtCompound tag = new NbtCompound();
        tag.put("cradle", cradleElem);
        return tag;
    }
}
