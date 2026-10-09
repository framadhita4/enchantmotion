package com.enchantmentanimation.neoforge;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = EnchantmentAnimationCore.MOD_ID)
public class EnchantTableBlockBreakListener {

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        EnchantmentAnimationCore.handleBlockDestroyed(level, pos);
    }
}
