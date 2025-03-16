package com.samsthenerd.inline.mixin.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samsthenerd.inline.impl.InlineStyle;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Function;

@Mixin(Style.Codecs.class)
public class MixinInlineStyleSerializer {
    @Shadow @Final @Mutable public static MapCodec<Style> MAP_CODEC;
    @Shadow @Final @Mutable public static Codec<Style> CODEC;

    @Unique
    private static MapCodec<Style> INLINE_CODEC;

    @Inject(method = "<clinit>", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/serialization/MapCodec;codec()Lcom/mojang/serialization/Codec;", ordinal = 0, shift = At.Shift.BEFORE))
    private static void extendCodec(CallbackInfo ci) {
        INLINE_CODEC = RecordCodecBuilder.mapCodec(instance ->
          instance.group(
            // Use the last MAP_CODEC, keeping any modified one from other mods
            RecordCodecBuilder.of(Function.identity(), MAP_CODEC),
            // Add extra stuff to parse, with it being optional
            InlineStyle.ISComponent.COMPONENT_TO_VALUE_MAP_CODEC.optionalFieldOf("inline_components", Map.of()).forGetter(InlineStyle::getComponentMap)
          ).apply(instance, (original, components) -> {
              components.forEach((component, value) -> {
                  original.setComponent((InlineStyle.ISComponent<? super Object>) component, value);
              });
              return original;
          })
        );

        MAP_CODEC = INLINE_CODEC;
        CODEC = MAP_CODEC.codec();
    }
}
