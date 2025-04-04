package com.samsthenerd.inline.tooltips.data;

import com.samsthenerd.inline.utils.EntityCradle;
import net.minecraft.item.tooltip.TooltipData;

import java.util.function.BiFunction;

public class EntityDisplayTTData implements TooltipData {
    public final EntityCradle cradle;
    // takes in the  width and height of the given texture and returns the width to render it at
    public BiFunction<Integer, Integer, Integer> widthProvider = (w, h) -> 128;

    public EntityDisplayTTData(EntityCradle cradle){
        this.cradle = cradle;
    }

    public EntityDisplayTTData(EntityCradle cradle, BiFunction<Integer, Integer, Integer> widthProvider){
        this.cradle = cradle;
        this.widthProvider = widthProvider;
    }
}
