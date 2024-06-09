package com.samsthenerd.inline.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.samsthenerd.inline.api.InlineClientAPI;
import com.samsthenerd.inline.api.data.ModIconData;
import com.samsthenerd.inline.xplat.IModMeta;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

@Mixin(value = ItemStack.class, priority = 10000)
public class MixinAddModIconToTooltip {
    @ModifyReturnValue(
        method="getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;",
        at = @At("RETURN")
    )
    private List<Text> findAndAddModIcon(List<Text> originalTT){
        if(!InlineClientAPI.INSTANCE.getConfig().shouldRenderModIcons()) return originalTT;
        ItemStack thisStack = (ItemStack)((Object)this);
        // probably modid !
        String modNamespace = Registries.ITEM.getId(thisStack.getItem()).getNamespace();
        if(modNamespace.equals("") || modNamespace.equals("minecraft")){
            return originalTT;
        }
        Optional<IModMeta> maybeMod = IModMeta.getMod(modNamespace);
        if(maybeMod.isEmpty()){
            return originalTT;
        }
        IModMeta mod = maybeMod.get();
        String modName = mod.getName();
        List<Text> newTT = new ArrayList<>();
        for(Text comp : originalTT){
            if(comp.getString().equals(modName)){
                newTT.add(comp.copy().append(Text.literal(" ")).append(ModIconData.makeModIcon(mod)));
            } else {
                newTT.add(comp);
            }
        }
        return newTT;
    }
}
