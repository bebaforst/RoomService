package info.margreiter.roomservice;

import info.margreiter.roomservice.logon.LogonCtrl;
import info.margreiter.vaadin.MainCtrl;
import info.margreiter.vaadin.RenderingException;
import info.margreiter.vaadin.TabCtrl;
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

public class Roomservice extends Application implements ClickListener,MainCtrl {
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
	
	
	@Override
	public void init() {
		Window mainWindow = new Window("RoomService");
		setMainWindow(mainWindow);
		try {
			initSubCtrl(new LogonCtrl(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	public void initSubCtrl(TabCtrl subCtrl){
			getMainWindow().removeAllComponents();
			getMainWindow().addComponent(subCtrl.getUi().getRoot());
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
//		if ("removeTab".equals(caption)) removeTab();
//		if ("addTab".equals(caption)) addTab();
		
				
	}
}
