package com.myrevolvermod.myrevolvermod.network;

import com.myrevolvermod.myrevolvermod.item.RevolverItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReloadRequestPacket {
    public static void encode(ReloadRequestPacket packet, FriendlyByteBuf buffer) {
    }

    public static ReloadRequestPacket decode(FriendlyByteBuf buffer) {
        return new ReloadRequestPacket();
    }

    public static void handle(ReloadRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof RevolverItem revolverItem) {
                revolverItem.reloadFromInventory(player, stack, true);
            }
        });
        context.setPacketHandled(true);
    }
}
