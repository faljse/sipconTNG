package se.omnitor.protocol.t140;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class PastePopupMenu {
	   private JPopupMenu CallControlMenu;
	   private JMenuItem  CallControlMenuItem;
	   public static PastePopupMenu instance;
	private PastePopupMenu(){
		CallControlMenu = new JPopupMenu();
		CallControlMenuItem = new JMenuItem("Paste");
		CallControlMenu.add(CallControlMenuItem);
	}
	public static PastePopupMenu getInstance()
	{
		if(instance==null)
			instance=new PastePopupMenu();
		return instance;
	}
	public JMenuItem getCallControlMenuItem(){
		return CallControlMenuItem;
	}
	public JPopupMenu CallControlMenu(){
		return CallControlMenu;
	}
	
	
	
}
