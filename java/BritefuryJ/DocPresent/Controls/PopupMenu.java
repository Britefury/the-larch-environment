//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPAbstractBox;
import BritefuryJ.DocPresent.DPElement;

public class PopupMenu
{
	private DPAbstractBox menuBox;
	private boolean bEmpty;
	
	
	protected PopupMenu(DPAbstractBox menuBox)
	{
		this.menuBox = menuBox;
		bEmpty = menuBox.size() == 0;
	}
	
	
	
	public void add(DPElement element)
	{
		menuBox.append( element );
		bEmpty = false;
	}
	
	
	public void popupToRightOf(DPElement element)
	{
		element.popupRight( menuBox, true );
	}
	
	public void popupBelow(DPElement element)
	{
		element.popupBelow( menuBox, true );
	}
	
	public void popupAtMousePosition(DPElement element)
	{
		element.getRootElement().popupAtMousePosition( menuBox, true );
	}
	
	
	
	
	public boolean isEmpty()
	{
		return bEmpty;
	}
}
