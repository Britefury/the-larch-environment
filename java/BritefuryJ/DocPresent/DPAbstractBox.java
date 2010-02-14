//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleParams.AbstractBoxStyleParams;


abstract public class DPAbstractBox extends DPContainerSequence
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	
	private int refPointIndex;
	

	
	public DPAbstractBox()
	{
		this( AbstractBoxStyleParams.defaultStyleParams );
	}

	public DPAbstractBox(AbstractBoxStyleParams styleParams)
	{
		super(styleParams);

		refPointIndex = 0;
	}


	
	

	public void setRefPointIndex(int refPointIndex)
	{
		if ( refPointIndex >= getChildren().size() )
		{
			throw new IndexOutOfBoundsException( "Vertical box - reference index out of range" );
		}
		this.refPointIndex = refPointIndex;
	}
	
	public int getRefPointIndex()
	{
		return refPointIndex;
	}
	
	
	
	public double getSpacing()
	{
		return ((AbstractBoxStyleParams) styleParams).getSpacing();
	}
}
