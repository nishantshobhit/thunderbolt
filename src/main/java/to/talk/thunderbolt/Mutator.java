package to.talk.thunderbolt;

import com.esotericsoftware.reflectasm.MethodAccess;

public class Mutator {
    
    private MethodAccess mAccess;
    private int index;
    
    public Mutator(MethodAccess mAccess, int index) {
        this.mAccess = mAccess;
        this.index = index;
    }
    
    public Class<?> getType() {
        Class<?>[][] params = mAccess.getParameterTypes();
        if (params != null && params.length > 0) {
            Class<?>[] param = params[index];
            if (param != null && param.length > 0) {
                return param[0];
            }
        }
        throw new HailStormException("bad mutator for property" + mAccess.toString());
    }
    
    public void invoke(Object instance, Object arg) {
        mAccess.invoke(instance, index, arg);
    }
}