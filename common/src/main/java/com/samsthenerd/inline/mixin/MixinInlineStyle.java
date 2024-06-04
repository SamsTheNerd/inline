package com.samsthenerd.inline.mixin;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.impl.InlineStyle;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Mixin(Style.class)
public class MixinInlineStyle implements InlineStyle {

    private InlineData data = null;
    private boolean _isGlowy = false; // this is purely client so doesn't need to be 

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



    // @Override
    // public Style setPattern(HexPattern pattern) {
    //     // yoinked from PatternTooltipComponent
    //     this.pattern = pattern;
    //     Pair<Float, List<Vec2f> > pair = RenderLib.getCenteredPattern(pattern, RENDER_SIZE, RENDER_SIZE, 16f);
    //     this.patScale = pair.getFirst();
    //     List<Vec2f> dots = pair.getSecond();
    //     this.zappyPoints = RenderLib.makeZappy(
    //         dots, RenderLib.findDupIndices(pattern.positions()),
    //         10, 0.8f, 0f, 0f, RenderLib.DEFAULT_READABILITY_OFFSET, RenderLib.DEFAULT_LAST_SEGMENT_LEN_PROP,
    //         0.0);
    //     this.pathfinderDots = dots;
    //     return (Style)(Object)this;
    // }

    // @Override
    // public Style withPattern(HexPattern pattern, boolean withPatternHoverEvent, boolean withPatternClickEvent) {
    //     Style style = (Style)(Object)this;

    //     if (withPatternHoverEvent) {
    //         StringBuilder bob = new StringBuilder();
    //         bob.append(pattern.getStartDir());
    //         var sig = pattern.anglesSignature();
    //         if (!sig.isEmpty()) {
    //             bob.append(" ");
    //             bob.append(sig);
    //         }
    //         Text hoverText = Text.translatable("hexcasting.tooltip.pattern_iota",
    //             Text.literal(bob.toString())).setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GOLD));
    //         ItemStack scrollStack = new ItemStack(HexItems.SCROLL_LARGE);
    //         scrollStack.setCustomName(hoverText);
    //         HexItems.SCROLL_LARGE.writeDatum(scrollStack, new PatternIota(pattern));
    //         style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(scrollStack)));
    //     }
    //     if(withPatternClickEvent){
    //         style = style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "<" + 
    //             pattern.getStartDir().toString().replace("_", "").toLowerCase() + "," + pattern.anglesSignature() + ">"));
    //     }
    //     return style.withParent(PatternStyle.fromPattern(pattern));
    // }

    
    // @Override
    // public Style setHidden(boolean hidden){
    //     this._isHidden = hidden;
    //     return (Style)(Object)this;
    // }

    // @Override
    // public Style withHidden(boolean hidden){
    //     return ((Style)(Object)this).withParent(((PatternStyle)Style.EMPTY.withBold(null)).setHidden(hidden));
    // }

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
        // if(this.isHidden() || ((PatternStyle) parent).isHidden()){
        //     ((PatternStyle) original).setHidden(true);
        // }
		return original;
	}

	@Inject(method = "equals(Ljava/lang/Object;)Z", at = @At("HEAD"), cancellable = true)
	private void InlineStyEquals(Object obj, CallbackInfoReturnable<Boolean> cir) {
		if (this != obj && (obj instanceof InlineStyle style)) {
			if (!Objects.equals(this.getInlineData(), style.getInlineData())) {
				cir.setReturnValue(false);
			}
            // if(this.isHidden() != style.isHidden()){
            //     cir.setReturnValue(false);
            // }
		}
	}

    private static final String DATA_KEY = "inlineData";
    private static final String HIDDEN_KEY = "isHidden";

	// @Mixin(Style.Serializer.class)
	// public static class MixinPatternStyleSerializer {
	// 	@ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
	// 	private Style InlineStyDeserialize(Style initialStyle, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
	// 		if (!jsonElement.isJsonObject() || initialStyle == null) {
	// 			return initialStyle;
	// 		}
	// 		JsonObject json = jsonElement.getAsJsonObject();
	// 		if (!json.has(DATA_KEY)) {
	// 			return initialStyle;
	// 		}
    //         Boolean hiddenFromJson = JsonHelper.hasBoolean(json, HIDDEN_KEY) ? JsonHelper.getBoolean(json, HIDDEN_KEY) : false;
    //         InlineData data = InlineData.fromJson(json.get(DATA_KEY));


    //         HexDir startDir = HexDir.fromString(startDirString);
    //         HexPattern pattern = HexPattern.fromAngles(angleSigString, startDir);
    //         return initialStyle.withPattern(pattern).setHidden(hiddenFromJson);
	// 	}

	// 	@ModifyReturnValue(method = "serialize", at = @At("RETURN"))
	// 	private JsonElement HexPatStySerialize(JsonElement jsonElement, Style style, Type type, JsonSerializationContext jsonSerializationContext) {
	// 		PatternStyle pStyle = (PatternStyle) style;
	// 		if (jsonElement == null || !jsonElement.isJsonObject() || pStyle.getPattern() == null) {
	// 			return jsonElement;
	// 		}
	// 		JsonObject json = jsonElement.getAsJsonObject();
    //         json.add(PATTERN_HIDDEN_KEY, new JsonPrimitive(pStyle.isHidden()));
    //         JsonObject patternObj = new JsonObject();
    //         patternObj.addProperty(PATTERN_START_DIR_KEY, pStyle.getPattern().getStartDir().toString());
    //         patternObj.addProperty(PATTERN_ANGLE_SIG_KEY, pStyle.getPattern().anglesSignature());
	// 		json.add(PATTERN_KEY, patternObj);
    //         return json;
	// 	}
	// }

    // // meant to be called at the 
    // private Style keepPattern(Style returnedStyle){
    //     PatternStyle pStyle = (PatternStyle)(Object)this;
    //     if(pStyle.getPattern() != null){
    //         ((PatternStyle) returnedStyle).setPattern(pStyle.getPattern());
    //     }
    //     if(pStyle.isHidden()){
    //         ((PatternStyle) returnedStyle).setHidden(true);
    //     }
    //     return returnedStyle;
    // }

    private Style keepData(Style newStyle){
        if(this.getInlineData() != null){
            ((InlineStyle) newStyle).setData(this.getInlineData());
            ((InlineStyle) newStyle).setGlowyMarker((this.hasGlowyMarker()));
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

