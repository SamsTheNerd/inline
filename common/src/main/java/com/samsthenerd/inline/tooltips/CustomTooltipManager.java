package com.samsthenerd.inline.tooltips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private static final String TOOLTIP_DATA_KEY = "inlinecustomtooltip";

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
        var ctpd = new CTPData<T>(provider, content);

        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, tagComp ->
            tagComp.with(NbtOps.INSTANCE, CTPData.CODEC, Optional.of(ctpd)).getOrThrow());
        return stack;
    }

    @Nullable
    public static CustomTooltipProvider<?> getProvider(ItemStack stack){
        var tagComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(tagComp == null) return null;
        try {
            return tagComp.get(CTPData.CODEC).getOrThrow().map(CTPData::provider).orElse(null);
        } catch (Exception e){
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static List<Text> getTooltipText(ItemStack stack){
        var tagComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(tagComp == null) return null;
        try {
            return tagComp.get(CTPData.CODEC).getOrThrow()
                .map(ctpd -> ctpd.provider().getTooltipText(ctpd.data))
                .orElse(null);
        } catch (Exception e){
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> Optional<TooltipData> getTooltipData(ItemStack stack){
        var tagComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(tagComp == null) return Optional.empty();
        try {
            return tagComp.get(CTPData.CODEC).getOrThrow()
                .flatMap(ctpd -> ctpd.provider().getTooltipData(ctpd.data));
        } catch (Exception e){
            return Optional.empty();
        }
    }

    private static final Map<Identifier, CustomTooltipProvider> PROVIDERS = new HashMap<>();

    private static final Codec<CustomTooltipProvider> PROVIDERS_CODEC = Identifier.CODEC.comapFlatMap(
        DataResult.partialGet(PROVIDERS::get, () -> "Provider not found"),
        CustomTooltipProvider::getId
    );

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

    // helper class to make data storage a tad easier
    public record CTPData<T>(CustomTooltipProvider<T> provider, T data){
        @SuppressWarnings("unchecked")
        public static final MapCodec<Optional<CTPData>> CODEC = PROVIDERS_CODEC.dispatch("inline_ctp_id",
            (CTPData ctpd) -> ctpd.provider(),
            CTPData::makeCTPDCodec
        ).optionalFieldOf(TOOLTIP_DATA_KEY);

        public static <T> MapCodec<CTPData<T>> makeCTPDCodec(CustomTooltipProvider<T> provider){
            return provider.getCodec().fieldOf("inline_ctp_data").xmap(
                d -> new CTPData<T>(provider, d),
                CTPData::data
            );
        }
    }
}
