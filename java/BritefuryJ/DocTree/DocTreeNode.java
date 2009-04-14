//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocTree;

import java.util.ArrayList;
import java.util.Collection;

public abstract class DocTreeNode
{
	public abstract Object getNode();
	public abstract DocTreeNode getParentTreeNode();
	public abstract int getIndexInParent();
	
	
	protected static Object coerce(Object x)
	{
		if ( x instanceof DocTreeNode )
		{
			return ((DocTreeNode)x).getNode();
		}
		else
		{
			return x;
		}
	}


	protected static ArrayList<Object> coerceCollection(Collection<? extends Object> xs)
	{
		ArrayList<Object> ys = new ArrayList<Object>();
		ys.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			ys.add( coerce( x ) );
		}
		return ys;
	}
}
