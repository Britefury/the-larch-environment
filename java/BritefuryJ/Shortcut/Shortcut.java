//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Shortcut;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.HashUtils;

public class Shortcut implements Presentable
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
	
	
	public static Shortcut fromPressedEvent(KeyEvent event)
	{
		return new Shortcut( event.getKeyCode(), Modifier.getKeyModifiersFromEvent( event ) );
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
	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String keyName = KeyEvent.getKeyText( keyCode );
		Pres key = keyBorder.surround( keyStyle.applyTo( new Label( keyName ) ) );
		List<String> modifierNames = Modifier.getKeyModifierNames( keyMods );
		ArrayList<Pres> contents = new ArrayList<Pres>();
		for (String modName: modifierNames)
		{
			contents.add( modifierBorder.surround( modifierStyle.applyTo( new Label( modName ) ) ) );
		}
		contents.add( key );
		return new Row( contents.toArray( new Pres[] {} ) );
	}
	
	
	private static final SolidBorder keyBorder = new SolidBorder( 1.0, 1.0, 4.0, 4.0, new Color( 0.0f, 0.4f, 0.8f ), new Color( 0.9f, 0.95f, 1.0f ) );
	private static final StyleSheet keyStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.25f, 0.5f ) ) );

	private static final SolidBorder modifierBorder = new SolidBorder( 1.0, 1.0, 4.0, 4.0, new Color( 0.0f, 0.8f, 0.0f ), new Color( 0.9f, 1.0f, 0.9f ) );
	private static final StyleSheet modifierStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );
}
