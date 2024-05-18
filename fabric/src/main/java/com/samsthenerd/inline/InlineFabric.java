package com.samsthenerd.inline;

import net.fabricmc.api.ModInitializer;

public class InlineFabric implements ModInitializer {
    @Override
	public void onInitialize() {
        Inline.onInitialize();
    }
}
