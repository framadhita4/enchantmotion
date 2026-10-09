package com.enchantmentanimation.core.mixin;

import com.enchantmentanimation.core.EnchantTableItemHolder;
import com.enchantmentanimation.core.EnchantmentAnimationCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantingTableBlockEntityMixin extends BlockEntity implements EnchantTableItemHolder {

    @Unique
    private ItemStack enchantmotion$item = ItemStack.EMPTY;

    @Unique
    private ItemStack enchantmotion$lapis = ItemStack.EMPTY;

    @Unique
    private long enchantmotion$enchantStartTimeMs = -1L;

    @Unique
    private int enchantmotion$enchantTier = 1;

    @Unique
    private boolean enchantmotion$clientExploded = false;

    @Unique
    private int enchantmotion$lapisImpactSoundMask = 0;

    public EnchantingTableBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public ItemStack enchantmotion$getItem() {
        return this.enchantmotion$item == null ? ItemStack.EMPTY : this.enchantmotion$item;
    }

    @Override
    public void enchantmotion$setItem(ItemStack stack) {
        ItemStack oldStack = this.enchantmotion$item;
        this.enchantmotion$item = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack;

        // Auto-trigger animation if item transformed into enchanted item and not currently enchanting
        if (!this.enchantmotion$item.isEmpty() && !enchantmotion$isEnchanting()) {
            boolean wasEnchanted = oldStack != null && (oldStack.isEnchanted() || oldStack.is(Items.ENCHANTED_BOOK));
            boolean isEnchanted = this.enchantmotion$item.isEnchanted() || this.enchantmotion$item.is(Items.ENCHANTED_BOOK);
            if (!wasEnchanted && isEnchanted) {
                enchantmotion$triggerEnchant(1);
            }
        }
    }

    @Override
    public ItemStack enchantmotion$getLapis() {
        return this.enchantmotion$lapis == null ? ItemStack.EMPTY : this.enchantmotion$lapis;
    }

    @Override
    public void enchantmotion$setLapis(ItemStack stack) {
        this.enchantmotion$lapis = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack;
    }

    @Override
    public void enchantmotion$triggerEnchant(int tier) {
        this.enchantmotion$enchantStartTimeMs = System.currentTimeMillis();
        this.enchantmotion$enchantTier = Math.max(1, tier + 1);
        this.enchantmotion$clientExploded = false;
        this.enchantmotion$lapisImpactSoundMask = 0;

        EnchantmentAnimationCore.triggerEnchant(this, this.level, this.worldPosition, tier);
    }

    @Override
    public float enchantmotion$getEnchantProgress(float partialTick) {
        return EnchantmentAnimationCore.getEnchantProgress(this.enchantmotion$enchantStartTimeMs, partialTick);
    }

    @Override
    public boolean enchantmotion$isEnchanting() {
        return EnchantmentAnimationCore.isEnchanting(this.enchantmotion$enchantStartTimeMs);
    }

    @Override
    public int enchantmotion$getEnchantTier() {
        return this.enchantmotion$enchantTier;
    }

    @Override
    public void enchantmotion$finishEnchantAndDrop(Level level, BlockPos pos) {
        EnchantmentAnimationCore.finishEnchantAndDrop(this, level, pos);
    }

    @Override
    public void enchantmotion$resetEnchant() {
        this.enchantmotion$enchantStartTimeMs = -1L;
        this.enchantmotion$lapisImpactSoundMask = 0;
    }

    @Override
    public boolean enchantmotion$hasClientExploded() {
        return this.enchantmotion$clientExploded;
    }

    @Override
    public void enchantmotion$setClientExploded(boolean exploded) {
        this.enchantmotion$clientExploded = exploded;
    }

    @Override
    public int enchantmotion$getLapisImpactSoundMask() {
        return this.enchantmotion$lapisImpactSoundMask;
    }

    @Override
    public void enchantmotion$setLapisImpactSoundMask(int mask) {
        this.enchantmotion$lapisImpactSoundMask = mask;
    }

    @Override
    public long enchantmotion$getEnchantStartTimeMs() {
        return this.enchantmotion$enchantStartTimeMs;
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.enchantmotion$triggerEnchant(type);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Inject(method = "bookAnimationTick", at = @At("TAIL"), remap = false)
    private static void enchantmotion$onBookTick(Level level, BlockPos pos, BlockState state, EnchantingTableBlockEntity be, CallbackInfo ci) {
        if (be instanceof EnchantTableItemHolder holder) {
            EnchantmentAnimationCore.tickAnimation(holder, level, pos);
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = false)
    private void enchantmotion$saveItem(ValueOutput output, CallbackInfo ci) {
        EnchantmentAnimationCore.saveItemData(this, output);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void enchantmotion$loadItem(ValueInput input, CallbackInfo ci) {
        EnchantmentAnimationCore.loadItemData(this, input);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
