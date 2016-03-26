//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DocModel;

import java.math.BigInteger;
import java.util.List;

import BritefuryJ.Isolation.IsolationBarrier;
import BritefuryJ.ModelAccess.DocModel.StringToIntegerReader;
import org.python.core.*;

import BritefuryJ.ClipboardFilter.ClipboardCopyable;
import BritefuryJ.DocModel.DMIOReader.BadModuleNameException;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMIOWriter.InvalidDataTypeException;


public abstract class DMNode implements ClipboardCopyable
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



	protected static class WrappedRef
	{
		protected Object value;

		public WrappedRef(Object value)
		{
			this.value = value;
		}
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
			value = Py.java2py( copy );
			memo.__setitem__( key, value );
			return value;
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
	
	
	
	private static boolean isPrimitive(Object x)
	{
		return x instanceof String  ||  x.getClass().isPrimitive()  ||  x instanceof BigInteger  ||  x instanceof PyComplex;
	}

	@SuppressWarnings("unchecked")
	public static Object coerce(Object x)
	{
		if ( x == null )
		{
			return x;
		}
		else if ( x instanceof DMNode  ||  x instanceof WrappedRef )
		{
			return x;
		}
		else if ( isPrimitive( x ) )
		{
			return x;
		}
		else if ( x instanceof List )
		{
			return new DMList( (List<Object>)x );
		}
		else if ( x instanceof PyJavaType  ||  x instanceof PyObjectDerived )
		{
			Object xx = Py.tojava( (PyObject)x, Object.class );
			if ( xx instanceof PyJavaType  ||  xx instanceof PyObjectDerived )
			{
				System.out.println( "DMNode.coerceForStorage(): Could not unwrap " + x );
				return null;
			}
			else
			{
				return coerce( xx );
			}
		}
		else
		{
			return x;
		}
	}


	@SuppressWarnings("unchecked")
	protected static Object coerceForStorage(Object x)
	{
		if ( x == null )
		{
			return x;
		}
		else if ( x instanceof WrappedRef )
		{
			return ((WrappedRef)x).value;
		}
		else if ( x instanceof DMNode )
		{
			return x;
		}
		else if ( isPrimitive( x ) )
		{
			return x;
		}
		else if ( x instanceof List )
		{
			return new DMList( (List<Object>)x );
		}
		else if ( x instanceof PyJavaType  ||  x instanceof PyObjectDerived )
		{
			Object xx = Py.tojava( (PyObject)x, Object.class );
			if ( xx instanceof PyJavaType  ||  xx instanceof PyObjectDerived )
			{
				System.out.println( "DMNode.coerceForStorage(): Could not unwrap " + x );
				return x;
			}
			else
			{
				return coerceForStorage( xx );
			}
		}
		else
		{
			return x;
		}
	}



	public static WrappedRef reference(Object x)
	{
		return new WrappedRef( x );
	}
	
	
	
	public static WrappedRef embed(PyObject x)
	{
		return reference( x );
	}

	public static IsolationBarrier<PyObject> embedIsolated(PyObject x)
	{
		return new IsolationBarrier<PyObject>( x );
	}
}
