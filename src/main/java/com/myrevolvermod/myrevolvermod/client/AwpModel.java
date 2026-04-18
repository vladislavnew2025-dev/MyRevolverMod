package com.myrevolvermod.myrevolvermod.client;

import com.myrevolvermod.myrevolvermod.MyRevolverMod;
import com.myrevolvermod.myrevolvermod.item.AwpItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AwpModel extends GeoModel<AwpItem> {
    @Override
    public ResourceLocation getModelResource(AwpItem animatable) {
        return new ResourceLocation(MyRevolverMod.MOD_ID, "geo/awp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AwpItem animatable) {
        return new ResourceLocation(MyRevolverMod.MOD_ID, "textures/item/awp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AwpItem animatable) {
        return new ResourceLocation(MyRevolverMod.MOD_ID, "animations/awp.animation.json");
    }
}
