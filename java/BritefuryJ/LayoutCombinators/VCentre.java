//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LayoutCombinators;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.View.GSymFragmentView;

public class VCentre extends Alignment
{
	public VCentre(Object child)
	{
		super( child );
	}

	@Override
	public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return presentChild( child, fragment, styleSheet, inheritedState ).alignVCentre();
	}
}
