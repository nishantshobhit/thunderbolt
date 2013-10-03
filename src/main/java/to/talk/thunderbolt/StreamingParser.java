package to.talk.thunderbolt;

import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
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
        try {
            XMLEventReader r = xmlif.createXMLEventReader(is);
            Stack<Object> stack = new Stack<>();
            Object parsedObject = null;
            while (r.hasNext()) {
                XMLEvent e = r.nextEvent();
                if (e.isStartElement()) {
                    // the top level element corresponds to the supplied class
                    if (stack.isEmpty()) {
                        T obj = (T) ReflectionUtils.newInstance(clazz);
                        setAttributes(obj, e);
                        stack.push(obj);
                    } else {
                        
                        String property = camelCase(e.asStartElement().getName().getLocalPart(), false);
                        Mutator mutator = ReflectionUtils.mutator(stack.peek().getClass(), property);
                        
                        // ignore elements which do not have getters/setters
                        if (mutator == null) continue;

                        Object value = null, instance = stack.peek();
                        if (isSimpleType(mutator.getType())) {
                            value = toSimpleType(getCharacters(r), mutator.getType());
                        } else {
                            value = ReflectionUtils.newInstance(mutator.getType());
                            if (r.peek().isCharacters()) {
                                Mutator m = ReflectionUtils.mutator(value.getClass(), "value");
                                if (m != null) {
                                    m.invoke(value, getCharacters(r));
                                }
                            }
                            // create the object and set attributes
                            setAttributes(value, e);
                            stack.push(value);
                        }
                        // link to the parent
                        mutator.invoke(instance, value);
                    }
                } else if (e.isEndElement()) {
                    String className = camelCase(e.asEndElement().getName().getLocalPart(), true);
                    Class<?> topOfStack = stack.peek().getClass();
                    if (className.equals(topOfStack.getSimpleName())) { 
                        parsedObject = stack.pop();
                    }
                }
            }
            return (stack.isEmpty()) ? (T) parsedObject : (T) stack.pop();
        } catch (XMLStreamException ex) {
            throw new HailStormException(ex);
        }
    }

    private String getCharacters(XMLEventReader rdr) throws XMLStreamException {
        String str = null;
        XMLEvent e = rdr.nextEvent();
        if (e.isCharacters()) {
            str = e.asCharacters().getData(); 
        }
        return str;
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
    
    @SuppressWarnings("unchecked")
    private void setAttributes(Object instance, XMLEvent e) {
        StartElement startEl = e.asStartElement();
        Iterator<Attribute> attrIterator = startEl.getAttributes();
        while (attrIterator.hasNext()) {
            Attribute attr = attrIterator.next();
            String property = camelCase(attr.getName().getLocalPart(), false);
            Mutator mutator = ReflectionUtils.mutator(instance.getClass(), property);
            if (mutator != null) {
                Object value = toSimpleType(attr.getValue(), mutator.getType());
                mutator.invoke(instance, value);
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