//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeGridRow;
import BritefuryJ.LSpace.StyleParams.GridRowStyleParams;

public class LSGridRow extends LSContainerSequence
{
	public LSGridRow(GridRowStyleParams styleParams, LSElement[] items)
	{
		super( styleParams, items );
		
		layoutNode = new LayoutNodeGridRow( this );
	}
}
