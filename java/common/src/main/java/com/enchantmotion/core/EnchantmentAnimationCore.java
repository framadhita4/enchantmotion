package com.enchantmentanimation.core;

import com.enchantmentanimation.common.EnchantmentAnimationCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Core business and animation logic shared across all mod loader implementations.
 * All common functions (timing, animation lifecycle, particle dispatch, sounds,
 * container sync, and block drops) are centralized here.
 */
public final class EnchantmentAnimationCore {

    public static final String MOD_ID = EnchantmentAnimationCommon.MOD_ID;
    public static final String MOD_NAME = EnchantmentAnimationCommon.MOD_NAME;
    public static final Logger LOGGER = LoggerFactory.getLogger("EnchantmentAnimationCore");

    public static final long ENCHANT_ANIMATION_DURATION_MS = 3600L;
    public static final float ITEM_BASE_START_Y = 0.82F;
    public static final float ITEM_BASE_TARGET_Y = 1.38F;
    public static final float ITEM_SCALE = 0.5F;

    private static boolean initialized = false;

    private EnchantmentAnimationCore() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        EnchantmentAnimationCommon.init();
        LOGGER.info("EnchantMotion Core logic initialized.");
    }

    /**
     * Compute current animation progress from 0.0F to 1.0F.
     */
    public static float getEnchantProgress(long startTimeMs, float partialTick) {
        if (startTimeMs <= 0L) {
            return 1.0F;
        }
        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        if (elapsedMs < 0L || elapsedMs >= ENCHANT_ANIMATION_DURATION_MS) {
            return 1.0F;
        }
        return (float) elapsedMs / (float) ENCHANT_ANIMATION_DURATION_MS;
    }

    /**
     * Check if the enchanting animation is actively running.
     */
    public static boolean isEnchanting(long startTimeMs) {
        if (startTimeMs <= 0L) {
            return false;
        }
        return (System.currentTimeMillis() - startTimeMs) < ENCHANT_ANIMATION_DURATION_MS;
    }

    /**
     * Trigger animation sequence.
     */
    public static void triggerEnchant(EnchantTableItemHolder holder, Level level, BlockPos pos, int tier) {
        if (level == null) {
            return;
        }

        if (level.isClientSide()) {
            spawnChargeStartParticles(level, pos);
        } else if (level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            if (server != null) {
                CompletableFuture.delayedExecutor(ENCHANT_ANIMATION_DURATION_MS, TimeUnit.MILLISECONDS).execute(() -> {
                    server.execute(() -> {
                        BlockEntity be = serverLevel.getBlockEntity(pos);
                        if (be instanceof EnchantTableItemHolder currentHolder && currentHolder.enchantmotion$getEnchantStartTimeMs() > 0L) {
                            currentHolder.enchantmotion$finishEnchantAndDrop(serverLevel, pos);
                        }
                    });
                });
            }
        }
    }

    /**
     * Spawn burst particles when animation begins charging.
     */
    public static void spawnChargeStartParticles(Level level, BlockPos pos) {
        RandomSource rnd = level.getRandom();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.38;
        double cz = pos.getZ() + 0.5;

        for (int i = 0; i < 15; i++) {
            double vx = (rnd.nextDouble() - 0.5) * 0.12;
            double vy = 0.05 + rnd.nextDouble() * 0.1;
            double vz = (rnd.nextDouble() - 0.5) * 0.12;
            level.addParticle(ParticleTypes.END_ROD, cx, cy, cz, vx, vy, vz);
        }
    }

    /**
     * Finish animation, spawn drop entity and explosion particles.
     */
    public static void finishEnchantAndDrop(EnchantTableItemHolder holder, Level level, BlockPos pos) {
        if (level == null || level.isClientSide() || holder.enchantmotion$getEnchantStartTimeMs() <= 0L) {
            return;
        }

        holder.enchantmotion$resetEnchant();
        holder.enchantmotion$setClientExploded(false);

        ItemStack item = holder.enchantmotion$getItem();
        if (!item.isEmpty()) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 1.25;
            double cz = pos.getZ() + 0.5;

            // Spawn dropped ItemEntity above table
            ItemEntity itemEntity = new ItemEntity(level, cx, cy, cz, item.copy());
            itemEntity.setDeltaMovement(0.0, 0.28, 0.0);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);

            // Audio cues
            level.playSound(null, cx, cy, cz, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.9F, 1.25F);
            level.playSound(null, cx, cy, cz, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F);

            // Visual explosion & magic particles on server
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, cx, cy, cz, 3, 0.15, 0.15, 0.15, 0.0);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, 45, 0.3, 0.3, 0.3, 0.22);
                serverLevel.sendParticles(ParticleTypes.ENCHANT, cx, cy, cz, 65, 0.45, 0.45, 0.45, 0.55);
                serverLevel.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 25, 0.25, 0.25, 0.25, 0.15);
            }
        }

        // Clear table state
        holder.enchantmotion$setItem(ItemStack.EMPTY);
        if (holder instanceof BlockEntity be) {
            be.setChanged();
            level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3);
        }
    }

    /**
     * Called during book tick for both client (rendering effects) and server (safety check).
     */
    public static void tickAnimation(EnchantTableItemHolder holder, Level level, BlockPos pos) {
        if (level == null || holder == null) {
            return;
        }

        // Server-side completion safety check
        if (!level.isClientSide()) {
            long startTime = holder.enchantmotion$getEnchantStartTimeMs();
            if (startTime > 0L) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= ENCHANT_ANIMATION_DURATION_MS) {
                    holder.enchantmotion$finishEnchantAndDrop(level, pos);
                }
            }
            return;
        }

        // Client-side visual particle and audio effects
        if (holder.enchantmotion$isEnchanting()) {
            float progress = holder.enchantmotion$getEnchantProgress(0.0F);
            RandomSource rnd = level.getRandom();
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 1.38 + progress * 0.18;
            double cz = pos.getZ() + 0.5;

            // Inward swirling enchantment runes spiraling into the charging item
            if (progress < 0.95F) {
                int runeSpawns = 1 + (int) (progress * 3);
                for (int r = 0; r < runeSpawns; r++) {
                    double radius = 0.65 - progress * 0.35 + rnd.nextDouble() * 0.2;
                    double angle = rnd.nextDouble() * Math.PI * 2.0;
                    double px = cx + Math.cos(angle) * radius;
                    double py = cy + (rnd.nextDouble() - 0.5) * 0.35;
                    double pz = cz + Math.sin(angle) * radius;
                    double vx = (cx - px) * (1.2 + progress * 1.5);
                    double vy = (cy - py) * (1.2 + progress * 1.5);
                    double vz = (cz - pz) * (1.2 + progress * 1.5);
                    level.addParticle(ParticleTypes.ENCHANT, px, py, pz, vx, vy, vz);
                }
            }

            // Sound and spark effects when each lapis lazuli impacts the item
            int activeLapis = Math.min(3, Math.max(1, holder.enchantmotion$getEnchantTier()));
            float dashPhaseStart = 0.20F;
            float dashPhaseEnd = 0.80F;
            float slotDuration = (dashPhaseEnd - dashPhaseStart) / activeLapis;
            float dashDuration = slotDuration * 0.65F;

            int currentMask = holder.enchantmotion$getLapisImpactSoundMask();
            for (int i = 0; i < activeLapis; i++) {
                float dashImpact = dashPhaseStart + i * slotDuration + dashDuration;
                if (progress >= dashImpact && (currentMask & (1 << i)) == 0) {
                    currentMask |= (1 << i);
                    holder.enchantmotion$setLapisImpactSoundMask(currentMask);
                    level.playLocalSound(pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 0.9F, 1.2F + i * 0.25F, false);
                    level.playLocalSound(pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.6F, 1.4F + i * 0.2F, false);
                    for (int k = 0; k < 12; k++) {
                        double vx = (rnd.nextDouble() - 0.5) * 0.22;
                        double vy = (rnd.nextDouble() - 0.5) * 0.22;
                        double vz = (rnd.nextDouble() - 0.5) * 0.22;
                        level.addParticle(ParticleTypes.ENCHANTED_HIT, cx, cy, cz, vx, vy, vz);
                        level.addParticle(ParticleTypes.ELECTRIC_SPARK, cx, cy, cz, vx * 0.7, vy * 0.7, vz * 0.7);
                    }
                }
            }

            // Client explosion at final moment
            if (!holder.enchantmotion$hasClientExploded() && progress >= 0.97F) {
                holder.enchantmotion$setClientExploded(true);
                level.addParticle(ParticleTypes.EXPLOSION, cx, cy, cz, 0.0, 0.0, 0.0);
                for (int i = 0; i < 35; i++) {
                    double vx = (rnd.nextDouble() - 0.5) * 0.3;
                    double vy = 0.05 + rnd.nextDouble() * 0.25;
                    double vz = (rnd.nextDouble() - 0.5) * 0.3;
                    level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, vx, vy, vz);
                    level.addParticle(ParticleTypes.END_ROD, cx, cy, cz, vx * 0.8, vy * 0.8, vz * 0.8);
                }
                level.playLocalSound(pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.9F, 1.25F, false);
                level.playLocalSound(pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    /**
     * Check if table GUI opening should be blocked while actively enchanting.
     */
    public static boolean preventOpenWhileEnchanting(Level level, BlockPos pos) {
        if (level != null && level.getBlockEntity(pos) instanceof EnchantTableItemHolder holder) {
            return holder.enchantmotion$isEnchanting();
        }
        return false;
    }

    /**
     * Restore saved items into container slots on GUI open.
     */
    public static void restoreMenuSlots(Container enchantSlots, ContainerLevelAccess access) {
        access.execute((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnchantTableItemHolder holder) {
                ItemStack savedItem = holder.enchantmotion$getItem();
                ItemStack savedLapis = holder.enchantmotion$getLapis();
                if (!savedItem.isEmpty()) {
                    enchantSlots.setItem(0, savedItem.copy());
                }
                if (!savedLapis.isEmpty()) {
                    enchantSlots.setItem(1, savedLapis.copy());
                }
            }
        });
    }

    /**
     * Sync container slot changes to block entity.
     */
    public static void handleSlotsChanged(Container enchantSlots, ContainerLevelAccess access) {
        access.execute((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnchantTableItemHolder holder) {
                ItemStack currentItem = enchantSlots.getItem(0);
                holder.enchantmotion$setItem(currentItem.isEmpty() ? ItemStack.EMPTY : currentItem.copy());
                ItemStack currentLapis = enchantSlots.getItem(1);
                holder.enchantmotion$setLapis(currentLapis.isEmpty() ? ItemStack.EMPTY : currentLapis.copy());
                be.setChanged();
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            }
        });
    }

    /**
     * Trigger animation and safely close container when enchantment button is pressed.
     */
    public static void handleClickMenuButton(Player player, int id, ContainerLevelAccess access) {
        if (player instanceof ServerPlayer serverPlayer) {
            access.execute((level, pos) -> {
                level.blockEvent(pos, level.getBlockState(pos).getBlock(), 1, id);
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof EnchantTableItemHolder holder) {
                    holder.enchantmotion$triggerEnchant(id);
                }
            });

            serverPlayer.level().getServer().execute(serverPlayer::closeContainer);
        }
    }

    /**
     * Persist items to block entity and remove silently from container when menu closes.
     */
    public static void handleMenuClosed(Container enchantSlots, ContainerLevelAccess access) {
        access.execute((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnchantTableItemHolder holder) {
                ItemStack currentItem = enchantSlots.getItem(0);
                holder.enchantmotion$setItem(currentItem.isEmpty() ? ItemStack.EMPTY : currentItem.copy());
                ItemStack currentLapis = enchantSlots.getItem(1);
                holder.enchantmotion$setLapis(currentLapis.isEmpty() ? ItemStack.EMPTY : currentLapis.copy());
                be.setChanged();
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            }
            enchantSlots.removeItemNoUpdate(0);
            enchantSlots.removeItemNoUpdate(1);
        });
        enchantSlots.removeItemNoUpdate(0);
        enchantSlots.removeItemNoUpdate(1);
    }

    /**
     * Drop contained item and lapis when block is broken.
     */
    public static void handleBlockDestroyed(LevelAccessor levelAccessor, BlockPos pos) {
        if (!(levelAccessor instanceof Level level) || level.isClientSide()) {
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EnchantTableItemHolder holder) {
            ItemStack item = holder.enchantmotion$getItem();
            if (!item.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item);
                holder.enchantmotion$setItem(ItemStack.EMPTY);
            }
            ItemStack lapis = holder.enchantmotion$getLapis();
            if (!lapis.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, lapis);
                holder.enchantmotion$setLapis(ItemStack.EMPTY);
            }
            holder.enchantmotion$resetEnchant();
        }
    }

    /**
     * Compute effective bookshelf power level (1, 2, or 3) across all platforms.
     */
    public static int calculateBookshelfLevel(Level level, BlockPos tablePos) {
        if (level == null || tablePos == null) {
            return 1;
        }

        int count = 0;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(level, tablePos, offset)) {
                count++;
            }
        }

        if (count >= 15) {
            return 3;
        } else if (count >= 5) {
            return 2;
        }
        return 1;
    }

    /**
     * Save item and lapis data to block entity storage.
     */
    public static void saveItemData(EnchantTableItemHolder holder, ValueOutput output) {
        if (!holder.enchantmotion$getItem().isEmpty()) {
            output.store("EnchantedItem", ItemStack.OPTIONAL_CODEC, holder.enchantmotion$getItem());
        }
        if (!holder.enchantmotion$getLapis().isEmpty()) {
            output.store("EnchantedLapis", ItemStack.OPTIONAL_CODEC, holder.enchantmotion$getLapis());
        }
    }

    /**
     * Load item and lapis data from block entity storage.
     */
    public static void loadItemData(EnchantTableItemHolder holder, ValueInput input) {
        holder.enchantmotion$setItem(input.read("EnchantedItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        holder.enchantmotion$setLapis(input.read("EnchantedLapis", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }
}
