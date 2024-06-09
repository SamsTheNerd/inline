package com.samsthenerd.inline.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineClientAPI;
import com.samsthenerd.inline.api.InlineClientConfig;
import com.samsthenerd.inline.api.InlineMatcher;
import com.samsthenerd.inline.api.InlineRenderer;
import com.samsthenerd.inline.api.renderers.InlineErrorRenderer;
import com.samsthenerd.inline.config.InlineClientConfigImpl;

import net.minecraft.util.Identifier;

public class InlineClientImpl implements InlineClientAPI{
    private final Map<Identifier, InlineRenderer<?>> RENDERERS = new HashMap<>();
    private final Map<Identifier, InlineMatcher> MATCHERS = new HashMap<>();

    @Override
    public void addRenderer(InlineRenderer<?> renderer){
        if(RENDERERS.containsKey(renderer.getId())){
            Inline.LOGGER.error("renderer with id " + renderer.getId().toString() + " already exists");
            return;
        }
        RENDERERS.put(renderer.getId(), renderer);
    }

    @Override
    public InlineRenderer<?> getRenderer(Identifier id){
        if(RENDERERS.get(id) == null) {
            Inline.logPrint("couldn't find renderer: " + id.toString());
            Inline.logPrint("available renderers: ");
            for(Identifier i : RENDERERS.keySet()){
                Inline.logPrint("\t-" + i.toString());
            }
            return InlineErrorRenderer.INSTANCE;
        }
        return RENDERERS.get(id);
    }

    @Override
    public Set<InlineRenderer<?>> getAllRenderers(){
        return new HashSet<>(RENDERERS.values());
    }
    
    @Override
    public void addMatcher(InlineMatcher matcher){
        MATCHERS.put(matcher.getId(), matcher);
    }

    @Override
    public InlineMatcher getMatcher(Identifier id){
        return MATCHERS.get(id);
    }

    @Override
    public Set<InlineMatcher> getAllMatchers(){
        return new HashSet<>(MATCHERS.values());
    }

    public InlineClientConfig getConfig(){
        return InlineClientConfigImpl.getInstance();
    }
}
