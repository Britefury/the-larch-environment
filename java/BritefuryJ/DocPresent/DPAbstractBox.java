//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.AbstractBoxStyleSheet;



abstract public class DPAbstractBox extends DPContainerSequence
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	
	public DPAbstractBox(ElementContext context)
	{
		this( context, AbstractBoxStyleSheet.defaultStyleSheet );
	}

	public DPAbstractBox(ElementContext context, AbstractBoxStyleSheet styleSheet)
	{
		super( context, styleSheet );
	}


	
	

	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}


	public double getSpacing()
	{
		return ((AbstractBoxStyleSheet)styleSheet).getSpacing();
	}
}
