package com.samsthenerd.inline.mixin.core;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.impl.InlineStyle;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(Style.class)
public class MixinInlineStyle implements InlineStyle {

    @Unique
    private final Map<ISComponent<?>, Object> components = new HashMap<>();

    @NotNull
    @Unique
    private static Style inline$makeCopy(Style original){
        return original.withColor(original.getColor());
    }

    @Unique
    private Style getCopy(){
        return inline$makeCopy((Style)(Object)this);
    }

    @Override
    public Style withInlineData(InlineData data){
        return getCopy().setComponent(InlineStyle.INLINE_DATA_COMP, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> C getComponent(ISComponent<C> component){
        return (C)components.getOrDefault(component, component.defaultValue());
    }

    @Override
    public Set<ISComponent<?>> getComponents(){
        return new HashSet<>(components.keySet());
    }

    @Override
    public <C> Style withComponent(ISComponent<C> component, @Nullable C value){
        return getCopy().setComponent(component, value);
    }

    @Override
    public <C> Style setComponent(ISComponent<C> component, @Nullable C value){
        if(value == null){
            this.components.remove(component);
        } else {
            this.components.put(component, value);
        }
        return (Style)(Object)this;
    }

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "withParent(Lnet/minecraft/text/Style;)Lnet/minecraft/text/Style;", at = @At("RETURN"))
	private Style InlineStyWithParent(Style original, Style parent) {
        Style maybeNewStyle = inline$makeCopy(original);
        keepData(maybeNewStyle); // make sure that we've still got everything.
        for(ISComponent comp : parent.getComponents()){
            if(!maybeNewStyle.getComponents().contains(comp)){
                maybeNewStyle.setComponent(comp, parent.getComponent(comp));
            } else {
                maybeNewStyle.setComponent(comp, comp.merger().apply(getComponent(comp), parent.getComponent(comp)));
            }
        }
        return maybeNewStyle;
	}

	@ModifyReturnValue(method = "equals(Ljava/lang/Object;)Z", at = @At("RETURN"))
	private boolean InlineStyEquals(boolean original, Object obj) {
		if (original && this != obj && (obj instanceof MixinInlineStyle style)) {
            Set<ISComponent> allComps = Stream.concat(
                    this.components.keySet().stream(),
                    style.components.keySet().stream()
            ).collect(Collectors.toSet());
            // see if any comps are different
            for(ISComponent<?> comp : allComps){
                if(!Objects.equals(this.getComponent(comp), style.getComponent(comp))){
                    return false;
                }
            }
		}
        return original;
	}

    @Unique
    private static final String COMP_KEY = "inlinecomps";

	@Mixin(Style.Serializer.class)
	public static class MixinInlineStyleSerializer {
		@ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
		private Style InlineStyDeserialize(Style initialStyle, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
			if (!jsonElement.isJsonObject() || initialStyle == null) {
				return initialStyle;
			}
			JsonObject json = jsonElement.getAsJsonObject();
            if (!json.has(COMP_KEY)) {
                return initialStyle;
            }
            Style copiedStyle = inline$makeCopy(initialStyle);
            for(Map.Entry<String, JsonElement> compEntry : json.get(COMP_KEY).getAsJsonObject().entrySet()){
                ISComponent comp = ISComponent.ALL_COMPS.get(compEntry.getKey());
                if(comp == null) continue;
                Optional<?> compVal = comp.codec().parse(JsonOps.INSTANCE, compEntry.getValue()).result();
                compVal.ifPresent(val -> copiedStyle.setComponent(comp, val));
            }
            return copiedStyle;
		}

        @SuppressWarnings("unchecked")
		@ModifyReturnValue(method = "serialize", at = @At("RETURN"))
		private JsonElement HexPatStySerialize(JsonElement jsonElement, Style style, Type type, JsonSerializationContext jsonSerializationContext) {
			if (jsonElement == null || !jsonElement.isJsonObject()) {
				return jsonElement;
			}
			JsonObject json = jsonElement.getAsJsonObject();
            JsonObject compsJson = new JsonObject();
            // save all comps
            for(ISComponent comp : style.getComponents()){
                Optional<JsonElement> dataElem = comp.codec().encodeStart(JsonOps.INSTANCE, style.getComponent(comp)).result();
                dataElem.ifPresent(element -> compsJson.add(comp.id(), element));
            }
            if(compsJson.size() > 0) json.add(COMP_KEY, compsJson);
            return json;
		}
	}

    // a fix from skye to prevent breaking with font mods like caxton.
    @ModifyReturnValue(method = "getFont", at = @At("RETURN"))
    private Identifier overrideFont(Identifier original) {
        if(this.getInlineData() != null){
            return new Identifier("inline", "dummy_font");
        }
        return original;
    }

    @Unique
    private Style keepData(Style newStyle){
        for(ISComponent comp : components.keySet()){
            newStyle.setComponent(comp, getComponent(comp));
        }
        return newStyle;
    }

    @ModifyReturnValue(method = "withColor(Lnet/minecraft/text/TextColor;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
    private Style fixWithColor(Style newStyle, TextColor color){
        return keepData(newStyle);
    }

    @ModifyReturnValue(method = "withBold(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
    private Style fixWithBold(Style newStyle, Boolean boldBool){
        return keepData(newStyle);
    }
    
    @ModifyReturnValue(method = "withItalic(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithItalic(Style newStyle, Boolean boldBool){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withUnderline(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithUnderline(Style newStyle, Boolean boldBool){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withStrikethrough(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithStrikethrough(Style newStyle, Boolean boldBool){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withObfuscated(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithObfuscated(Style newStyle, Boolean boldBool){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withClickEvent(Lnet/minecraft/text/ClickEvent;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithClickEvent(Style newStyle, ClickEvent clickEvent){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withHoverEvent(Lnet/minecraft/text/HoverEvent;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithHoverEvent(Style newStyle, HoverEvent hoverEvent){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withInsertion(Ljava/lang/String;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithInsertion(Style newStyle, String insertionString){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withFont(Lnet/minecraft/util/Identifier;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithFont(Style newStyle, Identifier fontID){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withFormatting(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithFormatting(Style newStyle, Formatting formatting){
		return keepData(newStyle);
	}

    @ModifyReturnValue(method = "withExclusiveFormatting(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/Style;",
    at=@At("RETURN"))
	private Style fixWithExclusiveFormatting(Style newStyle, Formatting formatting){
		return keepData(newStyle);
	}
}

