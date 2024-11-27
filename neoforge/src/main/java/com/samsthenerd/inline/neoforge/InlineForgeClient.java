package com.samsthenerd.inline.neoforge;

import java.util.Map.Entry;
import java.util.function.Function;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.registry.InlineTooltips;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InlineForgeClient {
    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent evt) {
        Inline.logPrint("registering tooltip components");
        // evt.register(MirrorTooltipData.class, MirrorTooltipComponent::new);
        InlineTooltips.init();
        for(Entry<Class<? extends TooltipData>, Function<TooltipData, TooltipComponent>> entry : InlineTooltips.tooltipDataToComponent.entrySet()){
            evt.register(entry.getKey(), entry.getValue());
        }
    }
}

