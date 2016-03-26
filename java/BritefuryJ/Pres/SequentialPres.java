//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import java.util.List;

public abstract class SequentialPres extends Pres
{
	protected Pres children[];
	
	
	public SequentialPres(Object children[])
	{
		this.children = mapCoerce( children );
	}
	
	public SequentialPres(List<Object> children)
	{
		this.children = mapCoerce( children );
	}
}
