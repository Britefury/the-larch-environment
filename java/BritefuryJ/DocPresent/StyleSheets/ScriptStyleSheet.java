//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

public class ScriptStyleSheet extends ContainerStyleSheet
{
	public static ScriptStyleSheet defaultStyleSheet = new ScriptStyleSheet();
	
	
	private double columnSpacing, rowSpacing;
	
	
	public ScriptStyleSheet()
	{
		this( 1.0, 1.0 );
	}
	
	public ScriptStyleSheet(double columnSpacing, double rowSpacing)
	{
		super();
		
		this.columnSpacing = columnSpacing;
		this.rowSpacing = rowSpacing;
	}
	
	
	
	public double getColumnSpacing()
	{
		return this.columnSpacing;
	}

	
	public double getRowSpacing()
	{
		return this.rowSpacing;
	}
}
