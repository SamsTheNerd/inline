package com.samsthenerd.inline.mixin;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.samsthenerd.inline.utils.mixin.RequireMods;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@ApiStatus.Internal
public class InlineMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Inline Mixin Plugin");

    @Override
    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetName, String className) {
        try {
            List<AnnotationNode> annotationNodes = MixinService.getService()
              .getBytecodeProvider()
              .getClassNode(className).visibleAnnotations;
            if (annotationNodes == null) return true;

            boolean shouldApply = true;
            for (AnnotationNode node : annotationNodes) {
                if (node.desc.equals(Type.getDescriptor(RequireMods.class))) {
                    List<String> modIds = Annotations.getValue(node, "value");
                    boolean applyIfPresent = Annotations.getValue(node, "applyIfPresent", Boolean.TRUE);
                    if (anyModsLoaded(modIds)) {
                        LOGGER.debug("{} is {}being applied because {} are loaded",
                          className,
                          applyIfPresent ? "" : "not ",
                          modIds
                        );
                        shouldApply = applyIfPresent;
                    } else {
                        LOGGER.debug("{} is {}being applied because {} are not loaded",
                          className,
                          !applyIfPresent ? "" : "not ",
                          modIds
                        );
                        shouldApply = !applyIfPresent;
                    }
                }

                if(!shouldApply) break;
            }

            return shouldApply;
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    private static boolean anyModsLoaded(List<String> modIds) {
        for (String modId : modIds) {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                return true;
            }
        }
        return false;
    }
}
