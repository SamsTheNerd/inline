package com.samsthenerd.inline.mixin.feature.playerskins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinOfflinePlayerSkins extends PlayerEntity{

    public MixinOfflinePlayerSkins(){
        super(null, null, 0, null);
    }

    @Shadow
    abstract PlayerListEntry getPlayerListEntry();

    @ModifyReturnValue(
        method="getSkinTexture()Lnet/minecraft/util/Identifier;",
        at = @At("RETURN")
    )
    private Identifier betterGetSkinTexture(Identifier originalTexture){
        if(getPlayerListEntry() == null){
            return MinecraftClient.getInstance().getSkinProvider().loadSkin(getGameProfile());
        }
        return originalTexture;
    }

    // i guess ?
    @ModifyReturnValue(
        method="hasSkinTexture()Z",
        at = @At("RETURN")
    )
    private boolean betterCheckSkinTexture(boolean originalCheck){
        return true;
    }
}
