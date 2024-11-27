package com.samsthenerd.inline.api.data;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.impl.InlineStyle;

import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.ItemStackContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemInlineData implements InlineData<ItemInlineData>{

    private ItemStack stack;

    @Override
    public ItemDataType getType(){
        return ItemDataType.INSTANCE;
    }

    @Override
    public Identifier getRendererId(){
        return Inline.id( "item");
    }

    public ItemStack getStack(){
        return stack;
    }

    public ItemInlineData(ItemStack stack){
        this.stack = stack;
    }

    // gives a character that's styled to appear as the item, with the same hover event
    public static Text make(ItemStack stack){
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ItemStackContent(stack));
        Style style = ((InlineStyle)Style.EMPTY.withHoverEvent(hover)).withInlineData(new ItemInlineData(stack));
        return Text.literal("#").setStyle(style);
    }

    public static class ItemDataType implements InlineDataType<ItemInlineData> {
        public static ItemDataType INSTANCE = new ItemDataType();

        @Override
        public Identifier getId(){
            return Inline.id( "item");
        }

        @Override
        public Codec<ItemInlineData> getCodec(){
            return ItemStack.CODEC.xmap(
                ItemInlineData::new,
                ItemInlineData::getStack
            );
        }
    }
}
