package com.samsthenerd.inline.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.api.InlineData;
import net.minecraft.text.Style;
import net.minecraft.text.Style.Codecs;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * duck interface to carry added style data
 */
public interface InlineStyle {
    default InlineData getInlineData(){return getComponent(INLINE_DATA_COMP); }

    default Style withInlineData(InlineData data){return null;}

    static Style fromInlineData(InlineData data){
        return (Style.EMPTY).withInlineData(data);
    }

    default <C> C getComponent(ISComponent<C> component){return null;}
    default Map<ISComponent<?>, Object> getComponentMap(){ return null; }
    default Set<ISComponent<?>> getComponents(){ return null; }
    default <C> Style withComponent(ISComponent<C> component, @Nullable C value){ return null; }
    default <C> Style setComponent(ISComponent<C> component, @Nullable C value){ return null; }

    // ensure that C has a valid .equals() in order for the styles to have it as well.
    record ISComponent<C>(String id, Codec<C> codec, C defaultValue, BiFunction<C, C, C> merger){
        public static final Map<String, ISComponent<?>> ALL_COMPS = new HashMap<>();

        public static final Codec<ISComponent<?>> CODEC = Codec.STRING
          .comapFlatMap(
            id -> Optional.ofNullable(ALL_COMPS.get(id))
              .<DataResult<ISComponent<?>>>map(DataResult::success)
              .orElseGet(() -> DataResult.error(() -> "Unknown Inline style component: " + id)),
            ISComponent::id
          );

        public static final Codec<Map<ISComponent<?>, Object>> COMPONENT_TO_VALUE_MAP_CODEC
            = Codec.dispatchedMap(InlineStyle.ISComponent.CODEC, InlineStyle.ISComponent::codec);

        public ISComponent(String id, Codec<C> codec, C defaultValue, BiFunction<C, C, C> merger){
            this.id = id; this.codec = codec; this.defaultValue = defaultValue; this.merger = merger;
            ALL_COMPS.put(id, this);
        }

        public ISComponent(String id, Codec<C> codec, C defaultValue){
            this(id, codec, defaultValue, (a, b) -> a);
        }
    }

    ISComponent<InlineData<?>> INLINE_DATA_COMP = new ISComponent<>("inlinedata", InlineImpl.INLINE_DATA_CODEC, null);
    ISComponent<Boolean> HIDDEN_COMP = new ISComponent<>("hidden", Codec.BOOL, false);
    ISComponent<Double> SIZE_MODIFIER_COMP = new ISComponent<>("size", Codec.DOUBLE, 1.0);

    /**
     * GLOWY_MARKER_COMP indicates if the *currently rendered* text is an outline. ie, the outline is currently being
     * rendered and it has this style.
     */
    ISComponent<Boolean> GLOWY_MARKER_COMP = new ISComponent<>("glowy", Codec.BOOL, false);
    /**
     * GLOWY_PARENT_COMP indicates that this text *has* outlines, but that the currently rendered text is the center,
     * not the outline. It stores the color of the outline. This is really only used for if you want to self-handle the
     * glow outline.
     */
    ISComponent<Integer> GLOWY_PARENT_COMP = new ISComponent<>("glowyparent", Codec.INT, -1);

    static Style makeCopy(Style original){
        // do it twice to get around the no change check.
        return original.withStrikethrough(!original.isStrikethrough()).withStrikethrough(original.isStrikethrough());
    }

    // utility to test that codecs are working
    static Style testStyleSer(Style sty){
//        Inline.logPrint("Before: ");
//        for(var entry : sty.getComponentMap().entrySet()){
//            Inline.logPrint("\t" + entry.getKey() + " : " + entry.getValue());
//        }
//        var compsEncoded = ISComponent.COMPONENT_TO_VALUE_MAP_CODEC.encodeStart(JsonOps.INSTANCE, sty.getComponentMap()).getOrThrow();
//        Inline.logPrint(compsEncoded.toString());
        var encoded = Codecs.CODEC.encodeStart(JsonOps.INSTANCE, sty).getOrThrow();
//        Inline.logPrint(encoded.toString());
        Style newerStyle = Codecs.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
//        Inline.logPrint("After: ");
//        for(var entry : newerStyle.getComponentMap().entrySet()){
//            Inline.logPrint("\t" + entry.getKey() + " : " + entry.getValue());
//        }
        return newerStyle;
    }
}
