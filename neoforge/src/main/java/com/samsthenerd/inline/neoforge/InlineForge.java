package com.samsthenerd.inline.neoforge;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.InlineClient;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.matching.MatchContext;
import com.samsthenerd.inline.config.InlineConfigHandler;
import com.samsthenerd.inline.impl.InlineImpl;
import com.samsthenerd.inline.neoforge.xplat.ForgeAbstractions;
import com.samsthenerd.inline.neoforge.xplat.ForgeModMeta;
import com.samsthenerd.inline.xplat.XPlatInstances;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;


@Mod("inline")
public class InlineForge {
    public InlineForge(){
        // so that we can register properly with architectury
        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        IEventBus modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        modBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::onServerChatDecoration);

        // note, technically double nested lambdas, so should be fine ?
//        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
//            () -> IConfigScreenFactory.((client, parent) -> InlineConfigHandler.getConfigScreen(parent)));
//
//
//        .unsafeRunWhenOn(Dist.CLIENT, () -> () -> modBus.register(InlineForgeClient.class));

        XPlatInstances forgeXPlats = new XPlatInstances(
            ForgeModMeta::getMod,
            new ForgeAbstractions()
        );

        Inline.onInitialize(forgeXPlats);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            InlineClient.initClient();
            ModLoadingContext.get().getActiveContainer().registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> InlineConfigHandler.getConfigScreen(parent));
        });
    }

    private void onServerChatDecoration(ServerChatEvent event){
        MatchContext.ChatMatchContext ctx = MatchContext.ChatMatchContext.of(event.getPlayer(), event.getMessage());
        for(InlineMatcher matcher : InlineImpl.SERVER_CHAT_MATCHERS){
            matcher.match(ctx);
        }
        event.setMessage(ctx.getFinalStyledText());
    }
}
