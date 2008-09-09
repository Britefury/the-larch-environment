//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

public class ContainerStyleSheet extends WidgetStyleSheet
{
	public static ContainerStyleSheet defaultStyleSheet = new ContainerStyleSheet();
	
	
	private Color backgroundColour;
	
	
	public ContainerStyleSheet()
	{
		this( null );
	}
	
	public ContainerStyleSheet(Color backgroundColour)
	{
		super();
		
		this.backgroundColour = backgroundColour;
	}
	
	
	
	public Color getBackgroundColour()
	{
		return this.backgroundColour;
	}
}
