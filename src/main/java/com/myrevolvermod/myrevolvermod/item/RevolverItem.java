package com.myrevolvermod.myrevolvermod.item;

import com.myrevolvermod.myrevolvermod.client.RevolverRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class RevolverItem extends Item implements GeoItem {
    private static final String AMMO_TAG = "Ammo";
    private static final int MAX_AMMO = 6;

    private static final RawAnimation IDLE = RawAnimation.begin().then("idle", Animation.LoopType.LOOP);
    private static final RawAnimation FIRE = RawAnimation.begin().thenPlay("fire");
    private static final RawAnimation RELOAD = RawAnimation.begin().thenPlay("reload");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RevolverItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private RevolverRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new RevolverRenderer();
                }
                return this.renderer;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack,
                                                   LocalPlayer player,
                                                   HumanoidArm arm,
                                                   ItemStack itemInHand,
                                                   float partialTick,
                                                   float equipProcess,
                                                   float swingProcess) {
                // Apply stable base first-person transform, but skip vanilla swing/use bobbing.
                int armSide = arm == HumanoidArm.RIGHT ? 1 : -1;
                poseStack.translate(armSide * 0.56F, -0.52F, -0.72F);
                return true;
            }
        });
    }


    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                reloadFromInventory(player, stack, true);
            }
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.consume(stack);
        }

        if (getAmmo(stack) <= 0) {
            if (!level.isClientSide) {
                level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.0f);
            }
            return InteractionResultHolder.consume(stack);
        }

        if (!level.isClientSide) {
            fire(level, player);
            setAmmo(stack, getAmmo(stack) - 1);
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "fire");
            level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.55f, 1.3f);
        }

        player.getCooldowns().addCooldown(this, 4);
        return InteractionResultHolder.consume(stack);
    }

    public boolean reloadFromInventory(Player player, ItemStack stack, boolean triggerAnimation) {
        int currentAmmo = getAmmo(stack);
        int missingAmmo = MAX_AMMO - currentAmmo;
        if (missingAmmo <= 0) {
            return false;
        }

        int ammoInInventory = countAmmoItems(player);
        if (ammoInInventory <= 0 && !player.getAbilities().instabuild) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.0f);
            return false;
        }

        int toLoad = player.getAbilities().instabuild ? missingAmmo : Math.min(missingAmmo, ammoInInventory);
        if (!player.getAbilities().instabuild) {
            consumeAmmoItems(player, toLoad);
        }

        setAmmo(stack, currentAmmo + toLoad);

        if (triggerAnimation && player.level() instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "reload");
        }

        player.level().playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 0.9f, 0.9f);
        player.getCooldowns().addCooldown(this, 12);
        return true;
    }

    private void fire(Level level, Player player) {
        SmallFireball fireball = new SmallFireball(level, player,
                player.getLookAngle().x,
                player.getLookAngle().y,
                player.getLookAngle().z);
        fireball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        fireball.setDeltaMovement(player.getLookAngle().scale(1.5));
        fireball.setNoGravity(true);
        level.addFreshEntity(fireball);
    }

    private int countAmmoItems(Player player) {
        int total = 0;
        for (ItemStack inventoryStack : player.getInventory().items) {
            if (inventoryStack.is(Items.FIRE_CHARGE)) {
                total += inventoryStack.getCount();
            }
        }
        return total;
    }

    private void consumeAmmoItems(Player player, int amount) {
        for (ItemStack inventoryStack : player.getInventory().items) {
            if (amount <= 0) {
                return;
            }
            if (!inventoryStack.is(Items.FIRE_CHARGE)) {
                continue;
            }

            int consumed = Math.min(amount, inventoryStack.getCount());
            inventoryStack.shrink(consumed);
            amount -= consumed;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<RevolverItem> controller = new AnimationController<>(this, "controller", 2, state -> {
            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        });

        controller.triggerableAnim("fire", FIRE);
        controller.triggerableAnim("reload", RELOAD);
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((13.0f * getAmmo(stack)) / MAX_AMMO);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xD07A38;
    }

    private int getAmmo(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(AMMO_TAG)) {
            tag.putInt(AMMO_TAG, MAX_AMMO);
        }
        return tag.getInt(AMMO_TAG);
    }

    private void setAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt(AMMO_TAG, Math.max(0, Math.min(MAX_AMMO, ammo)));
    }
}
