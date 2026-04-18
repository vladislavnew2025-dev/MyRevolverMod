package com.myrevolvermod.myrevolvermod.item;

import com.myrevolvermod.myrevolvermod.client.AwpRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class AwpItem extends RevolverItem {
    public AwpItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AwpRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AwpRenderer();
                }
                return this.renderer;
            }
        });
    }
}
