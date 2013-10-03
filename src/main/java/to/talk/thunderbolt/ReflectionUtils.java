package to.talk.thunderbolt;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.MethodAccess;

public class ReflectionUtils {
    
    private static Map<Class<?>, ConstructorAccess<?>> caCache = new HashMap<>();
    private static Map<Class<?>, MethodAccess> maCache = new HashMap<>();
    
    // WARNING: this cache is capable of producing a memory leak
    private static Map<String, Mutator> mutatorCache = new HashMap<>();
    
    public static Object newInstance(Class<?> clazz) {
        if (!caCache.containsKey(clazz)) {
            ConstructorAccess<?> consAccess = ConstructorAccess.get(clazz);
            caCache.put(clazz, consAccess);
        }
        return caCache.get(clazz).newInstance();
    }
    
    public static Mutator mutator(Class<?> clazz, String property) {
        String key = clazz.getName() + ":" + property;
        if (!mutatorCache.containsKey(key)) {
            MethodAccess mAccess = getMethodAccess(clazz);
            String capitalizedProperty = StringUtils.capitalize(property);
            int index = -100;
            try {
                index = mAccess.getIndex("set" + capitalizedProperty);
            } catch (IllegalArgumentException cause) {
                // no setter found
            }
            mutatorCache.put(key, (index == -100) ? null : new Mutator(mAccess, index));
        }
        return mutatorCache.get(key);
    }
    
    private static MethodAccess getMethodAccess(Class<?> clazz) {
        if (!maCache.containsKey(clazz)) {
            MethodAccess mAccess = MethodAccess.get(clazz);
            maCache.put(clazz, mAccess);
        }
        return maCache.get(clazz);
    }
}
