//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPWhitespace;

public class WhitespaceElement extends LeafElement
{
	public WhitespaceElement(String whitespace)
	{
		this( whitespace, 0.0 );
	}

	public WhitespaceElement(String whitespace, double width)
	{
		super( new DPWhitespace(  width ), whitespace );
	}



	public DPWhitespace getWidget()
	{
		return (DPWhitespace)widget;
	}
}
