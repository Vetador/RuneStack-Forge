package com.vetador.runestack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.mojang.text2speech.Narrator.LOGGER;
import static com.vetador.runestack.ModToggle.toggleKeyBinding;
import static com.vetador.runestack.RuneStack.MODID;
import static com.vetador.runestack.RuneStack.featuresEnabled;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class TickHandler {

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event)
    {
        if (toggleKeyBinding == null) {
            return;
        }
        if (event.phase == TickEvent.Phase.END)
        {
            while (toggleKeyBinding.consumeClick()) {
                toggleEnabled();
            }
        }
    }

    static void toggleEnabled() {
        featuresEnabled = !featuresEnabled;
        LOGGER.info("RuneStack is now: " + (featuresEnabled ? "enabled" : "disabled"));
    }

}
