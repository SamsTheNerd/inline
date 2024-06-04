package com.samsthenerd.inline.tooltips;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomTooltipManager {

    public static final Item HIJACKED_ITEM = Items.FEATHER;

    public static <T> ItemStack getForTooltip(CustomTooltipProvider<T> provider, T content){
        ItemStack stack = new ItemStack(HIJACKED_ITEM);
        NbtCompound tag = new NbtCompound();
        tag.putString("id", provider.getId().toString());
        tag.put("data", provider.getTag(content));
        stack.setSubNbt("inlinecustomtooltip", tag);
        return stack;
    }

    @Nullable
    public static CustomTooltipProvider<?> getProvider(ItemStack stack){
        NbtCompound tag = stack.getSubNbt("inlinecustomtooltip");
        // we're throwing this all in a try/catch because it's good enough 
        try{
            return PROVIDERS.get(new Identifier(tag.getString("id")));
        } catch(Exception e) {
            return null;
        }
    }

    // null indicates no override
    @Nullable
    public static List<Text> getTooltipText(ItemStack stack){
        try{
            NbtCompound tag = stack.getSubNbt("inlinecustomtooltip");
            return getProvider(stack).getTooltipText(tag.getCompound("data"));
        } catch(Exception e) {
            return null;
        }
    }

    @Nullable
    public static Optional<TooltipData> getTooltipData(ItemStack stack){
        try{
            NbtCompound tag = stack.getSubNbt("inlinecustomtooltip");
            return getProvider(stack).getTooltipData(tag.getCompound("data"));
        } catch(Exception e) {
            return null;
        }
    }

    private static final Map<Identifier, CustomTooltipProvider> PROVIDERS = new HashMap<>();

    public static <T> CustomTooltipProvider<T> registerProvider(CustomTooltipProvider<T> provider){
        PROVIDERS.put(provider.getId(), provider);
        return provider;
    }

    // T can kinda be whatever, it's just *something* so that we can 
    public static interface CustomTooltipProvider<T>{
        public Identifier getId();

        @NotNull
        public List<Text> getTooltipText(NbtCompound tag);

        @NotNull
        public Optional<TooltipData> getTooltipData(NbtCompound tag);

        public NbtCompound getTag(T content);
    }
}
