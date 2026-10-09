package com.enchantmentanimation.core.mixin;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin extends AbstractContainerMenu {

    @Shadow(remap = false)
    @Final
    private ContainerLevelAccess access;

    @Shadow(remap = false)
    @Final
    private Container enchantSlots;

    @Unique
    private boolean enchantmotion$isClosing = false;

    @Unique
    private boolean enchantmotion$isRestoring = false;

    protected EnchantmentMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"), remap = false)
    private void enchantmotion$restoreItemOnOpen(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        this.enchantmotion$isRestoring = true;
        try {
            EnchantmentAnimationCore.restoreMenuSlots(this.enchantSlots, this.access);
            this.broadcastChanges();
        } finally {
            this.enchantmotion$isRestoring = false;
        }
    }

    @Inject(method = "slotsChanged", at = @At("TAIL"), remap = false)
    private void enchantmotion$onSlotsChanged(Container container, CallbackInfo ci) {
        if (this.enchantmotion$isClosing || this.enchantmotion$isRestoring) {
            return;
        }
        EnchantmentAnimationCore.handleSlotsChanged(this.enchantSlots, this.access);
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"), remap = false)
    private void enchantmotion$onClickMenuButton(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            EnchantmentAnimationCore.handleClickMenuButton(player, id, this.access);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"), remap = false)
    private void enchantmotion$persistItemOnClose(Player player, CallbackInfo ci) {
        this.enchantmotion$isClosing = true;
        EnchantmentAnimationCore.handleMenuClosed(this.enchantSlots, this.access);
    }
}
