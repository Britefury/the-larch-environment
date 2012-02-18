//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.StyleSheet.StyleValues;

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
		
		return Table.applyTableBorder( style, grid );
	}
}
