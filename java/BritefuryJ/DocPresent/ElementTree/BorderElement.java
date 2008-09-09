//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.StyleSheets.BorderStyleSheet;

public class BorderElement extends BinElement
{
	public BorderElement()
	{
		this( BorderStyleSheet.defaultStyleSheet );
	}
	
	public BorderElement(BorderStyleSheet styleSheet)
	{
		super( new DPBorder( styleSheet ) );
	}
	
	
	public DPBorder getWidget()
	{
		return (DPBorder)widget;
	}
}
