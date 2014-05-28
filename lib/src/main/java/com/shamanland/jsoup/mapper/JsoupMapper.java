package com.shamanland.jsoup.mapper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;

public class JsoupMapper {
    private static final JsoupMapper sInstance;
    private static final HashMap<Class, Object> sWrappers;

    static {
        sInstance = new JsoupMapper();
        sWrappers = new HashMap<Class, Object>();
        sWrappers.put(boolean.class, false);
        sWrappers.put(byte.class, (byte) 0);
        sWrappers.put(char.class, (char) 0);
        sWrappers.put(short.class, (short) 0);
        sWrappers.put(int.class, 0);
        sWrappers.put(long.class, 0L);
        sWrappers.put(float.class, 0F);
        sWrappers.put(double.class, 0D);
        sWrappers.put(void.class, null);
    }

    public static JsoupMapper getInstance() {
        return sInstance;
    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(Class<T> clazz) throws JsoupMapperException {
        try {
            if (clazz.isPrimitive()) {
                return (T) sWrappers.get(clazz);
            }

            return clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new JsoupMapperException(ex);
        } catch (IllegalAccessException ex) {
            throw new JsoupMapperException(ex);
        }
    }

    private static Object invokeMethod(Method method, Object receiver, Object... args) throws JsoupMapperException {
        try {
            return method.invoke(receiver, args);
        } catch (IllegalAccessException ex) {
            throw new JsoupMapperException(ex);
        } catch (InvocationTargetException ex) {
            throw new JsoupMapperException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T fromString(Class<T> clazz, String value) throws JsoupMapperException {
        try {
            Constructor<T> ctor = clazz.getConstructor(String.class);
            return ctor.newInstance(value);
        } catch (NoSuchMethodException ex) {
            // fall down
        } catch (InvocationTargetException ex) {
            throw new JsoupMapperException(ex);
        } catch (InstantiationException ex) {
            throw new JsoupMapperException(ex);
        } catch (IllegalAccessException ex) {
            throw new JsoupMapperException(ex);
        }

        try {
            Method m = clazz.getMethod("valueOf", String.class);
            return (T) invokeMethod(m, null, value);
        } catch (NoSuchMethodException ex) {
            // fall down
        }

        return newInstance(clazz);
    }

    public <T> T readValue(Element element, Class<T> clazz) throws JsoupMapperException {
        return readValue(element, clazz, null);
    }

    @SuppressWarnings("unchecked")
    private <T> T readValue(Element element, Class<T> clazz, Method setter) throws JsoupMapperException {
        final Class<?> itemClazz;
        final boolean isArray = clazz.isArray();
        final boolean isCollection = Collection.class.isAssignableFrom(clazz);

        if (isArray) {
            itemClazz = clazz.getComponentType();
        } else if (isCollection) {
            TypeVariable<Class<T>>[] params = clazz.getTypeParameters();
            if (params.length != 1) {
                throw new JsoupMapperException(clazz.getName());
            }

            itemClazz = params[0].getGenericDeclaration();
        } else {
            itemClazz = clazz;
        }

        Elements rootElements = null;

        if (itemClazz.isAnnotationPresent(JsoupSelector.class)) {
            String selector = itemClazz.getAnnotation(JsoupSelector.class).value();
            rootElements = element.select(selector);
        }

        if (isCollection) {
            if (clazz.isAnnotationPresent(JsoupSelector.class)) {
                String selector = clazz.getAnnotation(JsoupSelector.class).value();
                rootElements = element.select(selector);
            }
        }

        if (setter != null && setter.isAnnotationPresent(JsoupSelector.class)) {
            String selector = setter.getAnnotation(JsoupSelector.class).value();
            rootElements = element.select(selector);
        }

        if (rootElements == null) {
            rootElements = new Elements(element);
        }

        final T result;

        if (isArray) {
            int count = rootElements.size();
            result = (T) Array.newInstance(itemClazz, count);
            Object[] array = (Object[]) result;

            for (int i = 0; i < count; ++i) {
                array[i] = parseElement(rootElements.get(i), itemClazz);
            }
        } else if (isCollection) {
            result = newInstance(clazz);
            Collection collection = (Collection) result;

            for (Element e : rootElements) {
                collection.add(parseElement(e, itemClazz));
            }
        } else if (rootElements.size() > 0) {
            Element single = rootElements.first();
            String value = null;

            if (clazz.isAnnotationPresent(JsoupAttributeValue.class)) {
                value = single.attr(clazz.getAnnotation(JsoupAttributeValue.class).value());
            } else if (clazz.isAnnotationPresent(JsoupHtmlValue.class)) {
                value = single.html();
            } else if (clazz.isAnnotationPresent(JsoupTextValue.class)) {
                value = single.text();
            }

            if (setter != null) {
                if (setter.isAnnotationPresent(JsoupAttributeValue.class)) {
                    value = single.attr(setter.getAnnotation(JsoupAttributeValue.class).value());
                } else if (setter.isAnnotationPresent(JsoupHtmlValue.class)) {
                    value = single.html();
                } else if (setter.isAnnotationPresent(JsoupTextValue.class)) {
                    value = single.text();
                }
            }

            if (value == null) {
                result = parseElement(single, clazz);
            } else {
                result = fromString(clazz, value);
            }
        } else {
            result = null;
        }

        return result;
    }

    private <T> T parseElement(Element element, Class<T> clazz) throws JsoupMapperException {
        T result = newInstance(clazz);

        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith("set")) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 1) {
                    Object param = readValue(element, params[0], m);
                    invokeMethod(m, result, param);
                }
            }
        }

        return result;
    }
}
