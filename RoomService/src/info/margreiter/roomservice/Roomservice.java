package info.margreiter.roomservice;

import info.margreiter.vaadin.RenderingException;
import info.margreiter.vaadin.VaadXmlRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.vaadin.Application;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

public class Roomservice extends Application implements ClickListener {
	// alt k     alt m    und F4   werden als shortcuttest verwendet
	
	ShortcutListener sl=new ShortcutListener("alt+k",ShortcutAction.KeyCode.K, new int[]{ShortcutAction.ModifierKey.ALT}) {
		public void handleAction(Object sender, Object target) {
			// TODO Test 10.01.2012
			System.out.println(sender.toString());
			System.out.println("globaler shortcut funktioniert !!"  + target.toString());
		}
	};
	ShortcutListener sl2=new ShortcutListener("&m") {
		public void handleAction(Object sender, Object target) {
			// TODO Test 10.01.2012
			System.out.println(sender.toString());
			System.out.println("globaler shortcut funktioniert !!"  + target.toString());
		}
	};
	private VaadXmlRenderer baseUi;
	private VaadXmlRenderer subUi=null;
	
	
	@Override
	public void init() {
		Window mainWindow = new Window("FerpsProdVaadin");
		Label label = new Label("Hello VaadinTESTj user und so weiter");
	
		mainWindow.addComponent(label);
		TextField ts=new TextField();
		setMainWindow(mainWindow);
		try {
				baseUi=readUI("example2.xml");
				mainWindow.addComponent(baseUi.getRoot());
				Button addTab = (Button) baseUi.getById("addTab");
				addTab.addListener((ClickListener) this);
				addTab.addShortcutListener(sl);
				
				Button shortCutButton=(Button) baseUi.getById("removeTab");
				shortCutButton.setClickShortcut(KeyCode.F4);
				shortCutButton.addListener(this);
				
				Button mnemonicButton=(Button) baseUi.getById("myBut3");
				mnemonicButton.addShortcutListener(sl2);
				
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RenderingException e) {
			e.printStackTrace();
		}

		
		
	}
	
	private VaadXmlRenderer readUI(String resource) throws IOException, RenderingException {
		// TODO Test 25.01.2012
		URL xmlResource = getClass().getResource(resource);
		InputStream stream;
		if (null==xmlResource) throw new IOException("resourceNotFound");
		stream=xmlResource.openStream();
		VaadXmlRenderer ui = new VaadXmlRenderer();
		ui.readFrom(stream);
		return ui;
	}

	public void buttonClick(ClickEvent event) {
		// TODO Test 09.01.2012
		
		System.out.println("caption:" + event.getButton().getCaption() + " .. id: " + event.getButton().getDebugId() + event.getButton() + event.isCtrlKey());
		System.out.println("button click funktioniert " + event.toString());
		String caption = event.getButton().getCaption();
		if ("removeTab".equals(caption)) removeTab();
		if ("addTab".equals(caption)) addTab();
		
				
	}

	private void addTab() {
		// TODO Test 25.01.2012
		removeTab();
		try {
			subUi=readUI("subexample2.xml");
			Component tabsheet = subUi.getById("removeAbleGrid");
//			Component mainPanel = baseUi.getById("mainPanel");
			Component mainPanel = baseUi.getById("mainTab");
			if (mainPanel instanceof ComponentContainer) {
				ComponentContainer container = (ComponentContainer)mainPanel;
				container.addComponent(tabsheet);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RenderingException e) {
			e.printStackTrace();
		}
	}

	private void removeTab() {
		// TODO Test 25.01.2012
		if (null!=subUi){
			Component tabsheet = subUi.getById("removeAbleGrid");			
			Component mainPanel = baseUi.getById("mainPanel");
			if (mainPanel instanceof ComponentContainer) {
				ComponentContainer container = (ComponentContainer)mainPanel;
				container.removeComponent(tabsheet);
			}
			subUi=null;
		}
	}
	
}
