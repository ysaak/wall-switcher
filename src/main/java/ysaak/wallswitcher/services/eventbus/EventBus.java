package ysaak.wallswitcher.services.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ysaak.wallswitcher.App;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple event bus
 */
public class EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static Map<Class<?>, List<Subscriber>> subscribersByEventType = new HashMap<>();
    private static Map<Object, List<Subscriber>> subscribersByListener = new HashMap<>();

    private static final Lock LOCK = new ReentrantLock();
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    public static void register(Object object) {
        LOCK.lock();

        try {
            if (!subscribersByListener.containsKey(object)) {
                List<Subscriber> subscribers = new ArrayList<>();


                // get methods
                Method[] methods = object.getClass().getDeclaredMethods();

                for (Method method : methods) {
                    if (method.isAnnotationPresent(Subscribe.class) && !method.isSynthetic()) {
                        if (method.getParameterCount() == 1) {
                            Class<?> eventType = method.getParameterTypes()[0];

                            Subscriber s = new Subscriber(object, method, eventType);
                            subscribers.add(s);

                            if (!subscribersByEventType.containsKey(eventType)) {
                                subscribersByEventType.put(eventType, new ArrayList<>());
                            }

                            subscribersByEventType.get(eventType).add(s);
                        } else {
                            LOGGER.error("Method {} has @Subscribe annotation but has {} parameters. Subscriber methods must have exactly one parameter", method.getName(), method.getParameterCount());
                        }
                    }
                }

                if (!subscribers.isEmpty()) {
                    subscribersByListener.put(object, subscribers);
                }
            }
        }
        finally {
            LOCK.unlock();
        }
    }

    public static void unregister(Object object) {
        LOCK.lock();

        try {
            List<Subscriber> subscribers = subscribersByListener.get(object);

            if (subscribers != null) {

                for (Class<?> eventType : subscribersByEventType.keySet()) {
                    subscribersByEventType.get(eventType).removeAll(subscribers);
                }
            }
        }
        finally {
            LOCK.unlock();
        }
    }

    public static void post(final Object event) {
        EXECUTOR.execute(() -> doPost(event));
    }

    private static void doPost(final Object event) {
        LOCK.lock();

        try {
            List<Subscriber> subscribers = subscribersByEventType.get(event.getClass());
            if (subscribers != null && !subscribers.isEmpty()) {

                subscribers.forEach(s -> s.call(event));
            }
        }
        finally {
            LOCK.unlock();
        }
    }

    private static class Subscriber {
        Object object;
        Method method;
        Class<?> eventType;

        Subscriber(Object object, Method method, Class<?> eventType) {
            this.object = object;
            this.method = method;
            this.eventType = eventType;
        }

        void call(Object event) {

            try {
                method.setAccessible(true);
                method.invoke(object, event);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }
}
