//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.RichText.Rule;


public class VPopupMenu extends PopupMenu
{
	public VPopupMenu()
	{
		super();
	}
	
	public VPopupMenu(Object items[])
	{
		super( items );
	}


	public void addSeparator()
	{
		add( Rule.hrule() );
	}


	@Override
	protected Pres createMenuBox(Pres boxItems[])
	{
		return new Column( boxItems );
	}
}
