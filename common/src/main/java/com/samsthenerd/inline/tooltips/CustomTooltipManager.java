package com.samsthenerd.inline.tooltips;

import com.mojang.serialization.Codec;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * An item-less custom tooltip system.
 * <p>
 * By default Minecraft only supports 3 types of {@link HoverEvent}s:
 * Text, Item, and Entity (which is kinda just text again). But, Item tooltips
 * have a pretty good tooltip system with {@link TooltipData} and {@link TooltipComponent}s.
 * Unfortunately, using those requires adding a new item, which is rather inconvenient, so
 * instead we hijack an arbitrary vanilla item to throw our tooltip system on. 
 * <p>
 * You can use these tooltips in hover events by making an item based hover event
 * out of the dummy itemstack from {@link CustomTooltipManager#getForTooltip(CustomTooltipProvider, Object)}.
 * <p>
 * Note that this isn't necessarily a part of the core Inline API, just a useful system
 * used for the built in player facing features.
 * @see CustomTooltipProvider
 */
public class CustomTooltipManager {

    public static final Item HIJACKED_ITEM = Items.FEATHER;

    /**
     * Gets a dummy itemstack hijacked with the data to display the tooltip 
     * provided by the provider for the given content. 
     * @param <T> Type of the content that the provider handles
     * @param provider A tooltip provider that can handle the given content.
     * @param content Arbitrary data to be handled by the provider.
     * @return an itemstack that will have a tooltip given by our provider and content.
     */
    //TODO: Test this on a dedicated server
    public static <T> ItemStack getForTooltip(CustomTooltipProvider<T> provider, T content){
        ItemStack stack = new ItemStack(HIJACKED_ITEM);
        List<Text> tooltipText = new ArrayList<>(provider.getTooltipText(content));
        if (!tooltipText.isEmpty()) {
            stack.set(DataComponentTypes.ITEM_NAME, tooltipText.removeFirst());
            if (!tooltipText.isEmpty()) {
                stack.set(DataComponentTypes.LORE, new LoreComponent(tooltipText));
            }
        }
        return stack;
    }

    @Nullable
    public static CustomTooltipProvider<?> getProvider(ItemStack stack){
//        NbtCompound tag = stack.getSubNbt("inlinecustomtooltip");
        // we're throwing this all in a try/catch because it's good enough 
        try{
//            return PROVIDERS.get(Identifier.of(tag.getString("id")));
            return null;
        } catch(Exception e) {
            return null;
        }
    }

    @Nullable
    public static <T> List<Text> getTooltipText(ItemStack stack){
        try{
//            NbtCompound tag = stack.getSubNbt("inlinecustomtooltip");
//            CustomTooltipProvider<T> provider = (CustomTooltipProvider<T>)getProvider(stack);
//            DataResult<T> contentRes = provider.getCodec().parse(NbtOps.INSTANCE, tag.get("data"));
//            T content = contentRes.resultOrPartial(Inline.LOGGER::error).orElseThrow();
//            return provider.getTooltipText(content);
            return null;
        } catch(Exception e) {
            return null;
        }
    }

    @Nullable
    public static <T> Optional<TooltipData> getTooltipData(ItemStack stack){
        try{
//            NbtCompound tag = stack.getSubNbt("inlinecustomtooltip");
//            CustomTooltipProvider<T> provider = (CustomTooltipProvider<T>)getProvider(stack);
//            DataResult<T> contentRes = provider.getCodec().parse(NbtOps.INSTANCE, tag.get("data"));
//            T content = contentRes.resultOrPartial(Inline.LOGGER::error).orElseThrow();
//            return provider.getTooltipData(content);
            return null;
        } catch(Exception e) {
            return null;
        }
    }

    private static final Map<Identifier, CustomTooltipProvider> PROVIDERS = new HashMap<>();

    /**
     * Registers a provider.
     * @param <T> type that the provider handles
     * @param provider
     * @return the same provider.
     */
    public static <T> CustomTooltipProvider<T> registerProvider(CustomTooltipProvider<T> provider){
        PROVIDERS.put(provider.getId(), provider);
        return provider;
    }

    /**
     * Makes a tooltip out of some arbitrary data of type T.
     * <p>
     * Delegating between providers is handled by the manager.
     */
    public static interface CustomTooltipProvider<T>{

        public Identifier getId();

        @NotNull
        public List<Text> getTooltipText(T content);

        @NotNull
        public Optional<TooltipData> getTooltipData(T content);

        @NotNull
        public Codec<T> getCodec();
    }
}
