//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class ElementAlignment
{
	public static int _HALIGN_MASK = 0x3;
	public static int HALIGN_LEFT = 0;
	public static int HALIGN_CENTRE = 1;
	public static int HALIGN_RIGHT = 2;
	public static int HALIGN_EXPAND = 3;

	public static int _VALIGN_MASK = 0x7  *  0x4;
	public static int VALIGN_REFY = 0  *  0x4;
	public static int VALIGN_REFY_EXPAND = 1  *  0x4;
	public static int VALIGN_TOP = 2  *  0x4;
	public static int VALIGN_CENTRE = 3  *  0x4;
	public static int VALIGN_BOTTOM = 4  *  0x4;
	public static int VALIGN_EXPAND = 5  *  0x4;
	
	public static int _ELEMENTALIGN_MASK = 0x1f;
	public static int _ELEMENTALIGN_END = 0x20;

	
	
	public static int flagValue(HAlignment hAlignment)
	{
		if ( hAlignment == HAlignment.LEFT )
		{
			return HALIGN_LEFT;
		}
		else if ( hAlignment == HAlignment.CENTRE )
		{
			return HALIGN_CENTRE;
		}
		else if ( hAlignment == HAlignment.RIGHT )
		{
			return HALIGN_RIGHT;
		}
		else if ( hAlignment == HAlignment.EXPAND )
		{
			return HALIGN_EXPAND;
		}
		else
		{
			throw new RuntimeException( "Unknown HAlignment value" );
		}
	}
	
	
	public static int flagValue(VAlignment vAlignment)
	{
		if ( vAlignment == VAlignment.REFY )
		{
			return VALIGN_REFY;
		}
		else if ( vAlignment == VAlignment.REFY_EXPAND )
		{
			return VALIGN_REFY_EXPAND;
		}
		else if ( vAlignment == VAlignment.TOP )
		{
			return VALIGN_TOP;
		}
		else if ( vAlignment == VAlignment.CENTRE )
		{
			return VALIGN_CENTRE;
		}
		else if ( vAlignment == VAlignment.BOTTOM )
		{
			return VALIGN_BOTTOM;
		}
		else if ( vAlignment == VAlignment.EXPAND )
		{
			return VALIGN_EXPAND;
		}
		else
		{
			throw new RuntimeException( "Unknown VAlignment value" );
		}
	}
	
	
	public static int flagValue(HAlignment hAlignment, VAlignment vAlignment)
	{
		int value = 0;
		
		if ( hAlignment == HAlignment.LEFT )
		{
			value = HALIGN_LEFT;
		}
		else if ( hAlignment == HAlignment.CENTRE )
		{
			value = HALIGN_CENTRE;
		}
		else if ( hAlignment == HAlignment.RIGHT )
		{
			value = HALIGN_RIGHT;
		}
		else if ( hAlignment == HAlignment.EXPAND )
		{
			value = HALIGN_EXPAND;
		}
		else
		{
			throw new RuntimeException( "Unknown HAlignment value" );
		}
		
		if ( vAlignment == VAlignment.REFY )
		{
			value |= VALIGN_REFY;
		}
		else if ( vAlignment == VAlignment.REFY_EXPAND )
		{
			value |= VALIGN_REFY_EXPAND;
		}
		else if ( vAlignment == VAlignment.TOP )
		{
			value |= VALIGN_TOP;
		}
		else if ( vAlignment == VAlignment.CENTRE )
		{
			value |= VALIGN_CENTRE;
		}
		else if ( vAlignment == VAlignment.BOTTOM )
		{
			value |= VALIGN_BOTTOM;
		}
		else if ( vAlignment == VAlignment.EXPAND )
		{
			value |= VALIGN_EXPAND;
		}
		else
		{
			throw new RuntimeException( "Unknown VAlignment value" );
		}
		
		return value;
	}
	
	
	
	
	public static HAlignment getHAlignment(int value)
	{
		value &= _HALIGN_MASK;
		if ( value == HALIGN_LEFT )
		{
			return HAlignment.LEFT;
		}
		else if ( value == HALIGN_CENTRE )
		{
			return HAlignment.CENTRE;
		}
		else if ( value == HALIGN_RIGHT )
		{
			return HAlignment.RIGHT;
		}
		else if ( value == HALIGN_EXPAND )
		{
			return HAlignment.EXPAND;
		}
		else
		{
			throw new RuntimeException( "Unknown h-alignment value" );
		}
	}


	public static VAlignment getVAlignment(int value)
	{
		value &= _VALIGN_MASK;
		if ( value == VALIGN_REFY )
		{
			return VAlignment.REFY;
		}
		else if ( value == VALIGN_REFY_EXPAND )
		{
			return VAlignment.REFY_EXPAND;
		}
		else if ( value == VALIGN_TOP )
		{
			return VAlignment.TOP;
		}
		else if ( value == VALIGN_CENTRE )
		{
			return VAlignment.CENTRE;
		}
		else if ( value == VALIGN_BOTTOM)
		{
			return VAlignment.BOTTOM;
		}
		else if ( value == VALIGN_EXPAND )
		{
			return VAlignment.EXPAND;
		}
		else
		{
			throw new RuntimeException( "Unknown v-alignment value" );
		}
	}
}
