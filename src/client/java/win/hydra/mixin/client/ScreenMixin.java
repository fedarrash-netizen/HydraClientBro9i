package win.hydra.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.hydra.client.Client;
import win.hydra.client.event.MouseEvent;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            Client.inst().eventBus().post(new MouseEvent(
                button,
                true,
                (int) mouseX,
                (int) mouseY
            ));
        }
    }

    @Inject(at = @At("HEAD"), method = "mouseReleased")
    private void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            Client.inst().eventBus().post(new MouseEvent(
                button,
                false,
                (int) mouseX,
                (int) mouseY
            ));
        }
    }
}
