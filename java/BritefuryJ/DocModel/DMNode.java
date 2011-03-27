//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;
import org.python.core.PyString;
import org.python.core.PyTuple;

import BritefuryJ.DocModel.DMIOReader.BadModuleNameException;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMIOWriter.InvalidDataTypeException;
import BritefuryJ.DocModel.Resource.DMJavaResource;
import BritefuryJ.DocModel.Resource.DMPyResource;
import BritefuryJ.DocModel.Resource.DMResource;


public abstract class DMNode implements Cloneable
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
	
	
	public static class ParentListIterator implements Iterator<DMNode>
	{
		Iterator<WeakReference<DMNode>> iter;
		
		private ParentListIterator(Iterator<WeakReference<DMNode>> iter)
		{
			this.iter = iter;
		}
		
		
		public boolean hasNext()
		{
			return iter.hasNext();
		}

		public DMNode next()
		{
			return iter.next().get();
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	
	public static class ParentListAccessor implements Collection<DMNode>
	{
		private DMNode node;
		
		
		
		private ParentListAccessor(DMNode node)
		{
			this.node = node;
		}
		
		
		
		public boolean add(DMNode e)
		{
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends DMNode> c)
		{
			throw new UnsupportedOperationException();
		}

		public void clear()
		{
			throw new UnsupportedOperationException();
		}

		public boolean contains(Object o)
		{
			for (WeakReference<DMNode> ref: node.parents)
			{
				if ( o == ref.get() )
				{
					return true;
				}
			}
			return false;
		}

		public boolean containsAll(Collection<?> c)
		{
			for (Object x: c)
			{
				if ( !contains( x ) )
				{
					return false;
				}
			}
			return true;
		}
		
		public boolean isEmpty()
		{
			return node.parents.isEmpty();
		}

		public Iterator<DMNode> iterator()
		{
			return new ParentListIterator( node.parents.iterator() );
		}

		public boolean remove(Object o)
		{
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		public int size()
		{
			return node.parents.size();
		}

		public Object[] toArray()
		{
			Object p[] = new Object[node.parents.size()];
			int i = 0;
			for (WeakReference<DMNode> ref: node.parents)
			{
				p[i++] = ref.get();
			}
			return p;
		}

		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a)
		{
			if ( a.length != node.parents.size() )
			{
				a = (T[])new Object[node.parents.size()];
			}
			int i = 0;
			for (WeakReference<DMNode> ref: node.parents)
			{
				a[i++] = (T)ref.get();
			}
			return a;
		}
	
	
	
		public int __len__()
		{
			return node.parents.size();
		}
		
		public int count(Object x)
		{
			return contains( x )  ?  1  :  0;
		}
		
		
		
		public ArrayList<DMNode> getValidParents()
		{
			ArrayList<DMNode> p = new ArrayList<DMNode>();
			for (WeakReference<DMNode> ref: node.parents)
			{
				DMNode node = ref.get();
				if ( node != null )
				{
					p.add( node );
				}
			}
			return p;
		}
		
		
		
		@SuppressWarnings("unchecked")
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}

			if ( x instanceof Collection<?> )
			{
				Collection<DMNode> cx = (Collection<DMNode>)x;
				
				if ( size() == cx.size() )
				{
					for (DMNode p: cx)
					{
						if ( !contains( p ) )
						{
							return false;
						}
					}
					return true;
				}
				else
				{
					return false;
				}
			}
			
			return false;
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

	
	
	protected ArrayList<WeakReference<DMNode>> parents;
	
	
	public DMNode()
	{
		parents = new ArrayList<WeakReference<DMNode>>();
	}
	
	
	
	protected abstract PyObject getPyFactory();
	
	
	public PyObject __reduce__()
	{
		DMPickleHelper.initialise();
		return new PyTuple( getPyFactory(), new PyTuple(), __getstate__() );
	}
	
	
	public PyObject __getstate__()
	{
		DMPickleHelper.initialise();
		if ( useDMSerialisationForPickling )
		{
			String str;
			try
			{
				str = DMIOWriter.writeAsString( this );
			}
			catch (InvalidDataTypeException e)
			{
				throw new RuntimeException( "InvalidDataTypeException while creating serialised form: " + e.getMessage() );
			}
			return new PyString( str );
		}
		else
		{
			try
			{
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				ObjectOutputStream objOut = new ObjectOutputStream( outStream );
				objOut.writeObject( this );
				return new PyString( new String( outStream.toByteArray(), "ISO-8859-1" ) );
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException( "UnsupportedEncodingException while creating serialised form: " + e.getMessage() );
			}
			catch (IOException e)
			{
				throw new RuntimeException( "IOException while creating serialised form: " + e.getMessage() );
			}
		}
	}
	
	public void __setstate__(PyObject state)
	{
		DMPickleHelper.initialise();
		if ( state instanceof PyString )
		{
			if ( useDMSerialisationForPickling )
			{
				String serialised = state.asString();
				DMNode node;
				try
				{
					node = (DMNode)DMIOReader.readFromString( serialised );
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
			else
			{
				try
				{
					String serialised = state.asString();
					byte bytes[] = serialised.getBytes( "ISO-8859-1" );
					ByteArrayInputStream inStream = new ByteArrayInputStream( bytes );
					ObjectInputStream objIn = new ObjectInputStream( inStream );
					DMNode node = (DMNode)objIn.readObject();
					become( node );
				}
				catch (UnsupportedEncodingException e)
				{
					throw new RuntimeException( "Cannot get UTF-8 encoding: " + e.getMessage() );
				}
				catch (IOException e)
				{
					throw new RuntimeException( "IOException while reading from serialised form: " + e.getMessage() );
				}
				catch (ClassNotFoundException e)
				{
					throw new RuntimeException( "Cannot read object; class not found: " + e.getMessage() );
				}
			}
		}
		else
		{
			throw Py.TypeError( "Pickle state should be a Python string" );
		}
	}
	
	
	public abstract void become(Object x);
	
	
	
	public Object deepCopy()
	{
		return deepCopy( new IdentityHashMap<Object,Object>() );
	}

	public Object deepCopy(IdentityHashMap<Object, Object> memo)
	{
		if ( memo.containsKey( this ) )
		{
			return memo.get( this );
		}
		else
		{
			return createDeepCopy( memo );
		}
	}

	protected abstract Object createDeepCopy(IdentityHashMap<Object, Object> memo);
	
	
	public abstract DMNodeClass getDMNodeClass();
	
	
	protected void addParent(DMNode p)
	{
		boolean bCleaned = false;
		for (int i = parents.size() - 1; i >= 0; i--)
		{
			Object x = parents.get( i ).get();
			if ( x == null )
			{
				parents.remove( i );
				bCleaned = true;
			}
			else if ( p == x )
			{
				throw new NodeAlreadyAChildException();
			}
		}
		parents.add( new WeakReference<DMNode>( p ) );
		onParentListModified();
		if ( bCleaned )
		{
			onParentListCleaned();
		}
	}
	
	protected void removeParent(DMNode p)
	{
		boolean bCleaned = false, bRemoved = false;
		for (int i = parents.size() - 1; i >= 0; i--)
		{
			Object x = parents.get( i ).get();
			if ( x == null )
			{
				parents.remove( i );
				bCleaned = true;
			}
			else if ( p == x )
			{
				parents.remove( i );
				onParentListModified();
				bRemoved = true;
			}
		}
		if ( !bRemoved )
		{
			throw new NodeNotAChildException();
		}
		if ( bCleaned )
		{
			onParentListCleaned();
		}
	}
	
	public ArrayList<DMNode> getValidParents()
	{
		ArrayList<DMNode> p = new ArrayList<DMNode>();
		for (WeakReference<DMNode> ref: parents)
		{
			DMNode node = ref.get();
			if ( node != null )
			{
				p.add( node );
			}
		}
		return p;
	}
	
	public ParentListAccessor getParentsLive()
	{
		return new ParentListAccessor( this );
	}
	
	protected void onParentListModified()
	{
	}
	
	protected void onParentListCleaned()
	{
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
	
	
	
	public static DMResource javaResource(Object x)
	{
		return new DMJavaResource( x );
	}

	public static DMResource pyResource(PyObject x)
	{
		return new DMPyResource( x );
	}

	public static DMResource resource(Object x)
	{
		if ( x instanceof PyObject )
		{
			return pyResource( (PyObject)x );
		}
		else
		{
			return javaResource( x );
		}
	}
	
	
	public static boolean useDMSerialisationForPickling = false;
}
