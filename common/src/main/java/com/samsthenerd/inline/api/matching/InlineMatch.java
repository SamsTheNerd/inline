package com.samsthenerd.inline.api.matching;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.samsthenerd.inline.api.InlineData;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

/**
 * Represents some match from a matcher. 
 * <p>
 * A match is attached to some input text by a matcher and is expected to 
 * provide styled characters to the CharacterVisitor in place of the text segment 
 * it's attached to.
 * <p>
 * {@link DataMatch} will work for most use cases.
 */
public interface InlineMatch {
    /**
     * Supplies styled characters
     * @param visitor accepts the styled characters
     * @param index the number of characters given to this visitor so far
     * @param currentStyle the style that this text would otherwise have.
     * @return the number of characters supplied to the visitor.
     */
    public int accept(CharacterVisitor visitor, int index, Style currentStyle);

    /**
     * Gets the length of this match
     * @return the number of characters we plan to give the visitor.
     */
    public int charLength();

    /**
     * A match representing some data.
     */
    public static class DataMatch implements InlineMatch{

        public final InlineData data;
        public final Style style;

        /**
         * Makes a DataMatch with the given data and style
         * @param data
         * @param style
         */
        public DataMatch(InlineData data, Style style){
            this.data = data;
            this.style = style;
        }

        /**
         * Makes a DataMatch with the given data
         * @param data
         */
        public DataMatch(InlineData data){
            this(data, Style.EMPTY);
        }

        public int accept(CharacterVisitor visitor, int index, Style currentStyle){
            Style nonDataStyle = style.withParent(currentStyle);
            Style dataStyle = nonDataStyle.withInlineData(data);
            visitor.accept(index, dataStyle, '.');
            return 1;
        }

        public int charLength(){
            return 1;
        }
    }

    /**
     * A match representing some text.
     */
    public static class TextMatch implements InlineMatch{

        private Text text;

        /**
         * Makes a TextMatch with the given text
         * @param text
         */
        public TextMatch(Text text){
            this.text = text;
        }

        public int accept(CharacterVisitor visitor, int index, Style currentStyle){
            if(text.getString().equals("")) return 1;
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
            return Math.max(text.getString().length(), 1);
        }
    }
}
