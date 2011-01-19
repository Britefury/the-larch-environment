//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.util.ArrayList;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;


public class Tooltip extends TimedPopup
{
	public Tooltip(String text, double timeout)
	{
		super( createChild( text ), timeout, false );
	}
	
	
	private static Pres createChild(String text)
	{
		String lineTexts[] = text.split( "\\r?\\n" );
		ArrayList<Object> lines = new ArrayList<Object>();
		for (String line: lineTexts)
		{
			lines.add( new Label( line ) );
		}
		return StyleSheet.instance.remapAttr( Primitive.border, Controls.tooltipBorder ).applyTo( new Border( new Column( lines ) ) );
	}
}
