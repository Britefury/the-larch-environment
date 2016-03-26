//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Layout;

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
