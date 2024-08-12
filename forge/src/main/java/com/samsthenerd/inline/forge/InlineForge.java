package com.samsthenerd.inline.forge;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.InlineClient;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.matching.MatchContext;
import com.samsthenerd.inline.config.InlineConfigHandler;
import com.samsthenerd.inline.forge.xplat.ForgeAbstractions;
import com.samsthenerd.inline.forge.xplat.ForgeModMeta;
import com.samsthenerd.inline.impl.InlineImpl;
import com.samsthenerd.inline.xplat.XPlatInstances;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.concurrent.CompletableFuture;

@Mod("inline")
public class InlineForge {
    public InlineForge(){
        // so that we can register properly with architectury
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onServerChatDecoration);
        
        // note, technically double nested lambdas, so should be fine ?
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, 
            () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> InlineConfigHandler.getConfigScreen(parent)));


        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modBus.register(InlineForgeClient.class));

        XPlatInstances forgeXPlats = new XPlatInstances(
            ForgeModMeta::getMod,
            new ForgeAbstractions()
        );

        Inline.onInitialize(forgeXPlats);
    }

    private void onClientSetup(FMLClientSetupEvent event) { 
        event.enqueueWork(() -> {
            InlineClient.initClient();
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
