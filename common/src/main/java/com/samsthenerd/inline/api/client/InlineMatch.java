package com.samsthenerd.inline.api.client;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.impl.InlineStyle;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public interface InlineMatch {
    // returns the number of characters consumed
    public int accept(CharacterVisitor visitor, int index, Style currentStyle);

    public int charLength();

    public static class DataMatch implements InlineMatch{

        private InlineData data;
        private Style style;

        public DataMatch(InlineData data, Style style){
            this.data = data;
            this.style = style;
        }

        public DataMatch(InlineData data){
            this(data, Style.EMPTY);
        }

        public int accept(CharacterVisitor visitor, int index, Style currentStyle){
            Style nonDataStyle = style.withParent(currentStyle);
            visitor.accept(index, ((InlineStyle)nonDataStyle).withInlineData(data), '.');
            return 1;
        }

        public int charLength(){
            return 1;
        }
    }

    public static class TextMatch implements InlineMatch{

        private Text text;

        public TextMatch(Text text){
            this.text = text;
        }

        public int accept(CharacterVisitor visitor, int index, Style currentStyle){
            AtomicInteger offset = new AtomicInteger(0);
            text.visit((Style style, String str)->{
                for(char c : str.toCharArray()){
                    visitor.accept(index + offset.get(), style.withParent(currentStyle), c);
                    offset.incrementAndGet();
                }
                return Optional.empty();
            }, currentStyle);
            return offset.get();
        }

        public int charLength(){
            return text.getString().length();
        }
    }
}
