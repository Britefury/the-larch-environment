//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.List;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;
import org.python.core.PyTuple;

import BritefuryJ.DocModel.DMIOReader.BadModuleNameException;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMIOWriter.InvalidDataTypeException;


public abstract class DMNode
{
	public static class CannotChangeNodeClassException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		
		private Class<?> sourceClass, destClass;
		
		
		public CannotChangeNodeClassException(Class<?> sourceClass, Class<?> destClass)
		{
			super( "Cannot change node class from " + destClass.getName() + " to " + sourceClass.getName() );
			this.sourceClass = sourceClass;
			this.destClass = destClass;
		}


		public Class<?> getSourceClass()
		{
			return sourceClass;
		}


		public Class<?> getDestClass()
		{
			return destClass;
		}
	}
	
	
	public static class NodeAlreadyAChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	public static class NodeNotAChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	
	private DMNode parent;
	
	
	public DMNode()
	{
	}
	
	
	
	public PyObject __reduce__()
	{
		return new PyTuple( Py.java2py( getClass() ), new PyTuple(), __getstate__() );
	}
	
	
	public PyObject __getstate__()
	{
		PyObject state;
		try
		{
			state = DMIOWriter.writeAsState( this );
		}
		catch (InvalidDataTypeException e)
		{
			throw new RuntimeException( "InvalidDataTypeException while creating serialised form: " + e.getMessage() );
		}
		return state;
	}
	
	public void __setstate__(PyObject state)
	{
		DMNode node;
		try
		{
			node = (DMNode)DMIOReader.readFromState( state );
		}
		catch (BadModuleNameException e)
		{
			throw new RuntimeException( "BadModuleNameException while creating serialised form: " + e.getMessage() );
		}
		catch (ParseErrorException e)
		{
			throw new RuntimeException( "ParseErrorException while creating serialised form: " + e.getMessage() );
		}
		become( node );
	}
	
	
	
	
	
	public abstract void become(Object x);
	
	
	
	protected static Object deepCopyOf(Object x, PyDictionary memo)
	{
		if ( x instanceof DMNode )
		{
			return Py.tojava( ((DMNode)x).__deepcopy__( memo ), DMNode.class );
		}
		else
		{
			return x;
		}
	}
	
	public PyObject __deepcopy__(PyDictionary memo)
	{
		long id = Py.java_obj_id( this );
		PyObject key = Py.newInteger( id );
		
		PyObject value = memo.get( key );
		if ( value != Py.None )
		{
			return value;
		}
		else
		{
			Object copy = createDeepCopy( memo );
			memo.__setitem__( key, value );
			return Py.java2py( copy );
		}
	}

	protected abstract Object createDeepCopy(PyDictionary memo);
	
	
	public abstract DMNodeClass getDMNodeClass();
	
	
	// Notify of child removals first
	protected void notifyRemoveChild(Object c)
	{
		if ( isRealised()  &&  c instanceof DMNode )
		{
			// Set @c's parent to null. The next step, notifyAddChild() will set it back to this again, if the child is re-added
			((DMNode)c).parent = null;
		}
	}
	
	// Notify of child additions second
	protected void notifyAddChild(Object c)
	{
		if ( isRealised()  &&  c instanceof DMNode )
		{
			((DMNode)c).realiseSubtree( this );
		}
	}
	
	protected void updateChildParentage(Object c)
	{
		if ( isRealised()  &&  c instanceof DMNode )
		{
			DMNode child = (DMNode)c;
			if ( child.parent == null )
			{
				// @child's parent has been set to null by notifyRemoveChild, and has not been re-added
				// We must set the parent back to this again, so that unrealiseSubtree() will unrealise the sub-tree
				child.parent = this;
				child.unrealiseSubtree( this );
			}
		}
	}
	
	private void realiseSubtree(DMNode p)
	{
		if ( parent != p )
		{
			parent = p;
			for (Object child: getChildren())
			{
				if ( child instanceof DMNode )
				{
					((DMNode)child).realiseSubtree( this );
				}
			}
		}
	}
	
	private void unrealiseSubtree(DMNode p)
	{
		if ( parent == p )
		{
			parent = null;
			for (Object child: getChildren())
			{
				if ( child instanceof DMNode )
				{
					((DMNode)child).unrealiseSubtree( this );
				}
			}
		}
	}
	
	
	public boolean isRealised()
	{
		return parent != null;
	}
	

	public void realiseAsRoot()
	{
		if ( !isRealised() )
		{
			realiseSubtree( this );
		}
	}
	
	
	
	public DMNode getParent()
	{
		// Handle the case where @this has been realised as a root
		return parent == this  ?  null  :  parent;
	}
	
	
	public abstract Iterable<Object> getChildren();
	
	
	
	
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
	
	
	
	public static DMEmbeddedObject embed(PyObject x)
	{
		return new DMEmbeddedObject( x );
	}

	public static DMEmbeddedIsolatedObject embedIsolated(PyObject x)
	{
		return new DMEmbeddedIsolatedObject( x );
	}
}
