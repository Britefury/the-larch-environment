//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeVBox;
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;


public class DPVBox extends DPAbstractBox
{
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	private int refPointIndex = -1;
	

	
	public DPVBox()
	{
		this( VBoxStyleParams.defaultStyleParams);
	}
	
	public DPVBox(VBoxStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeVBox( this );
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
}
