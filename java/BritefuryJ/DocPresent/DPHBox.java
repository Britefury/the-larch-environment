//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeHBox;
import BritefuryJ.DocPresent.StyleParams.HBoxStyleParams;


public class DPHBox extends DPAbstractBox
{
	public DPHBox()
	{
		this( HBoxStyleParams.defaultStyleParams);
	}
	
	public DPHBox(HBoxStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeHBox( this );
	}
}
