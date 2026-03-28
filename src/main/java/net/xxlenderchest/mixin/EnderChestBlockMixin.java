package net.xxlenderchest.mixin;

import net.xxlenderchest.XXLEnderChest;
import net.xxlenderchest.config.XXLConfig;
import net.xxlenderchest.permission.PermissionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts the ender chest block's useWithoutItem method to open a ChestMenu
 * with 4, 5, or 6 rows instead of the vanilla 3-row menu.
 *
 * Uses setActiveChest() exactly like vanilla to correctly drive the lid animation.
 */
@Mixin(EnderChestBlock.class)
public class EnderChestBlockMixin {

    private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");

    @Inject(
            method = "useWithoutItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void xxlenderchest$onUseWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        XXLConfig config = XXLEnderChest.getConfig();

        if (!config.isEnabled() || !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }

        int rows = PermissionHelper.resolveRows(serverPlayer, config);
        if (rows <= 3) {
            return;
        }

        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
        if (enderChest == null || !(level.getBlockEntity(pos) instanceof EnderChestBlockEntity enderChestBE)) {
            return;
        }

        // Check whether a solid block above prevents opening (vanilla parity)
        BlockPos abovePos = pos.above();
        if (level.getBlockState(abovePos).isRedstoneConductor(level, abovePos)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            MenuType<?> menuType = rowsToMenuType(rows);

            // setActiveChest drives the lid animation, same as vanilla
            enderChest.setActiveChest(enderChestBE);
            player.openMenu(new SimpleMenuProvider(
                    (syncId, playerInv, p) -> new ChestMenu(menuType, syncId, playerInv, enderChest, rows),
                    CONTAINER_TITLE
            ));
            player.awardStat(Stats.OPEN_ENDERCHEST);
            PiglinAi.angerNearbyPiglins(serverLevel, player, true);
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    private static MenuType<?> rowsToMenuType(int rows) {
        return switch (rows) {
            case 4  -> MenuType.GENERIC_9x4;
            case 5  -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }
}
