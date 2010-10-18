//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.SequentialPres;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class RGrid extends SequentialPres
{
	public RGrid(Object children[])
	{
		super( children );
	}
	
	public RGrid(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPRGrid grid = new DPRGrid( Primitive.tableParams.get( style ) );
		grid.setChildren( mapPresent( ctx, Primitive.useTableParams( style ), children ) );

		AbstractBorder tableBorder = style.get( Primitive.tableBorder, AbstractBorder.class );
		if ( tableBorder != null )
		{
			DPBorder border = new DPBorder( tableBorder );
			border.setChild( grid );
			return border;
		}
		else
		{
			return grid;
		}
	}
}
