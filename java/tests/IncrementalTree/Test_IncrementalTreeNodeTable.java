//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.IncrementalTree;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.IncrementalTree.IncrementalTreeNodeTable;
import BritefuryJ.IncrementalTree.IncrementalTree.DuplicatePolicy;

public abstract class Test_IncrementalTreeNodeTable extends TestCase
{
	protected static class TestIncrementalTree extends IncrementalTree
	{
		public TestIncrementalTree(Object root, DuplicatePolicy duplicatePolicy)
		{
			super( root, duplicatePolicy );
		}

		@Override
		protected IncrementalTreeNode createIncrementalTreeNode(Object node)
		{
			return new TestIncrementalTreeNode( this, node );
		}
	}
	
	protected static class TestIncrementalTreeNode extends IncrementalTreeNode
	{
		private Object result;
		
		public TestIncrementalTreeNode(IncrementalTree incrementalTree, Object docNode)
		{
			super( incrementalTree, docNode );
		}

		@Override
		protected Object getResultNoRefresh()
		{
			return result;
		}

		@Override
		protected void updateNodeResult(Object r)
		{
			result = r;
		}
	}
	
	
	protected DMList da, db, dc, dd;
	protected IncrementalTreeNodeTable table;
	protected IncrementalTreeNode ia, ib, ic, id1, id2;
	protected IncrementalTree tree;
	

	
	protected abstract IncrementalTreeNodeTable createTable();
	protected abstract void refIncrementalNode(IncrementalTreeNode node);
	protected abstract void unrefIncrementalNode(IncrementalTreeNode node);
	
	
	protected void assertNodeSetsEqual(Collection<IncrementalTreeNode> x, Collection<IncrementalTreeNode> y)
	{
		HashSet<IncrementalTreeNode> setX = new HashSet<IncrementalTreeNode>();
		HashSet<IncrementalTreeNode> setY = new HashSet<IncrementalTreeNode>();
		setX.addAll( x );
		setY.addAll( y );
		assertEquals( setX, setY );
	}
	
	
	public void setUp()
	{
		dd = new DMList( Arrays.asList( new Object[] { "d" } ) );
		dc = new DMList( Arrays.asList( new Object[] { dd } ) );
		db = new DMList( Arrays.asList( new Object[] { dd } ) );
		da = new DMList( Arrays.asList( new Object[] { db, dc } ) );
		
		
		tree = new TestIncrementalTree( da, DuplicatePolicy.ALLOW_DUPLICATES );
		
		ia = new TestIncrementalTreeNode( tree, da );
		ib = new TestIncrementalTreeNode( tree, db );
		ic = new TestIncrementalTreeNode( tree, dc );
		id1 = new TestIncrementalTreeNode( tree, dd );
		id2 = new TestIncrementalTreeNode( tree, dd );
		
		
		table = createTable();

		refIncrementalNode( ia );
		refIncrementalNode( ib );
		refIncrementalNode( ic );
		refIncrementalNode( id1 );
		refIncrementalNode( id2 );
	}
	
	public void tearDown()
	{
		da = db = dc = dd = null;
		
		table = null;

		tree = null;
		ia = ib = ic = id1 = id2 = null;
	}
}
