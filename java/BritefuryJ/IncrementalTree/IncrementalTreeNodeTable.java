//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalTree;

import java.lang.ref.WeakReference;
import java.util.Collection;

public abstract class IncrementalTreeNodeTable
{
	protected static class Key
	{
		private WeakReference<Object> node;
		private int hash;
		
		
		public Key(Object node)
		{
			this.node = new WeakReference<Object>( node );
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
	
	
	

	public abstract IncrementalTreeNode getUnrefedIncrementalNodeFor(Object docNode, IncrementalTreeNode.NodeResultFactory resultFactory);
	public abstract Collection<IncrementalTreeNode> get(Object docNode);
	public abstract boolean containsKey(Object docNode);
	public abstract int size();
	public abstract int getNumDocNodes();
	public abstract int getNumIncrementalNodesForDocNode(Object docNode);
	public abstract int getNumUnrefedIncrementalNodesForDocNode(Object docNode);

	public abstract void clean();
	protected abstract void refIncrementalNode(IncrementalTreeNode node);
	protected abstract void unrefIncrementalNode(IncrementalTreeNode node);
}
