//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ContextMenu;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

public abstract class ContextMenu
{
	protected boolean bEmpty;
	
	
	public ContextMenu()
	{
		bEmpty = true;
	}
	
	
	public void addItem(String text, Icon icon, ActionListener action)
	{
		JMenuItem item = new JMenuItem( text, icon );
		item.addActionListener( action );
		addItem( item );
	}
	
	public void addItem(String text, ActionListener action)
	{
		JMenuItem item = new JMenuItem( text );
		item.addActionListener( action );
		addItem( item );
	}
	
	public void addItem(Icon icon, ActionListener action)
	{
		JMenuItem item = new JMenuItem( icon );
		item.addActionListener( action );
		addItem( item );
	}
	
	public abstract void addItem(JMenuItem item);
	public abstract void addSeparator();
	
	
	
	public ContextMenu addSubMenu(String text)
	{
		ContextSubmenu sub = new ContextSubmenu( text );
		addItem( sub.menu );
		return sub;
	}
	
	
	public boolean isEmpty()
	{
		return bEmpty;
	}
}