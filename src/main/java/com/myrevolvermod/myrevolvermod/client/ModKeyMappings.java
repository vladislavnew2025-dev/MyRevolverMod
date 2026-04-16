package com.myrevolvermod.myrevolvermod.client;

import com.myrevolvermod.myrevolvermod.MyRevolverMod;
import com.myrevolvermod.myrevolvermod.item.RevolverItem;
import com.myrevolvermod.myrevolvermod.network.ModNetwork;
import com.myrevolvermod.myrevolvermod.network.ReloadRequestPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MyRevolverMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModKeyMappings {
    public static final KeyMapping RELOAD_KEY = new KeyMapping(
            "key.myrevolvermod.reload",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.gameplay"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(RELOAD_KEY);
    }

    @Mod.EventBusSubscriber(modid = MyRevolverMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientInputEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.screen != null) {
                return;
            }

            while (RELOAD_KEY.consumeClick()) {
                ItemStack heldItem = minecraft.player.getMainHandItem();
                if (heldItem.getItem() instanceof RevolverItem) {
                    ModNetwork.CHANNEL.sendToServer(new ReloadRequestPacket());
                }
            }
        }
    }
}
