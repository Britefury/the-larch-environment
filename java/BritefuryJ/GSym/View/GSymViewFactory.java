//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocTree.DocTree;
import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;

public abstract class GSymViewFactory
{
	public static class CannotCreateViewOfTerminalNode extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public abstract GSymNodeViewFunction createViewFunction();
	public abstract DVNode.NodeElementChangeListener createChangeListener();
	
	public DocView createDocumentView(Object xs, CommandHistory commandHistory) throws CannotCreateViewOfTerminalNode
	{
		DocTree tree = new DocTree();
		Object txs = tree.treeNode( xs );
		if ( txs instanceof DocTreeNode )
		{
			GSymViewInstance viewInstance = new GSymViewInstance( tree, (DocTreeNode)txs, this, commandHistory, createChangeListener() );
			return viewInstance.getView();
		}
		else
		{
			throw new CannotCreateViewOfTerminalNode();
		}
	}
}
