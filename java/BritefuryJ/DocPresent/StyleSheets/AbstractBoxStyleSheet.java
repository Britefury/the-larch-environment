//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

public class AbstractBoxStyleSheet extends ContainerStyleSheet
{
	public static AbstractBoxStyleSheet defaultStyleSheet = new AbstractBoxStyleSheet();
	
	
	protected double spacing, padding;
	protected boolean bExpand;


	public AbstractBoxStyleSheet()
	{
		this( 0.0, false, 0.0, null );
	}
	
	public AbstractBoxStyleSheet(Color backgroundColour)
	{
		this( 0.0, false, 0.0, backgroundColour );
	}
	
	public AbstractBoxStyleSheet(double spacing, boolean bExpand, double padding)
	{
		this( spacing, bExpand, padding, null );
	}
	
	public AbstractBoxStyleSheet(double spacing, boolean bExpand, double padding, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.spacing = spacing;
		this.bExpand = bExpand;
		this.padding = padding;
	}

	
	public double getSpacing()
	{
		return spacing;
	}

	public boolean getExpand()
	{
		return bExpand;
	}

	public double getPadding()
	{
		return padding;
	}
}
