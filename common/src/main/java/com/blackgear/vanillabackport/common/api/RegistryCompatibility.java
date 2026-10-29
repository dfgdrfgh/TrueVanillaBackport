package com.blackgear.vanillabackport.common.api;

import com.blackgear.platform.core.BuiltInCoreRegistry;
import com.blackgear.platform.core.api.RegistryKey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Use the installed Platform mod's code-driven registry API.
 *
 * Platform 1.4.2 exposes registerKey(String, Function); the 1.21.1
 * development API exposes register(String, Function) instead.
 * Resolve the public method once so both external Platform versions work
 * without embedding or modifying any Platform classes in VanillaBackport.
 */
public final class RegistryCompatibility {
    private static final Method REGISTER_KEY_METHOD = findRegisterKeyMethod();

    private RegistryCompatibility() {
    }

    private static Method findRegisterKeyMethod() {
        for (String method : new String[] { "registerKey", "register" }) {
            try {
                return BuiltInCoreRegistry.class.getMethod(method, String.class, Function.class);
            } catch (NoSuchMethodException ignored) {
                // Different releases use different names for this operation.
            }
        }
        throw new IllegalStateException("Installed Platform does not expose code-driven registry registration");
    }

    public static <T> RegistryKey<T> register(BuiltInCoreRegistry<T> registry, String name, T value) {
        return register(registry, name, ignored -> value);
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> RegistryKey<T> register(
        BuiltInCoreRegistry<T> registry, String name, Function<RegistryKey<T>, E> constructor
    ) {
        try {
            return (RegistryKey<T>) REGISTER_KEY_METHOD.invoke(registry, name, constructor);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot access Platform registry registration", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Platform registry registration failed for " + name, cause);
        }
    }
}
