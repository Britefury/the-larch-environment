//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeColumn;
import BritefuryJ.LSpace.StyleParams.ColumnStyleParams;


public class LSColumn extends LSAbstractBox
{
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	protected final static int FLAG_HAS_REFPOINT_INDEX = FLAGS_CONTAINER_END * 0x1;
	
	protected final static int FLAGS_COLUMN_END = FLAGS_CONTAINER_END << 1;

	
	
	private int refPointIndex = 0;
	

	
	public LSColumn()
	{
		this( ColumnStyleParams.defaultStyleParams);
	}
	
	public LSColumn(ColumnStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeColumn( this );
		clearFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	
	
	//
	//
	// Ref-point index
	//
	//


	public void setRefPointIndex(int refPointIndex)
	{
		this.refPointIndex = refPointIndex;
		setFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	public void clearRefPointIndex()
	{
		clearFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	
	
	public boolean hasRefPointIndex()
	{
		return testFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	public int getRefPointIndex()
	{
		return refPointIndex;
	}
}
