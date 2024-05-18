package com.samsthenerd.inline.tooltips.data;

import java.util.function.BiFunction;

import com.samsthenerd.inline.utils.Spritelike;

import net.minecraft.client.item.TooltipData;

// maxWidth/maxHeight are for how big it should render
public class SpriteTooltipData implements TooltipData{
    public final Spritelike sprite;
    // takes in the  width and height of the given texture and returns the width to render it at
    public BiFunction<Integer, Integer, Integer> widthProvider = (w, h) -> 128;

    public SpriteTooltipData(Spritelike sprite){
        this.sprite = sprite;
    }

    public SpriteTooltipData(Spritelike sprite, BiFunction<Integer, Integer, Integer> widthProvider){
        this.sprite = sprite;
        this.widthProvider = widthProvider;
    }
}
