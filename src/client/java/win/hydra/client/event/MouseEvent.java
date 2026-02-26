package win.hydra.client.event;

/**
 * Событие клика мыши.
 */
public class MouseEvent {

    private final int button;
    private final boolean pressed;
    private final int mouseX;
    private final int mouseY;

    public MouseEvent(int button, boolean pressed, int mouseX, int mouseY) {
        this.button = button;
        this.pressed = pressed;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public int getButton() {
        return button;
    }

    public boolean isPressed() {
        return pressed;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
}
