//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
