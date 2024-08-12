package com.samsthenerd.inline.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.InlineClientConfig;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineErrorRenderer;
import com.samsthenerd.inline.config.InlineClientConfigImpl;

import net.minecraft.util.Identifier;

public class InlineClientImpl implements InlineClientAPI{
    private final Map<Identifier, InlineRenderer<?>> RENDERERS = new HashMap<>();
    private final Map<Identifier, InlineMatcher> MATCHERS = new HashMap<>();

    private final Set<Identifier> WARNED_RENDERERS = new HashSet<>();

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
            if(!WARNED_RENDERERS.contains(id)){
                Inline.LOGGER.error("couldn't find renderer: " + id.toString());
                Inline.LOGGER.error("available renderers: ");
                for(Identifier i : RENDERERS.keySet()){
                    Inline.LOGGER.error("\t-" + i.toString());
                }
                WARNED_RENDERERS.add(id);
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
