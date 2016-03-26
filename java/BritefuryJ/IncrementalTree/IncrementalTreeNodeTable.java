//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.IncrementalTree;

import java.util.Collection;

public abstract class IncrementalTreeNodeTable
{
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
