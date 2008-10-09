//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocTree;

public class DocTreeString implements DocTreeNode
{
	private String node;
	private DocTreeNode parentTreeNode;
	private int indexInParent;
	
	
	public DocTreeString(String node, DocTreeNode parentTreeNode, int indexInParent)
	{
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
	
	
	public String toString()
	{
		return node;
	}
}
