//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Rule;


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


	public void addSeparator()
	{
		add( Rule.vrule() );
	}


	@Override
	protected Pres createMenuBox(Pres boxItems[])
	{
		return new Row( boxItems );
	}
}
