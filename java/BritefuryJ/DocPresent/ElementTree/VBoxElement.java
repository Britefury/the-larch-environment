//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class VBoxElement extends SequenceBranchElement
{
	public VBoxElement()
	{
		this( VBoxStyleSheet.defaultStyleSheet );
	}

	public VBoxElement(VBoxStyleSheet styleSheet)
	{
		super( new DPVBox( styleSheet ) );
	}


	public DPVBox getWidget()
	{
		return (DPVBox)widget;
	}




	public Element getContentLineFromChild(Element element)
	{
		return this;
	}
}
