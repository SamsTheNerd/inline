package com.samsthenerd.inline.mixin.feature.playerskins;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinOfflinePlayerSkins extends PlayerEntity{

    public MixinOfflinePlayerSkins(){
        super(null, null, 0, null);
    }

    // TODO: FIX
//
//    @Shadow
//    abstract PlayerListEntry getPlayerListEntry();
//
//    @ModifyReturnValue(
//        method="getSkinTexture()Lnet/minecraft/util/Identifier;",
//        at = @At("RETURN")
//    )
//    private Identifier betterGetSkinTexture(Identifier originalTexture){
//        if(getPlayerListEntry() == null){
//            return MinecraftClient.getInstance().getSkinProvider().loadSkin(getGameProfile());
//        }
//        return originalTexture;
//    }
//
//    // i guess ?
//    @ModifyReturnValue(
//        method="hasSkinTexture()Z",
//        at = @At("RETURN")
//    )
//    private boolean betterCheckSkinTexture(boolean originalCheck){
//        return true;
//    }
}
