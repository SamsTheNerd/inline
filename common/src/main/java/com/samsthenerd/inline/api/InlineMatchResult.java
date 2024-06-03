package com.samsthenerd.inline.api;

import java.util.ArrayList;
import java.util.List;

import com.samsthenerd.inline.api.InlineMatch.DataMatch;
import com.samsthenerd.inline.api.InlineMatch.TextMatch;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

public class InlineMatchResult {
    
    // Horrible generic, but whatever, consider it an implementation detail
    private List<Pair<Pair<Integer, Integer>, InlineMatch>> matches = new ArrayList<>();

    public InlineMatchResult addMatch(int start, int end, InlineData data){
        return addMatch(start, end, data, Style.EMPTY);
    }
    
    public InlineMatchResult addMatch(int start, int end, Text text){
        return addMatch(start, end, new TextMatch(text));
    }

    public InlineMatchResult addMatch(int start, int end, InlineData data, Style style){
        return addMatch(start, end, new DataMatch(data, style));
    }

    public InlineMatchResult addMatch(int start, int end, InlineMatch match){
        // TODO: add conflict detection here ?
        matches.add(new Pair<>(new Pair<>(start, end), match));
        return this;
    }

    public List<Pair<Pair<Integer, Integer>, InlineMatch>> getMatches(){
        return new ArrayList<>(matches);
    }

    

    
}
