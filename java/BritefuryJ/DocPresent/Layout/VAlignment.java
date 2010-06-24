//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public enum VAlignment
{
	REFY,
	REFY_EXPAND,
	TOP,
	CENTRE,
	BOTTOM,
	EXPAND;
	
	
	
	public static VAlignment noRefPoint(VAlignment vAlign)
	{
		if ( vAlign == VAlignment.REFY )
		{
			return CENTRE;
		}
		else if ( vAlign == VAlignment.REFY_EXPAND )
		{
			return EXPAND;
		}
		else
		{
			return vAlign;
		}
	}
	
	
	public static boolean isPack(VAlignment vAlign)
	{
		return vAlign == VAlignment.REFY;
	}

	public static boolean isExpand(VAlignment vAlign)
	{
		return vAlign == VAlignment.EXPAND  ||  vAlign == VAlignment.REFY_EXPAND;
	}

	public static boolean isRefY(VAlignment vAlign)
	{
		return vAlign == VAlignment.REFY  ||  vAlign == VAlignment.REFY_EXPAND;
	}
}
