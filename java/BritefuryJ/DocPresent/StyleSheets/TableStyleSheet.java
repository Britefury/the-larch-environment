//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;


public class TableStyleSheet extends ContainerStyleSheet
{
	public static final TableStyleSheet defaultStyleSheet = new TableStyleSheet();
	
	
	protected final double columnSpacing;
	protected final boolean columnExpand;

	protected final double rowSpacing;
	protected final boolean rowExpand;


	public TableStyleSheet()
	{
		this( 0.0, false, 0.0, false );
	}
	
	public TableStyleSheet(double columnSpacing, boolean columnExpand, double rowSpacing, boolean rowExpand)
	{
		super();
		
		this.columnSpacing = columnSpacing;
		this.columnExpand = columnExpand;

		this.rowSpacing = rowSpacing;
		this.rowExpand = rowExpand;
	}
	
	
	
	public double getColumnSpacing()
	{
		return columnSpacing;
	}

	public boolean getColumnExpand()
	{
		return columnExpand;
	}


	public double getRowSpacing()
	{
		return rowSpacing;
	}

	public boolean getRowExpand()
	{
		return rowExpand;
	}
}
