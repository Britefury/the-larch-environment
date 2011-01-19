//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.Pres.SequentialPres;

public abstract class AbstractBox extends SequentialPres
{
	public AbstractBox(Object children[])
	{
		super( children );
	}
	
	public AbstractBox(List<Object> children)
	{
		super( children );
	}
}
