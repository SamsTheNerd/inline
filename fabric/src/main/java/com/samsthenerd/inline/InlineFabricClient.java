package com.samsthenerd.inline;

import com.samsthenerd.inline.registry.InlineTooltips;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

public class InlineFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        InlineClient.initClient();

        InlineTooltips.init();
        TooltipComponentCallback.EVENT.register(InlineTooltips::getTooltipComponent);
    }
}
