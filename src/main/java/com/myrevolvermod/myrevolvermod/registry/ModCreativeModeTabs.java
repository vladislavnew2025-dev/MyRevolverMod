package com.myrevolvermod.myrevolvermod.registry;

import com.myrevolvermod.myrevolvermod.MyRevolverMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MyRevolverMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MYREVOLVERMOD_TAB = CREATIVE_MODE_TABS.register("myrevolvermod_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.myrevolvermod.main"))
                    .icon(() -> new ItemStack(ModItems.REVOLVER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.REVOLVER.get());
                        output.accept(ModItems.AWP.get());
                    })
                    .build());
}
