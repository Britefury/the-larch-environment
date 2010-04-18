//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import BritefuryJ.Math.Xform2;

class BrowserState
{
	private Location location;
	private Xform2 viewXform;
	
	
	public BrowserState(Location location)
	{
		this.location = location;
		this.viewXform = new Xform2();
	}
	
	public Location getLocation()
	{
		return location;
	}

	public Xform2 getViewTransformation()
	{
		return viewXform;
	}
	
	
	public void setViewTransformation(Xform2 x)
	{
		viewXform = x;
	}
}
