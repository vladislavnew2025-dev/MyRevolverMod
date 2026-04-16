package com.myrevolvermod.myrevolvermod.item;

import com.myrevolvermod.myrevolvermod.client.RevolverRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    private static final double RANGE = 32.0;

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
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                setAmmo(stack, MAX_AMMO);
                triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "reload");
                level.playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 0.9f, 0.9f);
            }
            player.getCooldowns().addCooldown(this, 12);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        int ammo = getAmmo(stack);
        if (ammo <= 0) {
            if (!level.isClientSide) {
                level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.0f);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            fire(level, player);
            setAmmo(stack, ammo - 1);
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "controller", "fire");
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 1.8f);
        }

        player.getCooldowns().addCooldown(this, 4);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private void fire(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(RANGE));

        HitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDistSqr = start.distanceToSqr(blockHit.getLocation());

        Entity hitEntity = null;
        Vec3 hitPos = blockHit.getLocation();

        for (Entity candidate : level.getEntities(player, new AABB(start, end).inflate(1.5))) {
            if (!(candidate instanceof LivingEntity living) || candidate == player) {
                continue;
            }

            AABB inflated = candidate.getBoundingBox().inflate(0.3);
            EntityHitResult entityHit = inflated.clip(start, end).map(vec3 -> new EntityHitResult(candidate, vec3)).orElse(null);
            if (entityHit == null) {
                continue;
            }

            double distSqr = start.distanceToSqr(entityHit.getLocation());
            if (distSqr <= maxDistSqr) {
                maxDistSqr = distSqr;
                hitEntity = candidate;
                hitPos = entityHit.getLocation();
            }
        }

        if (hitEntity instanceof LivingEntity livingTarget) {
            DamageSource source = player.damageSources().playerAttack(player);
            livingTarget.hurt(source, 7.0f);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, hitPos.x, hitPos.y, hitPos.z, 2, 0.02, 0.02, 0.02, 0.001);
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
        return getAmmo(stack) < MAX_AMMO;
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
