package com.samsthenerd.inline.mixin.core;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.impl.InlineStyle;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(Style.class)
public class MixinInlineStyle implements InlineStyle {

    @Unique
    private final Map<ISComponent<?>, Object> components = new HashMap<>();

    @Unique
    private Style getCopy(){
        return InlineStyle.makeCopy((Style)(Object)this);
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
        Style maybeNewStyle = InlineStyle.makeCopy(original);
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
		if (original && this != obj && (obj instanceof InlineStyle style)) {
            Set<ISComponent> allComps = Stream.concat(
                    this.components.keySet().stream(),
                    style.getComponents().stream()
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

