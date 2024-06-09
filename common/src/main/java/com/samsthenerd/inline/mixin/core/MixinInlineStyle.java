package com.samsthenerd.inline.mixin.core;

import java.lang.reflect.Type;
import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.impl.InlineStyle;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

@Mixin(Style.class)
public class MixinInlineStyle implements InlineStyle {

    private InlineData data = null;
    private boolean _isGlowy = false; // this is purely client so doesn't need to be serialized
    private boolean _hidden = false; 

    @Override
    public InlineData getInlineData(){
        return data;
    }

    @Override
    public Style withInlineData(InlineData data){
        return ((InlineStyle)((Style)(Object)this).withBold(null)).setData(data);
    }

    @Override
    public Style setData(InlineData data){
        this.data = data;
        return (Style)(Object)this;
    }

    @Override
    public Style setGlowyMarker(boolean glowy){
        this._isGlowy = glowy;
        return (Style)(Object)this;
    }

    @Override
    public Style withGlowyMarker(boolean glowy){
        return ((InlineStyle)((Style)(Object)this).withBold(null)).setGlowyMarker(glowy);
    }

    @Override
    public boolean hasGlowyMarker(){
        return this._isGlowy;
    }


    @Override
    public Style setHidden(boolean hidden){
        this._hidden = hidden;
        return (Style)(Object)this;
    }

    @Override
    public Style withHidden(boolean hidden){
        return ((Style)(Object)this).withParent(((InlineStyle)Style.EMPTY.withBold(null)).setHidden(hidden));
    }

    @Override
    public boolean isHidden(){
        return _hidden;
    }

    @ModifyReturnValue(method = "withParent(Lnet/minecraft/text/Style;)Lnet/minecraft/text/Style;", at = @At("RETURN"))
	private Style InlineStyWithParent(Style original, Style parent) {
        if(this.getInlineData() != null){
            original = ((InlineStyle) original).withInlineData(this.getInlineData());
        } else { // no data on this style, try falling back to inherit parent
            InlineData parentData = ((InlineStyle) parent).getInlineData();
            if(parentData != null){
                original = ((InlineStyle) original).withInlineData(parentData);
            }
        }
        if(this.isHidden() || ((InlineStyle) parent).isHidden()){
            ((InlineStyle) original).setHidden(true);
        }
		return original;
	}
	@Inject(method = "equals(Ljava/lang/Object;)Z", at = @At("HEAD"), cancellable = true)
	private void InlineStyEquals(Object obj, CallbackInfoReturnable<Boolean> cir) {
		if (this != obj && (obj instanceof InlineStyle style)) {
			if (!Objects.equals(this.getInlineData(), style.getInlineData())) {
				cir.setReturnValue(false);
			}
            if(this.isHidden() != style.isHidden()){
                cir.setReturnValue(false);
            }
		}
	}

    private static final String DATA_KEY = "inlineData";
    private static final String HIDDEN_KEY = "isHidden";

	@Mixin(Style.Serializer.class)
	public static class MixinInlineStyleSerializer {
		@ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
		private Style InlineStyDeserialize(Style initialStyle, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
			if (!jsonElement.isJsonObject() || initialStyle == null) {
				return initialStyle;
			}
			JsonObject json = jsonElement.getAsJsonObject();
			if (!json.has(DATA_KEY)) {
				return initialStyle;
			}
            Boolean hiddenFromJson = JsonHelper.hasBoolean(json, HIDDEN_KEY) ? JsonHelper.getBoolean(json, HIDDEN_KEY) : false;
            InlineData data = InlineAPI.INSTANCE.deserializeData(json.get(DATA_KEY).getAsJsonObject());

            return ((InlineStyle)initialStyle).withInlineData(data).setHidden(hiddenFromJson);
		}

		@ModifyReturnValue(method = "serialize", at = @At("RETURN"))
		private JsonElement HexPatStySerialize(JsonElement jsonElement, Style style, Type type, JsonSerializationContext jsonSerializationContext) {
			InlineStyle iStyle = (InlineStyle) style;
			if (jsonElement == null || !jsonElement.isJsonObject()) {
				return jsonElement;
			}
			JsonObject json = jsonElement.getAsJsonObject();
            if(iStyle.isHidden()){
                json.add(HIDDEN_KEY, new JsonPrimitive(true));
            }
            InlineData data = iStyle.getInlineData();
            if(data != null){
                json.add(DATA_KEY, InlineAPI.INSTANCE.serializeData(data));
            }
            return json;
		}
	}

    private Style keepData(Style newStyle){
        if(this.getInlineData() != null){
            ((InlineStyle) newStyle).setData(this.getInlineData());
            ((InlineStyle) newStyle).setGlowyMarker((this.hasGlowyMarker()));
        }
        if(this.isHidden()){
            ((InlineStyle) newStyle).setHidden(true);
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

