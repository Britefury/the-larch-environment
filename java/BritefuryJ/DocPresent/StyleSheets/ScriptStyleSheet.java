//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

public class ScriptStyleSheet extends ContainerStyleSheet
{
	public static ScriptStyleSheet defaultStyleSheet = new ScriptStyleSheet();
	
	
	private double spacing, scriptSpacing;
	
	
	public ScriptStyleSheet()
	{
		this( 1.0, 1.0, null );
	}
	
	public ScriptStyleSheet(double spacing, double scriptSpacing)
	{
		this( spacing, scriptSpacing, null );
	}
	
	public ScriptStyleSheet(double spacing, double scriptSpacing, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.spacing = spacing;
		this.scriptSpacing = scriptSpacing;
	}
	
	
	
	public double getSpacing()
	{
		return this.spacing;
	}

	
	public double getScriptSpacing()
	{
		return this.scriptSpacing;
	}
}
