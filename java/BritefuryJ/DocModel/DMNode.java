//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.List;

import org.python.core.Py;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;

import BritefuryJ.DocTree.DocTreeNode;


public abstract class DMNode
{
	@SuppressWarnings("unchecked")
	public static Object coerce(Object x)
	{
		if ( x == null )
		{
			return x;
		}
		else if ( x instanceof DMNode )
		{
			return x;
		}
		else if ( x instanceof DocTreeNode )
		{
			// !!!!!!THIS MUST REMAIN HERE!!!!!!
			// If not, attempting to convert a DocTree node to a DMNode, will result in the entire
			// tree being rebuilt. This causes two problems:
			// - Its wasteful; we can just extract the underlying DMNode
			// - It accesses all elements in the subtree; every list item, every object field value. These accesses are tracked by the
			// cell system; modifying a node in the tree will result in all document view nodes along the path from the node to
			// the root node being updated.
			return coerce( ((DocTreeNode)x).getNode() );
			// !!!!!!THIS MUST REMAIN HERE!!!!!!
		}
		else if ( x instanceof String )
		{
			// Create a clone of the string to ensure that all String objects in the document are
			// distinct, even if their contents are the same
			return new String( (String)x );
		}
		else if ( x instanceof List )
		{
			return new DMList( (List<Object>)x );
		}
		else if ( x instanceof DMObjectInterface )
		{
			return new DMObject( (DMObjectInterface)x );
		}
		else if ( x instanceof PyJavaType  ||  x instanceof PyObjectDerived )
		{
			Object xx = Py.tojava( (PyObject)x, Object.class );
			if ( xx instanceof PyJavaType  ||  xx instanceof PyObjectDerived )
			{
				System.out.println( "DMNode.coerce(): Could not unwrap " + x );
				return null;
			}
			else
			{
				return coerce( xx );
			}
		}
		else
		{
			System.out.println( "DMNode.coerce(): attempted to coerce " + x.getClass().getName() + " (" + x.toString() + ")" );
			//throw new RuntimeException();
			return x;
		}
	}
}
