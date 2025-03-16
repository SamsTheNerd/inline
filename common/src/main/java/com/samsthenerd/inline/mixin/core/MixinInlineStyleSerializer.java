package com.samsthenerd.inline.mixin.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samsthenerd.inline.impl.InlineStyle;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.function.Function;

@Mixin(Style.Codecs.class)
public class MixinInlineStyleSerializer {

    @ModifyExpressionValue(
        method = "<clinit>",
        at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"))
    private static MapCodec<Style> attachInlineDataToStyleCodec(MapCodec<Style> originalMapCodec){
        return RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                // Use the last MAP_CODEC, keeping any modified one from other mods
                RecordCodecBuilder.of(Function.identity(), originalMapCodec),
                // Add extra stuff to parse, with it being optional
                InlineStyle.ISComponent.COMPONENT_TO_VALUE_MAP_CODEC
                    .optionalFieldOf("inline_components", Map.of()).forGetter(InlineStyle::getComponentMap)
            ).apply(instance, (original, components) -> {
                components.forEach((component, value) -> {
                    original.setComponent((InlineStyle.ISComponent<? super Object>) component, value);
                });
                return original;
            })
        );
    }
}
