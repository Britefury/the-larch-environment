//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
