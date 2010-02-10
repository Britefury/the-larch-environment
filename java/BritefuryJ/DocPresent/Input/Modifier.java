//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Input;

public class Modifier
{
	public static final int CTRL = 0x1;
	public static final int SHIFT = 0x2;
	public static final int ALT = 0x4;
	public static final int ALT_GRAPH = 0x8;
	public static final int BUTTON1 = 0x0100;
	public static final int BUTTON2 = 0x0200;
	public static final int BUTTON3 = 0x0400;
	public static final int BUTTON4 = 0x0800;
	public static final int BUTTON5 = 0x1000;
	public static final int BUTTON6 = 0x2000;
	public static final int BUTTON7 = 0x4000;
	public static final int BUTTON8 = 0x8000;
	
	public static final int _KEYS_MASK = CTRL | SHIFT | ALT | ALT_GRAPH;
	
	public static final int _BUTTONS_SHIFT = 8;
	public static final int _BUTTONS_MASK = 0xff00;
	
	
	public static boolean getButton(int modifiers, int button)
	{
		return ( ( ( modifiers & _BUTTONS_MASK )  >>  _BUTTONS_SHIFT )  >>  button ) != 0;
	}

	public static int setButton(int modifiers, int button, boolean value)
	{
		int mask = 0x1 << _BUTTONS_SHIFT << button;
		if ( value )
		{
			return modifiers | mask;
		}
		else
		{
			return modifiers & ~mask;
		}
	}


	public static boolean isAButtonPressed(int modifiers)
	{
		return ( modifiers & _BUTTONS_MASK ) != 0;
	}
}
