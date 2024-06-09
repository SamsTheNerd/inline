package com.samsthenerd.inline.impl;

import com.samsthenerd.inline.api.InlineData;

import net.minecraft.text.Style;

// duck interface to carry added style data
public interface InlineStyle {
    default InlineData getInlineData(){return null;}

    default Style withInlineData(InlineData data){return null;}

    static Style fromInlineData(InlineData data){
        if(((Object)Style.EMPTY) instanceof InlineStyle){
            return ((InlineStyle)Style.EMPTY).withInlineData(data);
        } else {
            return Style.EMPTY;
        }
    }

    default Style setData(InlineData data){return null;}

    default Style withGlowyMarker(boolean glowy){return null;}

    default Style setGlowyMarker(boolean glowy){return null;}

    default boolean hasGlowyMarker(){ return false;}

    default Style setHidden(boolean hidden){return null;}

    default Style withHidden(boolean hidden){return null;}

    default boolean isHidden(){return false;}
}
