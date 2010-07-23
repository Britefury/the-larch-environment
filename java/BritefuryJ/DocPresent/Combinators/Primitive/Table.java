//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.Combinators.Pres;

public class Table extends Pres
{
	private Pres children[][];
	
	
	public Table()
	{
		this.children = null;
	}
	
	public Table(Object children[][])
	{
		this.children = new Pres[children.length][];
		for (int y = 0; y < children.length; y++)
		{
			Object row[] = children[y];
			this.children[y] = new Pres[row.length];
			for (int x = 0; x < row.length; x++)
			{
				this.children[y][x] = coerce( row[x] );
			}
		}
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		PresentationContext childCtx = ctx.withStyle( ctx.getStyle().useContainerParams() );		
		DPTable element = new DPTable( ctx.getStyle().getTableParams() );
		if ( children != null )
		{
			DPElement childElems[][] = new DPElement[children.length][];
			for (int y = 0; y < children.length; y++)
			{
				Pres row[] = children[y];
				childElems[y] = new DPElement[row.length];
				for (int x = 0; x < row.length; x++)
				{
					Pres child = row[x];
					childElems[y][x] = child != null  ?  child.present( childCtx ).layoutWrap()  :  null;
				}
			}
		}
		return element;
	}
}
