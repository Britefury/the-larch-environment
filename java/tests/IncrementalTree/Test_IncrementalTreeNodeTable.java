//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.IncrementalTree;

import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.IncrementalTree.IncrementalTreeNodeTable;
import BritefuryJ.IncrementalTree.IncrementalTree.DuplicatePolicy;

public abstract class Test_IncrementalTreeNodeTable extends TestCase
{
	protected DMList da, db, dc, dd;
	protected IncrementalTreeNodeTable table;
	protected IncrementalTreeNode ia, ib, ic, id1, id2;
	protected IncrementalTree tree;
	

	
	protected abstract IncrementalTreeNodeTable createTable();
	protected abstract void refIncrementalNode(IncrementalTreeNode node);
	protected abstract void unrefIncrementalNode(IncrementalTreeNode node);
	
	public void setUp()
	{
		dd = new DMList( Arrays.asList( new Object[] { "d" } ) );
		dc = new DMList( Arrays.asList( new Object[] { dd } ) );
		db = new DMList( Arrays.asList( new Object[] { dd } ) );
		da = new DMList( Arrays.asList( new Object[] { db, dc } ) );
		
		
		tree = new IncrementalTree( da, null, DuplicatePolicy.ALLOW_DUPLICATES );
		
		ia = new IncrementalTreeNode( tree, da, null );
		ib = new IncrementalTreeNode( tree, db, null );
		ic = new IncrementalTreeNode( tree, dc, null );
		id1 = new IncrementalTreeNode( tree, dd, null );
		id2 = new IncrementalTreeNode( tree, dd, null );
		
		
		table = createTable();

		table.put( da, ia );
		table.put( db, ib );
		table.put( dc, ic );
		table.put( dd, id1 );
		table.put( dd, id2 );
	}
	
	public void tearDown()
	{
		da = db = dc = dd = null;
		
		table = null;

		tree = null;
		ia = ib = ic = id1 = id2 = null;
	}
}
