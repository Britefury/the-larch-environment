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
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class LayoutCombinator implements Presentable
{
	protected DPElement presentChild(Object child, GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		if ( child instanceof LayoutCombinator )
		{
			return ((LayoutCombinator)child).present( fragment, styleSheet, inheritedState );
		}
		else
		{
			return fragment.presentFragment( child, styleSheet, inheritedState );
		}
	}

	protected DPElement[] mapPresentChildren(Object children[], GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		DPElement elements[] = new DPElement[children.length];
		for (int i = 0; i < children.length; i++)
		{
			Object child = children[i];
			if ( child instanceof LayoutCombinator )
			{
				elements[i] = ((LayoutCombinator)child).present( fragment, styleSheet, inheritedState );
			}
			else
			{
				elements[i] = fragment.presentFragment( child, styleSheet, inheritedState );
			}
		}
		return elements;
	}
}
