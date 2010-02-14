//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeVBox;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;


public class DPVBox extends DPAbstractBox
{
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public DPVBox()
	{
		this( VBoxStyleSheet.defaultStyleSheet );
	}
	
	public DPVBox(VBoxStyleSheet syleSheet)
	{
		super( syleSheet );
		
		layoutNode = new LayoutNodeVBox( this );
	}
}
