//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPWhitespace;

public class LayoutNodeWhitespace extends ContentLeafLayoutNode
{
	public LayoutNodeWhitespace(DPWhitespace element)
	{
		super( element );
	}

	

	protected void updateRequisitionX()
	{
		DPWhitespace whitespace = (DPWhitespace)element;
		double width = whitespace.getWhitespaceWidth();
		layoutReqBox.setRequisitionX( width, width );
	}

	protected void updateRequisitionY()
	{
		layoutReqBox.clearRequisitionY();
	}
}
