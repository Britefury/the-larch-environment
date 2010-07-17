//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LayoutCombinators;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.View.GSymFragmentView;

public class Table extends LayoutCombinator
{
	private PrimitiveStyleSheet styleSheet;
	private Object children[][];
	
	
	public Table(Object children[][])
	{
		this.children = children;
		styleSheet = PrimitiveStyleSheet.instance;
	}
	
	public Table(PyObject values[])
	{
		children = new Object[values.length][];
		for (int i = 0; i < values.length; i++)
		{
			children[i] = Py.tojava( values[i], Object[].class );
		}
		styleSheet = PrimitiveStyleSheet.instance;
	}
	
	
	public Table withStyle(PrimitiveStyleSheet styleSheet)
	{
		this.styleSheet = styleSheet;
		return this;
	}
	

	@Override
	public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		DPElement elements[][] = new DPElement[children.length][];
		
		for (int y = 0; y < children.length; y++)
		{
			Object row[] = children[y];
			elements[y] = new DPElement[row.length];
			for (int x = 0; x < row.length; x++)
			{
				elements[y][x] = row[x] != null  ?  presentChild( row[x], fragment, styleSheet, inheritedState )  :  null;
			}
		}
		return this.styleSheet.table( elements );
	}
}
