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

public class Row extends Sequence
{
	private PrimitiveStyleSheet styleSheet;

	
	public Row(Object[] children)
	{
		super( children );
		styleSheet = PrimitiveStyleSheet.instance;
	}

	public Row(PyObject[] values)
	{
		super( values );
		styleSheet = PrimitiveStyleSheet.instance;
	}
	

	@Override
	public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return this.styleSheet.hbox( mapPresentChildren( children, fragment, styleSheet, inheritedState ) );
	}
}
