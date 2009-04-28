//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Transformation;

import java.util.ArrayList;
import java.util.Arrays;

import org.python.core.PyObject;


public class Transformation
{
	private static class TransformationApplication extends TransformationFunction
	{
		private TransformationFunction identity;
		private ArrayList<TransformationFunction> xforms;
		private ArrayList<Boolean> stack;
		
		
		public TransformationApplication(TransformationFunction identity, ArrayList<TransformationFunction> xforms)
		{
			this.identity = identity;
			this.xforms = xforms;
			this.stack = new ArrayList<Boolean>();
			this.stack.add( false );
		}
		
		
		public Object apply(Object node, TransformationFunction innerNodeXform)
		{
			stack.add( false );
			
			boolean bTransformed = false;
			for (TransformationFunction f: xforms)
			{
				Object v = f.apply( node, this );
				if ( v != TransformationFunction.cannotApplyTransformationValue )
				{
					node = v;
					bTransformed = true;
				}
			}
			
			
			if ( bTransformed )
			{
				// Node transformed, set outer stack entry to True, and pop
				stack.set( stack.size() - 2, true );
				stack.remove( stack.size() - 1 );
				return node;
			}
			else
			{
				Object nodeCopy = identity.apply( node, this );
				if ( stack.get( stack.size() - 1 ).booleanValue() )
				{
					// Inner node was transformed; need to use a copy of @node
					stack.remove( stack.size() - 1 );
					// Propagate; outer applications should use the identity transform too
					if ( stack.size() > 0 )
					{
						stack.set( stack.size() - 1, true );
					}
					return nodeCopy;
				}
				else
				{
					// Unmodified tree
					stack.remove( stack.size() - 1 );
					return  node;
				}
			}
		}
	}
	
	
	private TransformationFunction identity;
	private ArrayList<TransformationFunction> xforms;
	
	
	public Transformation(TransformationFunction identity, TransformationFunction xforms[])
	{
		this.identity = identity;
		this.xforms = new ArrayList<TransformationFunction>();
		this.xforms.addAll( Arrays.asList( xforms ) );
	}
	
	public Transformation(TransformationFunction identity, PyObject xforms[])
	{
		this.identity = identity;
		this.xforms = new ArrayList<TransformationFunction>();
		for (PyObject x: xforms)
		{
			this.xforms.add( new TransformationFunction.PyTransformationFunction( x ) );
		}
	}
	
	
	public Object apply(Object node)
	{
		TransformationApplication a = new TransformationApplication( identity, xforms );
		return a.apply( node, null );
	}
	
	public Object __call__(Object node)
	{
		return apply( node );
	}
}
