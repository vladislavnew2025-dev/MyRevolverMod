package com.myrevolvermod.myrevolvermod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.myrevolvermod.myrevolvermod.item.AwpItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AwpRenderer extends GeoItemRenderer<AwpItem> {
    public AwpRenderer() {
        super(new AwpModel());
    }

    @Override
    public void renderByItem(ItemStack stack,
                             ItemDisplayContext displayContext,
                             PoseStack poseStack,
                             MultiBufferSource bufferSource,
                             int packedLight,
                             int packedOverlay) {
        boolean firstPerson = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        this.getGeoModel().getBone("right").ifPresent(bone -> bone.setHidden(!firstPerson));
        this.getGeoModel().getBone("left").ifPresent(bone -> bone.setHidden(!firstPerson));

        super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
