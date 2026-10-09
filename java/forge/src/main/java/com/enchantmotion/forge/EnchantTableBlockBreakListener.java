package com.enchantmentanimation.forge;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnchantmentAnimationCore.MOD_ID)
public class EnchantTableBlockBreakListener {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        EnchantmentAnimationCore.handleBlockDestroyed(level, pos);
    }
}
