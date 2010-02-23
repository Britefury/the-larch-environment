//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import BritefuryJ.DocPresent.Painter.Painter;


public class AbstractBoxStyleParams extends ContainerStyleParams
{
	public static final AbstractBoxStyleParams defaultStyleParams = new AbstractBoxStyleParams( null, 0.0 );
	
	
	protected final double spacing;


	public AbstractBoxStyleParams(Painter background, double spacing)
	{
		super( background );
		
		this.spacing = spacing;
	}


	public double getSpacing()
	{
		return spacing;
	}
}
