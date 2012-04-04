//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.event.KeyEvent;

import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.Util.HashUtils;

public class Shortcut
{
	private int keyCode;
	private int keyMods;
	
	
	public Shortcut(int keyCode, int keyMods)
	{
		this.keyCode = keyCode;
		this.keyMods = keyMods;
	}
	
	public Shortcut(char keyChar, int keyMods)
	{
		this.keyCode = (int)keyChar;
		this.keyMods = keyMods;
	}
	
	
	
	@Override
	public int hashCode()
	{
		return HashUtils.doubleHash( keyCode, keyMods );
	}
	
	@Override
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof Shortcut )
		{
			Shortcut sx = (Shortcut)x;
			
			return keyCode == sx.keyCode  &&  keyMods == sx.keyMods;
		}
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return "Shortcut( keyCode=" + keyCode + ", mods=" + Modifier.keyModifiersToString( keyMods ) + " )";
	}
	
	
	public static Shortcut fromPressedEvent(KeyEvent event)
	{
		return new Shortcut( event.getKeyCode(), Modifier.getKeyModifiers( event ) );
	}
}
