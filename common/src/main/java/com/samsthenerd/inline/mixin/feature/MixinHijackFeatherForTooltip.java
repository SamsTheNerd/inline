package com.samsthenerd.inline.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.samsthenerd.inline.tooltips.CustomTooltipManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// slightly lower priority in case other mods try to modify it.
@Mixin(value = ItemStack.class, priority = 5000)
public class MixinHijackFeatherForTooltip {
    @ModifyReturnValue(
        method="getTooltip(Lnet/minecraft/item/Item$TooltipContext;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/tooltip/TooltipType;)Ljava/util/List;",
        at = @At("RETURN")
    )
    private List<Text> hijackFeatherTooltipText(List<Text> originalTT){
        return getTooltipSomething(CustomTooltipManager::getTooltipText, originalTT);
    }

    @ModifyReturnValue(
        method="getTooltipData()Ljava/util/Optional;",
        at = @At("RETURN")
    )
    private Optional<TooltipData> hijackFeatherTooltipData(Optional<TooltipData> originalTT){
        return getTooltipSomething(CustomTooltipManager::getTooltipData, originalTT);
    }

    private <T> T getTooltipSomething(Function<ItemStack, T> ttGetter, T originalTT){
        ItemStack thisStack = (ItemStack)((Object)this);
        if(thisStack.getItem() == CustomTooltipManager.HIJACKED_ITEM){
            T newTT = ttGetter.apply(thisStack);
            if(newTT != null){
                return newTT;
            }
        }
        return originalTT;
    }
}
