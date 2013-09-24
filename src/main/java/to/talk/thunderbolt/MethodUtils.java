package to.talk.thunderbolt;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodUtils {

    private static class BeanMethods {
        Method get;
        Method set;
        private BeanMethods(Method getter, Method setter) {
            this.get = getter;
            this.set = setter;
        }
    }

    private static Map<String, BeanMethods> methodCache = new HashMap<String, BeanMethods>();
    
    public static Method propertyGetter(Object instance, String name) {
        return find(instance, name).get;
    }
    
    public static Method propertySetter(Object instance, String name) {
        return find(instance, name).set;
    }
    
    private static BeanMethods find(Object instance, String name) {
        String key = instance.getClass() + ":" + name;
        if (!methodCache.containsKey(key)) {
            Method getter = null, setter = null;
            for (Method m : instance.getClass().getMethods()) {
                if (m.getName().equals(name)) {
                    setter = m;
                    getter = findGetter(instance, name);
                    break;
                }
            }
            methodCache.put(key, new BeanMethods(getter, setter));
        }
        return methodCache.get(key);
    }
    
    private static Method findGetter(Object instance, String setter) {
        try {
            return instance.getClass().getMethod(setter.replace("set", "get"));
        } catch (NoSuchMethodException cause) {
            try {
                return instance.getClass().getMethod(setter.replace("set", "is"));
            } catch (NoSuchMethodException nestedCause) {
                return null;
            }
        }
    }
}
