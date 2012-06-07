//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Browser.Location;

public interface PageController
{
	public enum OpenOperation
	{
		OPEN_IN_CURRENT_TAB,
		OPEN_IN_NEW_TAB,
		OPEN_IN_NEW_WINDOW
	}
	
	public void openLocation(Location location, OpenOperation op);
}
