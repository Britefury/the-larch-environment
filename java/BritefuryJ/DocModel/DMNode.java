//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;

import BritefuryJ.DocTree.DocTreeNode;


public abstract class DMNode
{
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
			return node.parents.toArray();
		}

		public <T> T[] toArray(T[] a)
		{
			return node.parents.toArray( a );
		}
	
	
	
		public int __len__()
		{
			return node.parents.size();
		}
		
		public int count(Object x)
		{
			return contains( x )  ?  1  :  0;
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
	};

	public static class NodeNotAChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	};

	
	
	protected ArrayList<WeakReference<DMNode>> parents;
	
	
	public DMNode()
	{
		parents = new ArrayList<WeakReference<DMNode>>();
	}
	
	
	
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
	
	public ParentListAccessor getParents()
	{
		return new ParentListAccessor( this );
	}
	
	protected void onParentListModified()
	{
	}
	
	protected void onParentListCleaned()
	{
	}
	
	
	
	
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
