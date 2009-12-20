//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalTree;

import java.lang.ref.WeakReference;
import java.util.List;

import BritefuryJ.DocModel.DMNode;

public abstract class IncrementalTreeNodeTable
{
	protected static class Key
	{
		private WeakReference<DMNode> node;
		private int hash;
		
		
		public Key(DMNode node)
		{
			this.node = new WeakReference<DMNode>( node );
			hash = System.identityHashCode( node );
		}
		
		
		public int hashCode()
		{
			return hash;
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof Key )
			{
				Key kx = (Key)x;
				return node.get() == kx.node.get();
			}
			
			return false;
		}
	}
	
	
	

	public abstract IncrementalTreeNode takeUnusedIncrementalNodeFor(DMNode docNode, IncrementalTreeNode.NodeResultFactory resultFactory);
	public abstract List<IncrementalTreeNode> get(DMNode docNode);
	public abstract void put(DMNode docNode, IncrementalTreeNode viewNode);
	public abstract void remove(IncrementalTreeNode viewNode);
	public abstract boolean containsKey(DMNode docNode);
	public abstract int size();
	public abstract int getNumDocNodes();
	public abstract int getNumIncrementalNodesForDocNode(DMNode docNode);
	public abstract int getNumUnrefedIncrementalNodesForDocNode(DMNode docNode);

	public abstract void clean();
	protected abstract void refIncrementalNode(IncrementalTreeNode node);
	protected abstract void unrefIncrementalNode(IncrementalTreeNode node);
}
