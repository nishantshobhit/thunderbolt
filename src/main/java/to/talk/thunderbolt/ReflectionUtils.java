package to.talk.thunderbolt;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.MethodAccess;

public class ReflectionUtils {
    
    private static Map<String, ConstructorAccess<?>> caCache = new HashMap<>();
    private static Map<Class<?>, MethodAccess> maCache = new HashMap<>();
    private static Map<String, Mutator> mutatorCache = new HashMap<>();
    
    public static Object newInstance(String className) {
        ConstructorAccess<?> consAccess = getConsAccess(className);
        return consAccess.newInstance();
    }
    
    public static Mutator mutator(Class<?> clazz, String property) {
        if (!mutatorCache.containsKey(getKey(clazz, property))) {
            MethodAccess mAccess = getMethodAccess(clazz);
            String capitalizedProperty = StringUtils.capitalize(property);
            int index = -100;
            try {
                index = mAccess.getIndex("set" + capitalizedProperty);
            } catch (IllegalArgumentException cause) {
                try {
                    index = mAccess.getIndex("is" + capitalizedProperty);
                } catch (IllegalArgumentException innerCause) {
                    // no setter found
                }
            }
            mutatorCache.put(getKey(clazz, property), (index == -100) ? null : new Mutator(mAccess, index));
        }
        return mutatorCache.get(getKey(clazz, property));
    }
    
    private static String getKey(Class<?> clazz, String property) {
        return clazz.getName() + ":" + property;
    }
    
    private static ConstructorAccess<?> getConsAccess(String className) {
        if (!caCache.containsKey(className)) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException cause) {
                throw new HailStormException(cause);
            }
            ConstructorAccess<?> consAccess = ConstructorAccess.get(clazz);
            caCache.put(className, consAccess);
        }
        return caCache.get(className);
    }
    
    private static MethodAccess getMethodAccess(Class<?> clazz) {
        if (!maCache.containsKey(clazz)) {
            MethodAccess mAccess = MethodAccess.get(clazz);
            maCache.put(clazz, mAccess);
        }
        return maCache.get(clazz);
    }
}
