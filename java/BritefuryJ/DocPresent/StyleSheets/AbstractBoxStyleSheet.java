//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import BritefuryJ.DocPresent.Layout.BoxPackingParams;
import BritefuryJ.DocPresent.Layout.PackingParams;

public class AbstractBoxStyleSheet extends ContainerStyleSheet
{
	public static AbstractBoxStyleSheet defaultStyleSheet = new AbstractBoxStyleSheet();
	
	
	protected double spacing, padding;
	protected boolean bExpand;
	protected BoxPackingParams defaultPackingParams;


	public AbstractBoxStyleSheet()
	{
		this( 0.0, false, 0.0 );
	}
	
	public AbstractBoxStyleSheet(double spacing, boolean bExpand, double padding)
	{
		super();
		
		this.spacing = spacing;
		this.bExpand = bExpand;
		this.padding = padding;
		
		defaultPackingParams = new BoxPackingParams( padding, bExpand );
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



	public PackingParams getDefaultPackingParams()
	{
		return defaultPackingParams;
	}
}
