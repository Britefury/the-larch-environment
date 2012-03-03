//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeGridRow;
import BritefuryJ.LSpace.StyleParams.GridRowStyleParams;

public class LSGridRow extends LSContainerSequence
{
	public LSGridRow()
	{
		this( GridRowStyleParams.defaultStyleParams);
	}
	
	public LSGridRow(GridRowStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeGridRow( this );
	}
}
