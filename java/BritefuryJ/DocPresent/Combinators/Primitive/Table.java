//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.Combinators.PresentationCombinator;

public class Table extends PresentationCombinator
{
	private PresentationCombinator children[][];
	
	
	public Table()
	{
		this.children = null;
	}
	
	public Table(Object children[][])
	{
		this.children = new PresentationCombinator[children.length][];
		for (int y = 0; y < children.length; y++)
		{
			Object row[] = children[y];
			this.children[y] = new PresentationCombinator[row.length];
			for (int x = 0; x < row.length; x++)
			{
				this.children[y][x] = coerce( row[x] );
			}
		}
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPTable element = new DPTable( ctx.getStyle().getTableParams() );
		if ( children != null )
		{
			DPElement childElems[][] = new DPElement[children.length][];
			for (int y = 0; y < children.length; y++)
			{
				PresentationCombinator row[] = children[y];
				childElems[y] = new DPElement[row.length];
				for (int x = 0; x < row.length; x++)
				{
					PresentationCombinator child = row[x];
					childElems[y][x] = child != null  ?  child.present( ctx ).layoutWrap()  :  null;
				}
			}
		}
		return element;
	}
}
