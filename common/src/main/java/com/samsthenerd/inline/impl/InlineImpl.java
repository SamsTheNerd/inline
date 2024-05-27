package com.samsthenerd.inline.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineMatcher;

import net.minecraft.util.Identifier;

public class InlineImpl extends InlineAPI{

    private final Map<Identifier, InlineMatcher> MATCHERS = new HashMap<>();
    
    @Override
    public void addMatcher(Identifier id, InlineMatcher matcher){
        MATCHERS.put(id, matcher);
    }

    @Override
    public InlineMatcher getMatcher(Identifier id){
        return MATCHERS.get(id);
    }

    @Override
    public Set<InlineMatcher> getAllMatchers(){
        return new HashSet<>(MATCHERS.values());
    }
}
