package com.myrevolvermod.myrevolvermod.network;

import com.myrevolvermod.myrevolvermod.MyRevolverMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MyRevolverMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ReloadRequestPacket.class,
                ReloadRequestPacket::encode,
                ReloadRequestPacket::decode,
                ReloadRequestPacket::handle);
    }
}
