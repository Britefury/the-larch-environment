//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;

public class TableStyleSheet extends ContainerStyleSheet
{
	public static TableStyleSheet defaultStyleSheet = new TableStyleSheet();
	
	
	protected VAlignment rowAlignment;
	protected HAlignment columnAlignment;

	protected double spacingX;
	protected boolean bExpandX;

	protected double spacingY;
	protected boolean bExpandY;

	protected double paddingX;
	protected double paddingY;


	public TableStyleSheet()
	{
		this( VAlignment.BASELINES, HAlignment.CENTRE, 0.0, false, 0.0, 0.0, false, 0.0 );
	}
	
	public TableStyleSheet(VAlignment rowAlignment, HAlignment columnAlignment,
			double spacingX, boolean bExpandX, double paddingX, double spacingY, boolean bExpandY, double paddingY)
	{
		super();
		
		this.rowAlignment = rowAlignment;
		this.columnAlignment = columnAlignment;

		this.spacingX = spacingX;
		this.bExpandX = bExpandX;

		this.spacingY = spacingY;
		this.bExpandY = bExpandY;

		this.paddingX = paddingX;
		this.paddingY = paddingY;
	}
	
	
	
	public VAlignment getRowAlignment()
	{
		return rowAlignment;
	}

	public HAlignment getColumnAlignment()
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


	public double getSpacingY()
	{
		return spacingY;
	}

	public boolean getExpandY()
	{
		return bExpandY;
	}

	
	public double getPaddingX()
	{
		return paddingX;
	}

	public double getPaddingY()
	{
		return paddingY;
	}
}
