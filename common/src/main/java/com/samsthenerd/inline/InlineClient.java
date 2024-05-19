package com.samsthenerd.inline;

import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.api.renderers.SpriteInlineRenderer;

public class InlineClient {
    public static void initClient(){
        addDefaultRenderers();
    }

    private static void addDefaultRenderers(){
        InlineAPI.INSTANCE.addRenderer(InlineItemRenderer.INSTANCE);
        InlineAPI.INSTANCE.addRenderer(InlineEntityRenderer.INSTANCE);
        InlineAPI.INSTANCE.addRenderer(SpriteInlineRenderer.INSTANCE);
        InlineAPI.INSTANCE.addRenderer(PlayerHeadRenderer.INSTANCE);
    }
}
