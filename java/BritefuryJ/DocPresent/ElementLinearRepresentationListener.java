//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

public abstract class ElementLinearRepresentationListener
{
	public boolean textRepresentationModified(DPWidget element)
	{
		return false;
	}
	
	public boolean linearRepresentationModified(DPWidget element)
	{
		return false;
	}

	public boolean innerElementLinearRepresentationModified(DPWidget element)
	{
		return false;
	}
}
