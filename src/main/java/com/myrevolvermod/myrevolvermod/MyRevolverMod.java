package com.myrevolvermod.myrevolvermod;

import com.myrevolvermod.myrevolvermod.network.ModNetwork;
import com.myrevolvermod.myrevolvermod.registry.ModCreativeModeTabs;
import com.myrevolvermod.myrevolvermod.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(MyRevolverMod.MOD_ID)
public class MyRevolverMod {
    public static final String MOD_ID = "myrevolvermod";

    public MyRevolverMod() {
        GeckoLib.initialize();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modEventBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModNetwork.register();
    }
}
