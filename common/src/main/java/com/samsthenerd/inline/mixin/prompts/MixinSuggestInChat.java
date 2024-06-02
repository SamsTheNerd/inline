package com.samsthenerd.inline.mixin.prompts;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;

@Mixin(ChatScreen.class)
public class MixinSuggestInChat {

    @Shadow
    protected TextFieldWidget chatField;


    @WrapOperation(
        method="render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at=@At(
            value="INVOKE",
            target="net/minecraft/client/gui/screen/ChatInputSuggestor.render (Lnet/minecraft/client/gui/DrawContext;II)V"
        )
    )
    public void renderInlineSuggestions(ChatInputSuggestor chatSuggestor, DrawContext context, int mouseX, int mouseY, Operation<Void> original){
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(chatField.getText());
        // context.drawText(textRenderer, chatField.getText(), textWidth+chatField.getX(), chatField.getY()-12, 0xFFFFFF, true);
        original.call(chatSuggestor, context, mouseX, mouseY);
    }
}
