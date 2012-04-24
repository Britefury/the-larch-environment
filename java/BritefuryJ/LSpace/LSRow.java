//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeRow;
import BritefuryJ.LSpace.StyleParams.RowStyleParams;


public class LSRow extends LSAbstractBox
{
	public LSRow(LSElement[] items)
	{
		this( RowStyleParams.defaultStyleParams, items );
	}
	
	public LSRow(RowStyleParams styleParams, LSElement[] items)
	{
		super( styleParams, items );
		
		layoutNode = new LayoutNodeRow( this );
	}
}
