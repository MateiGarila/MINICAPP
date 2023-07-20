package app.uvtracker.sensor.pii.event;

import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EventRegistry implements IEventSource {

    @NonNull
    private final List<EventAcceptor> storage;

    public EventRegistry() {
        this.storage = new ArrayList<>(20);
    }

    @Override
    public void registerListener(IEventListener listener) {
        this.storage.addAll(
                Arrays.stream(listener.getClass().getDeclaredMethods())
                        .map(method -> EventAcceptor.getHandlerMethod(listener, method))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void unregisterAll() {
        this.storage.clear();
    }

    public void dispatch(Object eventObject) {
        this.storage.forEach(m -> m.tryCall(eventObject));
    }

}


class EventAcceptor {

    @NonNull
    private static final String TAG = EventAcceptor.class.getSimpleName();

    @NonNull
    private final IEventListener listener;

    @NonNull
    private final Method method;

    @NonNull
    private final Class<?> inputType;

    @NonNull
    private final EventHandler annotation;

    public static EventAcceptor getHandlerMethod(@NonNull IEventListener listener, @NonNull Method method) {
        EventHandler annotation = method.getAnnotation(EventHandler.class);
        if(annotation == null) return null;
        if(method.getParameterCount() != 1) {
            Log.w(TAG, "In class " + listener.getClass().getSimpleName() + ", method " + method.toGenericString() + " is annotated as event handler but does not declare exactly 1 argument.");
            return null;
        }
        if(!method.getReturnType().toString().equals("void")) {
            Log.i(TAG, "In class " + listener.getClass().getSimpleName() + ", method " + method.toGenericString() + " is annotated as event handler but has a return value. The return value will be discarded.");
        }
        if(!method.isAccessible()) {
            try {
                method.setAccessible(true);
            }
            catch(SecurityException ex) {
                Log.w(TAG, "In class " + listener.getClass().getSimpleName() + ", method " + method.toGenericString() + " count not be set public.");
                ex.printStackTrace();
                return null;
            }
        }
        return new EventAcceptor(listener, method, method.getParameterTypes()[0], annotation);
    }

    public EventAcceptor(@NonNull IEventListener listener, @NonNull Method method, @NonNull Class<?> inputType, @NonNull EventHandler annotation) {
        this.listener = listener;
        this.method = method;
        this.inputType = inputType;
        this.annotation = annotation;
    }

    public boolean tryCall(Object eventObject) {
        if(!this.inputType.isAssignableFrom(eventObject.getClass())) return false;
        try {
            this.method.invoke(this.listener, eventObject);
        } catch (IllegalAccessException e) {
            // TODO: exception handling 1
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // TODO: exception handling 2
            throw new RuntimeException(e);
        }
        return true;
    }

}
