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
	
	
	public DPVBox(ElementContext context)
	{
		this( context, VBoxStyleSheet.defaultStyleSheet );
	}
	
	public DPVBox(ElementContext context, VBoxStyleSheet syleSheet)
	{
		super( context, syleSheet );
		
		layoutNode = new LayoutNodeVBox( this );
	}
}
