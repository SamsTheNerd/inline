package com.samsthenerd.inline;

import com.samsthenerd.inline.api.InlineClientAPI;
import com.samsthenerd.inline.api.renderers.InlineEntityRenderer;
import com.samsthenerd.inline.api.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.renderers.PlayerHeadRenderer;
import com.samsthenerd.inline.api.renderers.SpriteInlineRenderer;

public class InlineClient {
    public static void initClient(){
        addDefaultRenderers();
    }

    private static void addDefaultRenderers(){
        InlineClientAPI.INSTANCE.addRenderer(InlineItemRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlineEntityRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(SpriteInlineRenderer.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(PlayerHeadRenderer.INSTANCE);
    }
}
