//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeHiddenText;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;

public class DPHiddenText extends DPContentLeaf
{
	public DPHiddenText()
	{
		this( ContentLeafStyleParams.defaultStyleParams, "" );
	}
	
	public DPHiddenText(String textRepresentation)
	{
		this( ContentLeafStyleParams.defaultStyleParams, textRepresentation );
	}
	
	public DPHiddenText(ContentLeafStyleParams styleParams)
	{
		this(styleParams, "" );
	}

	public DPHiddenText(ContentLeafStyleParams styleParams, String textRepresentation)
	{
		super( styleParams, textRepresentation );
		
		layoutNode = new LayoutNodeHiddenText( this );
	}
}
