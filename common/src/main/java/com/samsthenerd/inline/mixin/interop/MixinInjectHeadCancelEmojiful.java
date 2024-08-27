package com.samsthenerd.inline.mixin.interop;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// It doesn't look like forge has a way to mark a mod as incompatible so this will have to do.
@Pseudo
@Mixin(targets="com.hrznstudio.emojiful.ClientEmojiHandler")
public class MixinInjectHeadCancelEmojiful {
    @Inject(method="initEmojis()V", at=@At("HEAD"), cancellable = true, remap=false)
    private static void WhyWouldYouReplaceTheTextRendererInsteadOfUsingAMixinQuestionMarkQuestionMarkQuestionMark(CallbackInfo cancelTime){
        cancelTime.cancel();
    }
}
