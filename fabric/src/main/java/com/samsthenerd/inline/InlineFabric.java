package com.samsthenerd.inline;

import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.matching.MatchContext;
import com.samsthenerd.inline.impl.InlineImpl;
import com.samsthenerd.inline.xplat.FabricAbstractions;
import com.samsthenerd.inline.xplat.FabricModMeta;
import com.samsthenerd.inline.xplat.XPlatInstances;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;

public class InlineFabric implements ModInitializer {
    @Override
	public void onInitialize() {
        XPlatInstances fabricXPlats = new XPlatInstances(
            FabricModMeta::getMod,
            new FabricAbstractions()
        );
        Inline.onInitialize(fabricXPlats);

        ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
            MatchContext.ChatMatchContext ctx = MatchContext.ChatMatchContext.of(sender, message);
            for(InlineMatcher matcher : InlineImpl.SERVER_CHAT_MATCHERS){
                matcher.match(ctx);
            }
            return ctx.getFinalStyledText();
        });
    }
}
