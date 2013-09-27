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
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
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
            while (r.hasNext()) {
                XMLEvent e = r.nextEvent();
                if (e.isStartElement()) {
                    // the top level element corresponds to the supplied class
                    if (stack.isEmpty()) {
                        Object obj = getObject(clazz.getName());
                        setAttributes(obj, e);
                        stack.push(obj);
                    } else {
                        String property = camelCase(e.asStartElement().getName().getLocalPart(), false);
                        Method getter = MethodUtils.propertyGetter(stack.peek(), property);
                        if (getter == null) continue; // ignore elements which do not have getters/setters

                        if (isSimpleType(getter.getReturnType())) {
                            setProperty(stack.peek(), property, getCharacters(r));
                        } else {
                            String content = null;
                            if (r.peek().isCharacters()) {
                                content = getCharacters(r);
                            }
                            String className = StringUtils.capitalize(property);
                            Object obj = getObject(packageName + '.' + className, content);
                            setAttributes(obj, e);
                            setProperty(stack.peek(), property, obj);
                            stack.push(obj);
                        }
                    }
                } else if (e.isEndElement()) {
                    String className = camelCase(e.asEndElement().getName().getLocalPart(), true);
                    if (stack.peek().getClass().getSimpleName().equals(className)) {
                        stack.pop();
                    }
                }
            }
            return (T) stack.pop();
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

    private String camelCase(String str, boolean capitalize) {
        String[] parts = str.split("-");
        String ret = (capitalize) ? StringUtils.capitalize(parts[0]) : parts[0];
        return (parts.length > 1) ? ret + StringUtils.capitalize(parts[1]) : ret;
    }

    private Object getObject(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.newInstance();
        } catch (Exception e) {
            throw new HailStormException(e);
        }
    }

    private Object getObject(String className, String content) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> cons = clazz.getDeclaredConstructor(String.class);
            if (cons != null) {
                return cons.newInstance(content);
            } else {
                return clazz.newInstance();
            }
        } catch (Exception e) {
            throw new HailStormException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void setAttributes(Object obj, XMLEvent e) {
        StartElement startEl = e.asStartElement();
        Iterator<Attribute> attrIterator = startEl.getAttributes();
        while (attrIterator.hasNext()) {
            Attribute attr = attrIterator.next();
            setProperty(obj, camelCase(attr.getName().getLocalPart(), false), attr.getValue());
        }
    }

    private void setProperty(Object instance, String propertyName, Object arg) {
        Method setterMethod = MethodUtils.propertySetter(instance, propertyName);
        Method getterMethod = MethodUtils.propertyGetter(instance, propertyName);
        if (setterMethod != null && getterMethod != null) {
            try {
                if (arg != null) {
                    if (arg instanceof String) {
                        setterMethod.invoke(instance, toSimpleType((String)arg, getterMethod.getReturnType()));
                    } else {
                        setterMethod.invoke(instance, arg);
                    }
                } else {
                    // exception for boolean where the presence of an element without content means true
                    if (getterMethod.getReturnType().getName().equals("boolean")) {
                        setterMethod.invoke(instance, Boolean.TRUE);
                    }
                }
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
                return Boolean.valueOf(arg);
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
    private boolean isSimpleType(Class<?> returnType) {
        return primitivesMap.containsKey(returnType.getName()) || returnType.isAssignableFrom(String.class)
                || returnType.isEnum();
    }


    @SuppressWarnings("unchecked")
    private Object toSimpleType(String arg, Class<?> returnType) {
        // fix enums first
        if (returnType.isEnum()) {
            return Enum.valueOf(returnType.asSubclass(Enum.class), arg);
        }
        Cast casted = primitivesMap.get(returnType.getName());
        return (casted == null) ? arg : casted.doCast(arg);
    }
}