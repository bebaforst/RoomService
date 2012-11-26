package info.margreiter.vaadin;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.AlignmentUtils;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Layout.AlignmentHandler;
import com.vaadin.ui.Layout.MarginHandler;
import com.vaadin.ui.Layout.MarginInfo;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;

public class VaadXmlRenderer implements Serializable {

	private static String DEFAULT_NAMESPACE = "com.vaadin.ui";
	private static String NULL_ENCODING = "null";
	HashMap<Component, String> componentIdMap = new HashMap<Component, String>();
	HashMap<String, Component> idComponentMap = new HashMap<String, Component>();
	Component root;

	private void addComponentWithAttributes(ComponentContainer parent,
	        Component child, Map<String, String> attrs) {
		
	    if (parent instanceof TabSheet) {
	        TabSheet.Tab tab = ((TabSheet) parent).addTab(child);
	        if (attrs.containsKey("caption")) {
	            tab.setCaption(attrs.get("caption"));
	        }
	        if (attrs.containsKey("description")) {
	            tab.setCaption(attrs.get("description"));
	        }
	    } else if (parent instanceof AbstractOrderedLayout) {
	        parent.addComponent(child);
	        if (attrs.containsKey("expandRatio")) {
	            ((AbstractOrderedLayout) parent).setExpandRatio(child, Float
	                    .parseFloat(attrs.get("expandRatio")));
	        }
	    } else if (parent instanceof GridLayout) {
	    	GridLayout layout = (GridLayout)parent;
	    	String gridConstants= "1,1";
	    	if (attrs.containsKey("gridConstants")) {
	    		gridConstants = attrs.get("gridConstants");
	    	}
	    	addComponentToGridLayout(layout,child,gridConstants);
	    } else if (parent instanceof AbsoluteLayout) {
	        // TODO Implement AbsoluteLayout support
	        throw new UnsupportedOperationException("AbsoluteLayout");
	    } else if (parent instanceof Accordion) {
	        // TODO Implement Accordionsupport
	        throw new UnsupportedOperationException("Accordion");
	    } else if (parent instanceof CssLayout) {
	        // TODO Implement CssLayout support
	        throw new UnsupportedOperationException("CssLayout");
	    } else {
	        parent.addComponent(child);
	    }
	
	    if (parent instanceof AlignmentHandler
	            && attrs.containsKey("alignment")) {
	        AlignmentUtils.setComponentAlignment((AlignmentHandler) parent,
	                child, attrs.get("alignment"));
	    }
	
	}

	private void addComponentToGridLayout(GridLayout parent, Component child, String gridConstants) {
		// TODO Test 23.12.2011
		StringTokenizer st = new StringTokenizer(gridConstants, ",");
		
		int i = st.countTokens();
		int row1=parent.getRows() + 1;
		int column1=0;
		if ((i==2) || (i==4)) {
			column1=Integer.valueOf(st.nextToken());
			row1=Integer.valueOf(st.nextToken());
		}
		int column2=column1;
		int row2=row1;
		if (i==4) {
			column2=Integer.valueOf(st.nextToken());
			row2=Integer.valueOf(st.nextToken());
		}
		if (row2 < row1) row2=row1;
		if (column2 < column1) column2=column1;
		int maxColoumn = column1 > column2 ? column1:column2;
		int maxRow=row1>row2?row1:row2;
		if (parent.getRows() <= maxRow) parent.setRows(maxRow+1);
		if (parent.getColumns() <= maxColoumn) parent.setColumns(maxColoumn +1);
		parent.addComponent(child,column1,row1,column2,row2);
		
		
		
	}

	private Object decodeValue(String value, Class type) {
	    if (type == String.class) {
	        return NULL_ENCODING.equals(value) ? null : value;
	    } else if (type == java.lang.Boolean.TYPE) {
	        return Boolean.parseBoolean(value);
	    } else if (type == java.lang.Integer.TYPE) {
	        return Integer.parseInt(value);
	    } else if (type == java.lang.Long.TYPE) {
	        return Long.parseLong(value);
	    } else if (type == java.lang.Float.TYPE) {
	        return Float.parseFloat(value);
	    } else if (type == java.lang.Double.TYPE) {
	        return Double.parseDouble(value);
	    } else if (type == java.lang.Character.TYPE) {
	        return value.charAt(0);
	    } else if (type == java.lang.Byte.TYPE) {
	        return Byte.parseByte(value);
	    }
	    throw new UnsupportedOperationException(type.getName()
	            + " is not supported.");
	}

	/**
	 * Get a component in the UI by its assigned id.
	 * 
	 * @param id
	 *            Id assigned to the component with id attribute in XML.
	 */
	public Component getById(String id) {
	    return idComponentMap.get(id);
	}

	/**
	 * Get an id for a component.
	 * 
	 * @return Id assigned to the component in the XML with id attribute or null
	 *         if no id is assigned.
	 */
	public String getId(Component component) {
	    return componentIdMap.get(component);
	}

	/** Sets the root component of the user interface component tree */
	public Component getRoot() {
	    return root;
	}

	private Component instantiateComponent(String className){
		Component result = null;
	    try {
	        Class cc = Panel.class.getClassLoader().loadClass(className);
	        result= (Component) cc.newInstance();
	    } catch (ClassNotFoundException e) {
	    } catch (InstantiationException e) {
	    } catch (IllegalAccessException e) {
	    }
	    return result;
	}

	private boolean isTypeSupported(Class type) {
	    return type == String.class || type.isPrimitive();
	}

	private void readAndCreateComponent(Stack<Component> componentStack,
	        Stack<Map<String, String>> childAttributesStack, Component c, XMLStreamReader reader) throws RenderingException {        
	    Map<String, String> attrs = readCurrentElementAttributes(reader);
	    if (attrs.containsKey("id")) {
	        setId(c, attrs.get("id"));
	        attrs.remove("id");
	    }
	    childAttributesStack.push(attrs);	    	
	
	    componentStack.push(c);
	    if (root == null) {
	        root = c;
	    }
	}

	private void setComponentAttributes(Component c, Map<String, String> attrs)  {
		try {
	        BeanInfo bi = Introspector.getBeanInfo(c.getClass());
	        PropertyDescriptor pds[] = bi.getPropertyDescriptors();
	        for (int i = 0; i < pds.length; i++) {
	            String name = pds[i].getName();
	            if (c instanceof Sizeable
	                    && ("height".equals(name) || "width".equals(name))) {
	                continue;
	            }
	            if (name.trim().equals("alignment")) {
	            	System.out.println("aha:" + c.getClass().getName()) ;
	            }
	            if (attrs.containsKey(name)) {
	                String value = attrs.get(name);
	                Method writeMethod = pds[i].getWriteMethod();
	                Class type = pds[i].getPropertyType();
	                if (isTypeSupported(type)) {
	                    writeMethod.invoke(c, decodeValue(value, type));
	                }
	
	            }
	        }
	    } catch (IntrospectionException e) {
	        e.printStackTrace();
	    } catch (IllegalArgumentException e) {
	    	e.printStackTrace();
	    } catch (IllegalAccessException e) {
	    	e.printStackTrace();
	    } catch (InvocationTargetException e) {
	    	e.printStackTrace();
	    }
		
	    setComponentSize(c, attrs);
	    setComponentMargin(c, attrs.get("margin"));

	}

	private String readComponentClassName(XMLStreamReader reader) {
	    QName name = reader.getName();
	    String nameSpace = name.getNamespaceURI();
	    if (nameSpace == null || nameSpace.isEmpty()) {
	        nameSpace = DEFAULT_NAMESPACE;
	    }
	    String className = nameSpace + "." + name.getLocalPart();
	    System.out.println("XMLUI.readComponentClassName():" + className);
	    return className;
	}

	private Map<String, String> readCurrentElementAttributes(
	        XMLStreamReader reader) {
	    Map<String, String> am = new HashMap<String, String>();
	    for (int i = 0; i < reader.getAttributeCount(); i++) {
	        String n = reader.getAttributeLocalName(i);
	        String v = reader.getAttributeValue(i);
	        am.put(n, v);
	    }
	    return am;
	}

	private void readEndElement(Stack<Component> componentStack,
	        Stack<Map<String, String>> childAttributesStack,
	        XMLStreamReader reader) {
	    String cn = readComponentClassName(reader);
	    Component component=instantiateComponent(cn);
	    if (component !=null) {
	    	Map<String, String> attrs = childAttributesStack.size() >0 ?  childAttributesStack.pop() : new HashMap<String, String>();
	        Component c = componentStack.pop();
	        if (!c.getClass().getName().equals(cn)) {
	            throw new IllegalStateException();
	        }
	        setComponentAttributes(c, attrs);
	        if (!componentStack.isEmpty()) {
	            ComponentContainer parent = (ComponentContainer) componentStack
	                    .peek();
	            if ((parent instanceof Window && c instanceof Window)) {
	                ((Window) parent).addWindow((Window) c);
	            } else if ((parent instanceof Panel) && (c instanceof ComponentContainer)) {
	                ((Panel) parent).setContent((ComponentContainer) c);
	            } else if ((parent instanceof Form) && (c instanceof Layout)) {
	                ((Form) parent).setLayout((Layout) c);
	            } else {
	                addComponentWithAttributes(parent, c, attrs);
	            }
	        }
	    }
	}

	/**
	 * Resets the component tree and rebuilds it from XML.
	 * 
	 * @throws RenderingException
	 *             if the XML is not in a supported format.
	 */
	public void readFrom(InputStream in) throws RenderingException {
	    componentIdMap.clear();
	    root = null;
	    Stack<Component> componentStack = new Stack<Component>();
	    Stack<Map<String, String>> childAttributesStack = new Stack<Map<String, String>>();
	
	    XMLStreamReader reader;
	    try {
	        reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
	
	        while (reader.hasNext()) {
	            int eventType = reader.next();
	            switch (eventType) {
	            case XMLStreamConstants.START_ELEMENT:
	                readStartElement(componentStack, childAttributesStack,
	                        reader);
	                break;
	            case XMLStreamConstants.END_ELEMENT:
	                readEndElement(componentStack, childAttributesStack, reader);
	                break;
	            case XMLStreamConstants.CHARACTERS:
	            case XMLStreamConstants.CDATA:
	                readText(componentStack, reader);
	                break;
	            }
	        }
	
	    } catch (XMLStreamException e) {
	        throw new RenderingException(e);
	    } catch (FactoryConfigurationError e) {
	        throw new RenderingException(e);
	    }
	}

	private void setComponentMargin(Component c, String value) {
	    if (!(c instanceof MarginHandler)) {
	        return;
	    }
	    if (value == null) {
	        return;
	    }
	    MarginHandler mh = (MarginHandler) c;
	    if ("true".equals(value)) {
	        mh.setMargin(new MarginInfo(true));
	    } else if ("false".equals(value)) {
	        mh.setMargin(new MarginInfo(false));
	    } else {
	        mh.setMargin(new MarginInfo(value.indexOf("top") >= 0, value
	                .indexOf("right") >= 0, value.indexOf("bottom") >= 0, value
	                .indexOf("left") >= 0));
	    }
	}

	private void setComponentSize(Component c, Map<String, String> attrs) {
	    if (c instanceof Sizeable) {
	        if (attrs.containsKey("height")) {
	            ((Sizeable) c).setHeight(attrs.get("height"));
	        }
	        if (attrs.containsKey("width")) {
	            ((Sizeable) c).setWidth(attrs.get("width"));
	        }
	    }
	}

	private void readStartElement(Stack<Component> componentStack,
	        Stack<Map<String, String>> childAttributesStack,
	        XMLStreamReader reader) throws RenderingException {
	    String className = readComponentClassName(reader);
	    Component component=instantiateComponent(className);
	    if (component==null) {
	        childAttributesStack.push(readCurrentElementAttributes(reader));
	    } else {
	        readAndCreateComponent(componentStack, childAttributesStack, component, reader);
	    }
	}

	private String readText(Stack<Component> componentStack,
	        XMLStreamReader reader) {
	    Component c = componentStack.peek();
	    if (c instanceof Property) {
	        Property p = (Property) c;
	        Class type = p.getType();
	        if (isTypeSupported(type)) {
	            String value = reader.getText();
	            if (value != null && !value.isEmpty()) {
	                if (String.class == type && value != null
	                        && p.getValue() != null) {
	                    p.setValue(p.toString() + decodeValue(value, type));
	                } else {
	                    p.setValue(decodeValue(value, type));
	                }
	            }
	        }
	    }
	    return null;
	}

	/**
	 * Sets an id for a component. By setting an id, the id attiribute will be
	 * given for the tag corresponding to the component when UI is serialized to
	 * XML.
	 * 
	 * @param component
	 *            that will be assigned a new id
	 * @param id
	 *            string to be assigned for the component.
	 */
	public void setId(Component component, String id) {
	    String prevId = getId(component);
	    if (prevId != null) {
	        idComponentMap.remove(prevId);
	    }
	    idComponentMap.put(id, component);
	    componentIdMap.put(component, id);
	}

	/** Sets the root component of the user interface component tree */
	public void setRoot(Component newRoot) {
	    root = newRoot;
	}
	
}
