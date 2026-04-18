package com.myrevolvermod.myrevolvermod.registry;

import com.myrevolvermod.myrevolvermod.MyRevolverMod;
import com.myrevolvermod.myrevolvermod.item.AwpItem;
import com.myrevolvermod.myrevolvermod.item.RevolverItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MyRevolverMod.MOD_ID);

    public static final RegistryObject<Item> REVOLVER = ITEMS.register("revolver",
            () -> new RevolverItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> AWP = ITEMS.register("awp",
            () -> new AwpItem(new Item.Properties().stacksTo(1)));
}
