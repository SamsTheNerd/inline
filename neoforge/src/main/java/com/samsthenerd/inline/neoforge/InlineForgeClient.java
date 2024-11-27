package com.samsthenerd.inline.neoforge;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.registry.InlineTooltips;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import java.util.Map.Entry;
import java.util.function.Function;

@EventBusSubscriber(modid = "inline", value = Dist.CLIENT, bus= Bus.MOD)
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

