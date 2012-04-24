//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSFlowGrid;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.StyleSheet.StyleValues;

public class FlowGrid extends SequentialPres
{
	private int targetNumColumns;
	
	
	public FlowGrid(int targetNumColumns, Object children[])
	{
		super( children );
		
		this.targetNumColumns = targetNumColumns;
	}
	
	public FlowGrid(int targetNumColumns, List<Object> children)
	{
		super( children );
		
		this.targetNumColumns = targetNumColumns;
	}

	public FlowGrid(Object children[])
	{
		this( -1, children );
	}
	
	public FlowGrid(List<Object> children)
	{
		this( -1, children );
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement[] childElems = mapPresent( ctx, Primitive.useTableParams( style ).withAttr( Primitive.hAlign, HAlignment.PACK ), children );
		LSFlowGrid grid = new LSFlowGrid( Primitive.tableParams.get( style ), targetNumColumns, childElems );
		// TODO:
		// Problem is: the border element expands to take full width, since the preferred width is normally more than is available.
		// When row expand is false, then the table should shrink to fit around the grid.
		// return Table.applyTableBorder( style, grid );
		return grid;
	}
}
