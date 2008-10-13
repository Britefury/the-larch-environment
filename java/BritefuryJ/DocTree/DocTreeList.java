//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocTree;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.python.core.PySlice;

import BritefuryJ.DocModel.DMListInterface;

public class DocTreeList implements DocTreeNode, DMListInterface
{
	public class DocTreeListListIterator implements ListIterator<Object>
	{
		private DocTreeList xs;
		private ListIterator<Object> iter;
		
		private DocTreeListListIterator(DocTreeList xs, ListIterator<Object> iter)
		{
			this.xs = xs;
			this.iter = iter;
		}
		
		
		public boolean hasPrevious()
		{
			return iter.hasPrevious();
		}

		public boolean hasNext()
		{
			return iter.hasNext();
		}

		public Object previous()
		{
			int index = iter.previousIndex();
			return xs.tree.treeNode( iter.previous(), xs, index );
		}

		public Object next()
		{
			int index = iter.nextIndex();
			return xs.tree.treeNode( iter.next(), xs, index );
		}

		public int previousIndex()
		{
			return iter.previousIndex();
		}

		public int nextIndex()
		{
			return iter.nextIndex();
		}


		public void add(Object x)
		{
			iter.add( x );
		}

		public void remove()
		{
			iter.remove();
		}

		public void set(Object x)
		{
			iter.set( x );
		}
	}
	
	
	private DocTree tree; 
	private DMListInterface node;
	private DocTreeNode parentTreeNode;
	private int indexInParent;
	
	
	public DocTreeList(DocTree tree, DMListInterface node, DocTreeNode parentTreeNode, int indexInParent)
	{
		this.tree = tree;
		this.node = node;
		this.parentTreeNode = parentTreeNode;
		this.indexInParent = indexInParent;
	}
	
	
	public Object getNode()
	{
		return node;
	}

	public DocTreeNode getParentTreeNode()
	{
		return parentTreeNode;
	}

	public int getIndexInParent()
	{
		return indexInParent;
	}





	public boolean add(Object x)
	{
		return node.add( x );
	}
	
	public void add(int index, Object x)
	{
		node.add( index, x );
	}
	
	public boolean addAll(Collection<? extends Object> xs)
	{
		return node.addAll( xs );
	}
	
	public boolean addAll(int index, Collection<? extends Object> xs)
	{
		return node.addAll( index, xs );
	}
	

	
	public void clear()
	{
		node.clear();
	}
	
	
	public boolean contains(Object x)
	{
		return node.contains( x );
	}
	

	public boolean containsAll(Collection<?> x)
	{
		return node.containsAll( x );
	}
	
	
	public boolean equals(Object xs)
	{
		return node.equals( xs );
	}
	
	
	public Object get(int index)
	{
		return tree.treeNode( node.get( index ), this, index );
	}
	
	
	public int indexOf(Object x)
	{
		return node.indexOf( x );
	}

	
	public boolean isEmpty()
	{
		return node.isEmpty();
	}
	
	
	public Iterator<Object> iterator()
	{
		return listIterator();
	}
	
	
	public int lastIndexOf(Object x)
	{
		return node.lastIndexOf( x );
	}

	
	public ListIterator<Object> listIterator()
	{
		return new DocTreeListListIterator( this, node.listIterator() );
	}
	
	public ListIterator<Object> listIterator(int i)
	{
		return new DocTreeListListIterator( this, node.listIterator( i ) );
	}
	
	public Object remove(int i)
	{
		return node.remove( i );
	}
	
	public boolean remove(Object x)
	{
		return node.remove( x );
	}
	
	public boolean removeAll(Collection<?> x)
	{
		return node.removeAll( x );
	}
	
	public boolean retainAll(Collection<?> x)
	{
		return node.retainAll( x );
	}
	
	public Object set(int index, Object x)
	{
		return node.set( index, x );
	}
	
	public int size()
	{
		return node.size();
	}
	
	
	
	public List<Object> subList(int fromIndex, int toIndex)
	{
		return node.subList( fromIndex, toIndex );
	}

	public Object[] toArray()
	{
		return node.toArray();
	}

	public <T> T[] toArray(T[] a)
	{
		return node.toArray( a );
	}

	
	
	
	//
	// Python methods
	//
	
	
	public void append(Object x)
	{
		node.append( x );
	}
	
	public void extend(List<Object> xs)
	{
		node.extend( xs );
	}
	
	public void insert(int i, Object x)
	{
		node.insert( i, x );
	}
	
	public Object __getitem__(int i)
	{
		return tree.treeNode( node.__getitem__( i ), this, i );
	}
	
	public List<Object> __getitem__(PySlice i)
	{
		return node.__getitem__( i );
	}
	
	public void __setitem__(int i, Object x)
	{
		node.__setitem__( i, x );
	}
	
	public void __setitem__(PySlice i, List<Object> xs)
	{
		node.__setitem__(  i, xs );
	}
	
	public void __delitem__(int i)
	{
		node.__delitem__( i );
	}

	public void __delitem__(PySlice i)
	{
		node.__delitem__( i );
	}

	public Object pop()
	{
		return node.pop();
	}
	
	public Object pop(int i)
	{
		return node.pop( i );
	}

	public int __len__()
	{
		return node.__len__();
	}
	
	public int index(Object x)
	{
		return node.index( x );
	}
	
	public int index(Object x, int j)
	{
		return node.index( x, j );
	}

	public int index(Object x, int j, int k)
	{
		return node.index( x, j, k );
	}
	
	public int count(Object x)
	{
		return node.count( x );
	}
	
	public DMListInterface __add__(List<Object> xs)
	{
		return node.__add__( xs );
	}
	
	public DMListInterface __mul__(int n)
	{
		return node.__mul__( n );
	}

	public DMListInterface __rmul__(int n)
	{
		return node.__rmul__( n );
	}
}
