//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;



abstract public class DPAbstractBox extends DPContainerSequenceCollationRoot
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	
	public DPAbstractBox()
	{
		this( null );
	}

	public DPAbstractBox(ElementStyleSheet styleSheet)
	{
		super( styleSheet );
	}
}
