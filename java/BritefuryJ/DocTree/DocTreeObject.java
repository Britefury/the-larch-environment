//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocTree;

import java.util.HashMap;
import java.util.Map;

import org.python.core.Py;

import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectInterface;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;

public class DocTreeObject extends DocTreeNode implements DMObjectInterface
{
	private DocTree tree; 
	private DMObjectInterface node;
	private DocTreeNode parentTreeNode;
	private int indexInParent;
	
	
	public DocTreeObject(DocTree tree, DMObjectInterface node, DocTreeNode parentTreeNode, int indexInParent)
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

	
	
	
	public DMObjectClass getDMClass()
	{
		return node.getDMClass();
	}
	
	public boolean isInstanceOf(DMObjectClass cls)
	{
		return node.isInstanceOf( cls );
	}
	
	public int getFieldIndex(String key)
	{
		return node.getFieldIndex( key );
	}
	
	
	public int indexOfById(Object x)
	{
		return node.indexOfById( DocTreeNode.coerce( x ) );
	}
	
	public Object get(int index)
	{
		return tree.treeNode( node.get( index ), this, index );
	}
	
	public Object get(String key) throws InvalidFieldNameException
	{
		DMObjectClass objClass = node.getDMClass();
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
			throw new InvalidFieldNameException( key );
		}
		else
		{
			return tree.treeNode( node.get( index ), this, index );
		}
	}
	
	
	public void set(int index, Object value)
	{
		node.set( index, DocTreeNode.coerce( value ) );
	}
	
	public void set(String key, Object value) throws InvalidFieldNameException
	{
		node.set( key, DocTreeNode.coerce( value ) );
	}
	
	
	public String[] getFieldNames()
	{
		return node.getFieldNames();
	}
	
	public void update(Map<String, Object> table) throws InvalidFieldNameException
	{
		HashMap<String, Object> coercedTable = new HashMap<String, Object>();
		coercedTable.putAll( table );
		for (Map.Entry<String, Object> entry: coercedTable.entrySet())
		{
			entry.setValue( DocTreeNode.coerce( entry.getValue() ) );
		}
		node.update( coercedTable );
	}
	
	public void become(DMObject obj)
	{
		node.become( obj );
	}
	
	public void become(DocTreeObject obj)
	{
		node.become( (DMObject)obj.getNode() );
	}

	
	
	public Object __getitem__(int index)
	{
		return tree.treeNode( node.__getitem__( index ), this, index );
	}

	public Object __getitem__(String key)
	{
		DMObjectClass objClass = node.getDMClass();
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
		        throw Py.KeyError( key );
		}
		else
		{
			return tree.treeNode( node.__getitem__( index ), this, index );
		}
	}

	
	public void __setitem__(int fieldIndex, Object value)
	{
		node.__setitem__( fieldIndex, DocTreeNode.coerce( value ) );
	}

	public void __setitem__(String key, Object value)
	{
		node.__setitem__( key, DocTreeNode.coerce( value ) );
	}
}
