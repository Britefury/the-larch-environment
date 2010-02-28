//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ContextMenu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ContextSubmenu extends ContextMenu
{
	protected JMenu menu;
	
	
	protected ContextSubmenu(String text)
	{
		super();
		
		menu = new JMenu( text );
	}


	public void addItem(JMenuItem item)
	{
		menu.add( item );
		bEmpty = false;
	}
	
	public void addSeparator()
	{
		menu.addSeparator();
		bEmpty = false;
	}
}
