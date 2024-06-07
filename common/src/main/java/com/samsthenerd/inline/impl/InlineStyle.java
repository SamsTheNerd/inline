package com.samsthenerd.inline.impl;

import com.samsthenerd.inline.api.InlineData;

import net.minecraft.text.Style;

// duck interface to carry added style data
public interface InlineStyle {
    public InlineData getInlineData();

    public Style withInlineData(InlineData data);

    public static Style fromInlineData(InlineData data){
        if(((Object)Style.EMPTY) instanceof InlineStyle){
            return ((InlineStyle)Style.EMPTY).withInlineData(data);
        } else {
            return Style.EMPTY;
        }
    }

    public Style setData(InlineData data);

    public Style withGlowyMarker(boolean glowy);

    public Style setGlowyMarker(boolean glowy);

    public boolean hasGlowyMarker();

    public Style setHidden(boolean hidden);

    public Style withHidden(boolean hidden);

    public boolean isHidden();
}
