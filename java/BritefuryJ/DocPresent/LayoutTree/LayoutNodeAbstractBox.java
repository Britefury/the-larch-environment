//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPAbstractBox;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.StyleSheets.AbstractBoxStyleSheet;

public abstract class LayoutNodeAbstractBox extends ArrangedSequenceLayoutNode
{
	public LayoutNodeAbstractBox(DPContainer element)
	{
		super( element );
	}

	
	protected int getRefPointIndex()
	{
		return ((DPAbstractBox)element).getRefPointIndex();
	}
	
	protected double getSpacing()
	{
		return ((AbstractBoxStyleSheet)element.getStyleSheet()).getSpacing();
	}
}
