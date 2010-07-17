//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LayoutCombinators;

import org.python.core.PyObject;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.View.GSymFragmentView;

public class Grid extends Sequence
{
	private PrimitiveStyleSheet styleSheet;

	
	public Grid(Object[] children)
	{
		super( children );
		styleSheet = PrimitiveStyleSheet.instance;
	}

	public Grid(PyObject[] values)
	{
		super( values );
		styleSheet = PrimitiveStyleSheet.instance;
	}
	
	
	public Grid withStyle(PrimitiveStyleSheet styleSheet)
	{
		this.styleSheet = styleSheet;
		return this;
	}
	

	@Override
	public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return this.styleSheet.rgrid( mapPresentChildren( children, fragment, styleSheet, inheritedState ) );
	}
}
