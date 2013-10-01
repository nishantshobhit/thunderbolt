package to.talk.thunderbolt;

import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class StreamingParser {

    private XMLInputFactory xmlif;

    public StreamingParser() {
        xmlif = XMLInputFactory.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(InputStream is, Class<T> clazz) throws HailStormException {
        String packageName = clazz.getPackage().getName();
        try {
            XMLEventReader r;
            r = xmlif.createXMLEventReader(is);
            Stack<Object> stack = new Stack<>();
            Object parsedObject = null;
            while (r.hasNext()) {
                XMLEvent e = r.nextEvent();
                if (e.isStartElement()) {
                    // the top level element corresponds to the supplied class
                    if (stack.isEmpty()) {
                        Object obj = instantiate(clazz.getName());
                        setAttributes(obj, e);
                        stack.push(obj);
                    } else {
                        String property = camelCase(e.asStartElement().getName().getLocalPart(), false);
                        Method getter = MethodUtils.propertyGetter(stack.peek(), property);
                        if (getter == null) continue; // ignore elements which do not have getters/setters

                        Object value = null, instance = stack.peek();
                        if (isSimpleType(getter.getReturnType())) {
                            value = toSimpleType(getCharacters(r), getter.getReturnType());
                        } else {
                            String content = null;
                            if (r.peek().isCharacters()) {
                                content = getCharacters(r);
                            }
                            // create the object and set attributes
                            value = instantiate(packageName + '.' + StringUtils.capitalize(property), content);
                            setAttributes(value, e);
                            stack.push(value);
                        }
                        // link to the parent
                        setProperty(instance, property, value);
                    }
                } else if (e.isEndElement()) {
                    String className = camelCase(e.asEndElement().getName().getLocalPart(), true);
                    if (stack.peek().getClass().getSimpleName().equals(className)) {
                        parsedObject = stack.pop();
                    }
                }
            }
            return (T) parsedObject;
        } catch (XMLStreamException ex) {
            throw new HailStormException(ex);
        }
    }

    private String getCharacters(XMLEventReader rdr) throws XMLStreamException {
        XMLEvent e = rdr.nextEvent();
        if (e.isCharacters()) {
            return e.asCharacters().getData(); 
        } else {
            return null;
        }
    }

    // example : create-profile-full -> createProfileFull if capitalize is false
    private String camelCase(String str, boolean capitalize) {
        String[] parts = str.split("-");
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0]);
        for (int i=1; i< parts.length; i++) {
            sb.append(StringUtils.capitalize(parts[i]));
        }
        return (capitalize) ? StringUtils.capitalize(sb.toString()) : sb.toString();
    }

    private Object instantiate(String className, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            if (args == null || args.length == 0) {
                // no args, invoke the default cons
                return clazz.newInstance();
            } else {
                Class<?>[] argTypes = new Class<?>[args.length];
                int i = 0;
                for (Object obj : args) {
                    argTypes[i++] = obj.getClass();
                }
                try {
                    Constructor<?> cons = clazz.getDeclaredConstructor(argTypes);
                    return cons.newInstance(args);
                } catch (NoSuchMethodException exception) {
                    return clazz.newInstance();
                }
            }
        } catch (Exception cause) {
            throw new HailStormException(cause);
        }
    }

    @SuppressWarnings("unchecked")
    private void setAttributes(Object instance, XMLEvent e) {
        StartElement startEl = e.asStartElement();
        Iterator<Attribute> attrIterator = startEl.getAttributes();
        while (attrIterator.hasNext()) {
            Attribute attr = attrIterator.next();
            String property = camelCase(attr.getName().getLocalPart(), false);            
            Method getter = MethodUtils.propertyGetter(instance, property);
            if (getter != null) {
                Object value = toSimpleType(attr.getValue(), getter.getReturnType());
                setProperty(instance, property, value);
            }
        }
    }

    // invokes instance.setPropertyName(arg)
    private void setProperty(Object instance, String propertyName, Object arg) {
        Method setterMethod = MethodUtils.propertySetter(instance, propertyName);
        Method getterMethod = MethodUtils.propertyGetter(instance, propertyName);
        if (setterMethod != null && getterMethod != null) {
            try {
                setterMethod.invoke(instance, arg);
            } catch (Exception cause) {
                throw new HailStormException(cause);
            }
        }
    }

    private interface Cast {
        Object doCast(String arg);
    }

    private static Map<String, Cast> primitivesMap = new HashMap<>();
    static {
        primitivesMap.put("int", new Cast() {
            @Override
            public Integer doCast(String arg) {
                return Integer.valueOf(arg);
            }
        });
        primitivesMap.put("long", new Cast() {
            @Override
            public Object doCast(String arg) {
                return Long.valueOf(arg);
            }
        });
        primitivesMap.put("short", new Cast() {
            @Override
            public Object doCast(String arg) {
                return Short.valueOf(arg);
            }
        });
        primitivesMap.put("float", new Cast() {
            @Override
            public Object doCast(String arg) {
                return Float.valueOf(arg);
            }
        });
        primitivesMap.put("double", new Cast() {
            @Override
            public Object doCast(String arg) {
                return Double.valueOf(arg);
            }
        });
        primitivesMap.put("boolean", new Cast() {
            @Override
            public Object doCast(String arg) {
                return (arg == null) ? Boolean.TRUE : Boolean.valueOf(arg);
            }
        });
        primitivesMap.put("char", new Cast() {
            @Override
            public Object doCast(String arg) {
                return arg.charAt(0);
            }
        });
    }

    // java primitives, strings and enums
    private boolean isSimpleType(Class<?> type) {
        return primitivesMap.containsKey(type.getName()) || type.isAssignableFrom(String.class)
                || type.isEnum();
    }


    @SuppressWarnings("unchecked")
    private Object toSimpleType(String arg, Class<?> type) {
        if (!isSimpleType(type)) {
            throw new HailStormException("not a simple type " + type);
        }
        // fix enums first
        if (type.isEnum()) {
            return Enum.valueOf(type.asSubclass(Enum.class), arg);
        }
        // strings and java primitives
        Cast casted = primitivesMap.get(type.getName());
        return (casted == null) ? arg : casted.doCast(arg);
    }
}