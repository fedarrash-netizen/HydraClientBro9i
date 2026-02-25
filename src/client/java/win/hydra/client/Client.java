package win.hydra.client;

import win.hydra.client.api.events.orbit.EventBus;
import win.hydra.client.event.Render2DEvent;
import win.hydra.client.hud.IRenderer;
import win.hydra.client.screen.hud.impl.WatermarkRenderer;

/**
 * Simple central client container similar to the original Client.inst().
 */
public class Client {

    private static final Client INSTANCE = new Client();

    private final EventBus eventBus = new EventBus();

    private final WatermarkRenderer watermarkRenderer = new WatermarkRenderer();

    private final ServerTps serverTps = new ServerTps();

    public static Client inst() {
        return INSTANCE;
    }

    private Client() {
        // register HUD renderers
        eventBus.register(watermarkRenderer);
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public ServerTps serverTps() {
        return serverTps;
    }

    /**
     * Very small TPS provider stub.
     */
    public static class ServerTps {
        private double tps = 20.0D;

        public double getTPS() {
            return tps;
        }

        public void setTPS(double tps) {
            this.tps = tps;
        }
    }
}


