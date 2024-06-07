package com.vetador.runestack;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static com.vetador.runestack.RuneStack.*;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModToggle {

    public static KeyMapping toggleKeyBinding;

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        toggleKeyBinding = new KeyMapping(
                "key.runestack.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.runestack.keys"
        );
        event.register(toggleKeyBinding);
    }
}
