package to.talk.thunderbolt;

import org.apache.commons.lang.StringUtils;

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

    private static Map<String, BeanMethods> methodCache = new HashMap<>();
    
    public static Method propertyGetter(Object instance, String property) {
        return find(instance, property).get;
    }
    
    public static Method propertySetter(Object instance, String property) {
        return find(instance, property).set;
    }
    
    private static BeanMethods find(Object instance, String name) {
        String key = instance.getClass() + ":" + name;
        if (!methodCache.containsKey(key)) {
            Method getter = null, setter = null;
            String upperCasedProperty = StringUtils.capitalize(name);
            String setterName = "set" + upperCasedProperty, getterName = "get" + upperCasedProperty,
                    isGetterName = "is" + upperCasedProperty;
            for (Method m : instance.getClass().getMethods()) {
                if (m.getName().equals(getterName)) {
                    getter = m;
                } else if (m.getName().equals(isGetterName)) {
                    getter = m;
                } else if (m.getName().equals(setterName)) {
                    setter = m;
                }
            }
            methodCache.put(key, new BeanMethods(getter, setter));
        }
        return methodCache.get(key);
    }
}