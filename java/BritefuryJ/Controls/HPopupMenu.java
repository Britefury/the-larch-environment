//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;

public class HPopupMenu extends PopupMenu
{
	public HPopupMenu()
	{
		super();
	}
	
	public HPopupMenu(Object items[])
	{
		super( items );
	}

	@Override
	protected Pres createMenuBox(Pres boxItems[])
	{
		return new Row( boxItems );
	}
}
