package net.xxlenderchest.mixin;

import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Expands the internal storage of {@link PlayerEnderChestContainer} from 27 slots
 * (vanilla default) to 54 slots (6 rows x 9 columns - the maximum this mod supports).
 *
 * We always allocate 54 slots regardless of the configured row count so that
 * items stored in higher rows are never lost when the row count is reduced.
 *
 * Uses @ModifyArg (static) to safely intercept the super() constructor argument.
 * @ModifyConstant on a constructor causes a VerifyError; @ModifyArg before super()
 * must be declared static to avoid an InvalidInjectionException.
 */
@Mixin(PlayerEnderChestContainer.class)
public class PlayerEnderChestContainerMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/SimpleContainer;<init>(I)V"
            ),
            index = 0
    )
    private static int xxlenderchest$expandContainerSize(int original) {
        return 54;
    }
}
