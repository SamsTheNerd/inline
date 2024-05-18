package com.samsthenerd.inline.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.samsthenerd.inline.impl.InlineStyle;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public class InlineMatchResult {
    
    // Horrible generic, but whatever, consider it an implementation detail
    private List<Pair<Pair<Integer, Integer>, Match>> matches = new ArrayList<>();

    public InlineMatchResult addMatch(int start, int end, InlineData data){
        return addMatch(start, end, data, Style.EMPTY);
    }
    
    public InlineMatchResult addMatch(int start, int end, Text text){
        return addMatch(start, end, new TextMatch(text));
    }

    public InlineMatchResult addMatch(int start, int end, InlineData data, Style style){
        return addMatch(start, end, new DataMatch(data, style));
    }

    public InlineMatchResult addMatch(int start, int end, Match match){
        // TODO: add conflict detection here ?
        matches.add(new Pair<>(new Pair<>(start, end), match));
        return this;
    }

    public List<Pair<Pair<Integer, Integer>, Match>> getMatches(){
        return new ArrayList<>(matches);
    }

    public static interface Match {
        // TODO: let this modify the current style
        // returns the number of characters consumed
        public int accept(CharacterVisitor visitor, int index, Style currentStyle);

        public int charLength();
    }

    public static class DataMatch implements Match{

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

    public static class TextMatch implements Match{

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
