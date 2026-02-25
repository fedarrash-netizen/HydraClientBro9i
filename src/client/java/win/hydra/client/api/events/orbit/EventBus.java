package win.hydra.client.api.events.orbit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Very small reflection based event bus.
 */
public class EventBus {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Map<Class<?>, List<Listener>> listeners = new IdentityHashMap<>();

    public void register(Object target) {
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> eventType = method.getParameterTypes()[0];
            method.setAccessible(true);
            try {
                MethodHandle handle = LOOKUP.unreflect(method).bindTo(target);
                listeners.computeIfAbsent(eventType, c -> new ArrayList<>())
                        .add(new Listener(handle));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public void post(Object event) {
        if (event == null) return;
        List<Listener> list = listeners.get(event.getClass());
        if (list == null) return;
        for (Listener listener : list) {
            try {
                listener.handle.invoke(event);
            } catch (Throwable ignored) {
            }
        }
    }

    private record Listener(MethodHandle handle) {
    }
}


