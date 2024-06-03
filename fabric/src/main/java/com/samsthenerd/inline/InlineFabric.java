package com.samsthenerd.inline;

import com.samsthenerd.inline.xplat.FabricModMeta;
import com.samsthenerd.inline.xplat.XPlatInstances;

import net.fabricmc.api.ModInitializer;

public class InlineFabric implements ModInitializer {
    @Override
	public void onInitialize() {
        XPlatInstances fabricXPlats = new XPlatInstances(
            FabricModMeta::getMod
        );
        Inline.onInitialize(fabricXPlats);
    }
}
