package com.samsthenerd.inline.registry;

import com.samsthenerd.inline.tooltips.CustomTooltipManager;
import com.samsthenerd.inline.tooltips.components.EntityDisplayTTComp;
import com.samsthenerd.inline.tooltips.components.SpriteTooltipComponent;
import com.samsthenerd.inline.tooltips.data.EntityDisplayTTData;
import com.samsthenerd.inline.tooltips.data.SpriteTooltipData;
import com.samsthenerd.inline.tooltips.providers.EntityTTProvider;
import com.samsthenerd.inline.tooltips.providers.ModDataTTProvider;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class InlineTooltips {

    public static final Map<Class<? extends TooltipData>, Function<TooltipData, TooltipComponent>> tooltipDataToComponent = new HashMap<>();

    public static TooltipComponent getTooltipComponent(TooltipData data){
        Function<TooltipData, TooltipComponent> ttFunc = tooltipDataToComponent.get(data.getClass());
        return ttFunc == null ? null : ttFunc.apply(data);
    }

    public static <C extends TooltipComponent, D extends TooltipData> Function<TooltipData, C> convertTooltip(Class<D> dataClass, 
        Function<D, C> componentFactory){
        return (data) -> {
            if(dataClass.isInstance(data)){
                return componentFactory.apply(dataClass.cast(data));
            }
            return null;
        };
    }

    // should be called sided in tooltip registration stuff
    public static void init(){
        tooltipDataToComponent.put(SpriteTooltipData.class, convertTooltip(SpriteTooltipData.class, SpriteTooltipComponent::new));
        tooltipDataToComponent.put(EntityDisplayTTData.class, convertTooltip(EntityDisplayTTData.class, EntityDisplayTTComp::new));
        CustomTooltipManager.registerProvider(ModDataTTProvider.INSTANCE);
        CustomTooltipManager.registerProvider(EntityTTProvider.INSTANCE);
    }
}
