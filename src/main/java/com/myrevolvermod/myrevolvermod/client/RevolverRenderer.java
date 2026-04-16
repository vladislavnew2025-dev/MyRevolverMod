package com.myrevolvermod.myrevolvermod.client;

import com.myrevolvermod.myrevolvermod.item.RevolverItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class RevolverRenderer extends GeoItemRenderer<RevolverItem> {
    public RevolverRenderer() {
        super(new RevolverModel());
    }
}
