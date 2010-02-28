//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ContextMenu;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class ContextPopupMenu extends ContextMenu
{
	JPopupMenu menu;
	
	
	public ContextPopupMenu(String text)
	{
		super();
		
		menu = new JPopupMenu( text );
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
	
	
	public void show(JComponent component)
	{
		Point pos = component.getMousePosition();
		menu.show( component, pos.x, pos.y );
	}
}
