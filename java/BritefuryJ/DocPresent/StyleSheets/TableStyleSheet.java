//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import BritefuryJ.DocPresent.DPTable;

public class TableStyleSheet extends ContainerStyleSheet
{
	public static TableStyleSheet defaultStyleSheet = new TableStyleSheet();
	
	
	protected DPTable.RowAlignment rowAlignment;
	protected DPTable.ColumnAlignment columnAlignment;

	protected double spacingX, paddingX;
	protected boolean bExpandX;

	protected double spacingY, paddingY;
	protected boolean bExpandY;

	
	public TableStyleSheet()
	{
		this( DPTable.RowAlignment.BASELINES, DPTable.ColumnAlignment.CENTRE, 0.0, false, 0.0, 0.0, false, 0.0 );
	}
	
	public TableStyleSheet(DPTable.RowAlignment rowAlignment, DPTable.ColumnAlignment columnAlignment,
			double spacingX, boolean bExpandX, double paddingX, double spacingY, boolean bExpandY, double paddingY)
	{
		super();
		
		this.rowAlignment = rowAlignment;
		this.columnAlignment = columnAlignment;

		this.spacingX = spacingX;
		this.bExpandX = bExpandX;
		this.paddingX = paddingX;

		this.spacingY = spacingY;
		this.paddingY = paddingY;
		this.bExpandY = bExpandY;
	}
	
	
	
	public DPTable.RowAlignment getRowAlignment()
	{
		return rowAlignment;
	}

	public DPTable.ColumnAlignment getColumnAlignment()
	{
		return columnAlignment;
	}

	
	public double getSpacingX()
	{
		return spacingX;
	}

	public boolean getExpandX()
	{
		return bExpandX;
	}

	public double getPaddingX()
	{
		return paddingX;
	}


	public double getSpacingY()
	{
		return spacingY;
	}

	public boolean getExpandY()
	{
		return bExpandY;
	}

	public double getPaddingY()
	{
		return paddingY;
	}
}
