package com.myrevolvermod.myrevolvermod.client;

import com.myrevolvermod.myrevolvermod.MyRevolverMod;
import com.myrevolvermod.myrevolvermod.item.RevolverItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RevolverModel extends GeoModel<RevolverItem> {
    @Override
    public ResourceLocation getModelResource(RevolverItem animatable) {
        return new ResourceLocation(MyRevolverMod.MOD_ID, "geo/revolver.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RevolverItem animatable) {
        return new ResourceLocation(MyRevolverMod.MOD_ID, "textures/item/revolver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RevolverItem animatable) {
        return new ResourceLocation(MyRevolverMod.MOD_ID, "animations/revolver.animation.json");
    }
}
