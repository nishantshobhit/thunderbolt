package to.talk.thunderbolt;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class Parser {

    private XMLInputFactory xmlif;

    public Parser() {
        xmlif = XMLInputFactory.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(InputStream is, Class<T> clazz) throws HailStormException {
        String packageName = clazz.getPackage().getName();
        try {
            XMLEventReader r = null;
            r = xmlif.createXMLEventReader(is);
            Stack<Object> stack = new Stack<Object>();
            while (r.hasNext()) {
                XMLEvent e = r.nextEvent();
                if (e.isStartElement()) {
                    if (stack.isEmpty()) {
                        stack.push(getObject(clazz.getName()));
                        continue;
                    }
                    XMLEvent nextEvent = r.peek();
                    Object instance = stack.peek();
                    if (nextEvent.isCharacters() || nextEvent.isEndElement()) {
                        // simple type
                        String content = (nextEvent.isCharacters()) ? getCharacters(r) : null;
                        setProperty(instance, "set" + camelCase(getStartTagName(e), true), content);
                    } else {
                        // complex type
                        String classname = camelCase(getStartTagName(e), true);
                        Object obj = getObject(packageName + "." + classname);
                        setProperty(instance, "set" + classname, obj);
                        stack.push(obj);
                    }
                } else if (e.isEndElement()) {
                    String classname = camelCase(getEndTagName(e), true);
                    Object o = stack.peek();
                    if (o.getClass().getSimpleName().equals(classname)) {
                        stack.pop();
                    }
                }
            }
            return (T) stack.pop();
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
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

    private String getEndTagName(XMLEvent event) {
        return event.asEndElement().getName().getLocalPart();
    }

    private String getStartTagName(XMLEvent event) {
        return event.asStartElement().getName().getLocalPart();
    }

    private String camelCase(String str, boolean capitalize) {
        String[] parts = str.split("-");
        String ret = (capitalize) ? (char)(parts[0].charAt(0) - 32) + parts[0].substring(1) : parts[0]; 
        return (parts.length > 1) ? ret + (char)(parts[1].charAt(0) - 32) + parts[1].substring(1) : ret;
    }

    private Object getObject(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.newInstance();
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setProperty(Object instance, String setterName, Object arg) {
        Method setterMethod = MethodUtils.propertySetter(instance, setterName);
        Method getterMethod = MethodUtils.propertyGetter(instance, setterName);
        try {
            if (arg instanceof String) {
                setterMethod.invoke(instance, cast((String)arg, getterMethod.getReturnType()));
            } else {
                setterMethod.invoke(instance, arg);
            }
        } catch (Exception cause) {
            throw new HailStormException(cause);
        }
    }

    private interface Cast {
        Object doCast(String arg);
    }

    private static Map<String, Cast> castMap = new HashMap<String, Cast>();
    static {
        castMap.put("int", new Cast() {
            @Override
            public Integer doCast(String arg) {
                return Integer.valueOf(arg);
            }
        });
        castMap.put("long", new Cast() {
            @Override
            public Object doCast(String arg) {
                return Long.valueOf(arg);
            }
        });
        castMap.put("float", new Cast() {
            @Override
            public Object doCast(String arg) {
                return Float.valueOf(arg);
            }
        });
        castMap.put("double", new Cast() {
            @Override
            public Object doCast(String arg) {
                return Double.valueOf(arg);
            }
        });
        castMap.put("boolean", new Cast() {
            @Override
            public Object doCast(String arg) {
                return (arg == null) ? Boolean.TRUE : Boolean.valueOf(arg);
            }
        });
        castMap.put("char", new Cast() {
            @Override
            public Object doCast(String arg) {
                return Character.valueOf(arg.charAt(0));
            }
        });
    }

    private Object cast(String arg, Class<?> returnType) {
        if (arg == null && !returnType.getName().equals("boolean")) {
            return arg;
        }
        Cast casted = castMap.get(returnType.getName());
        return (casted == null) ? arg : casted.doCast(arg);
    }
}