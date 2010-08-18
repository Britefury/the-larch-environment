//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.PresCom;

import java.util.ArrayList;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;

public class UnescapedStringAsRow extends UnescapedString
{
	public UnescapedStringAsRow(String value)
	{
		super( value );
	}
	
	
	@Override
	protected Pres createContainer(ArrayList<Object> contents)
	{
		return new Row( contents );
	}
}
