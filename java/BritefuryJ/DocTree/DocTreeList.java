//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocTree;

import java.util.List;

import org.python.core.PySlice;

import BritefuryJ.DocModel.DMListInterface;
import BritefuryJ.DocTree.DocTree.NodeTypeNotSupportedException;

public class DocTreeList implements DocTreeNode, DMListInterface
{
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
