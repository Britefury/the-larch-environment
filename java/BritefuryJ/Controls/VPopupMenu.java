//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
