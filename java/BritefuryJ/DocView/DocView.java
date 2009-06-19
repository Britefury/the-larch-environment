//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import java.util.Arrays;

import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.DocTree.DocTree;
import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.Utils.Profile.ProfileTimer;

public class DocView implements DVNode.NodeRefreshListener
{
	public interface RootNodeInitialiser
	{
		public void initRootNode(DVNode rootView, Object rootDocNode);
	}
	
	public interface RefreshListener
	{
		public void onViewRequestRefresh(DocView view);
	}
	
	
	private DocTreeNode root;
	private RootNodeInitialiser rootNodeInitialiser;
	protected DocViewNodeTable nodeTable;
	private DVNode rootView;
	private DVNode.NodeElementChangeListener elementChangeListener;
	private boolean bRefreshRequired;
	private RefreshListener refreshListener;
	private VBoxElement rootBox;
	
	
	private boolean bProfilingEnabled;
	private ProfileTimer pythonTimer, javaTimer, elementTimer, contentChangeTimer, updateNodeElementTimer;
	
	private static VBoxStyleSheet rootBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, true, 0.0 );
	
	
	
	
	
	
	
	
	public DocView(DocTree tree, DocTreeNode root, RootNodeInitialiser rootNodeInitialiser)
	{
		this.root = root;
		this.rootNodeInitialiser = rootNodeInitialiser;
		
		nodeTable = new DocViewNodeTable();
		
		elementChangeListener = null;
		
		bRefreshRequired = true;
		
		rootBox = null;
		
		bProfilingEnabled = false;
		pythonTimer = new ProfileTimer();
		javaTimer = new ProfileTimer();
		elementTimer = new ProfileTimer();
		contentChangeTimer = new ProfileTimer();
		updateNodeElementTimer = new ProfileTimer();
	}
	
	
	public void setRefreshListener(RefreshListener listener)
	{
		refreshListener = listener;
	}
	
	public void setElementChangeListener(DVNode.NodeElementChangeListener elementChangeListener)
	{
		this.elementChangeListener = elementChangeListener;
	}
	
	
	protected DVNode getRootView()
	{
		if ( rootView == null )
		{
			rootView = buildNodeView( root );
			rootNodeInitialiser.initRootNode( rootView, root );
			rootView.setRefreshListener( this );
		}
		return rootView;
	}
	
	
	public Element getRootViewElement()
	{
		if ( rootBox == null )
		{
			performRefresh();
			DVNode rootView = getRootView();
			rootBox = new VBoxElement( rootBoxStyle );
			rootBox.setChildren( Arrays.asList( new Element[] { rootView.getElement() } ) );
		}
		return rootBox;
	}
	
	
	
	public DVNode buildNodeView(DocTreeNode treeNode)
	{
		if ( treeNode == null )
		{
			return null;
		}
		else
		{
			// Try to get a view node for @treeNode from the node table; one will be returned if a view node exists, else null.
			DVNode viewNode = nodeTable.get( treeNode );
			
			if ( viewNode == null )
			{
				// No view node in the table.
				// Try asking the table for an unused one
				viewNode = nodeTable.takeUnusedViewNodeFor( treeNode );
				
				if ( viewNode == null )
				{
					// No existing view node could be acquired.
					// Create a new one
					viewNode = new DVNode( this, treeNode, elementChangeListener );
					nodeTable.put( treeNode, viewNode );
				}
			}
			
			return viewNode;
		}
	}
	
	
	public DVNode getViewNodeForDocTreeNode(DocTreeNode treeNode)
	{
		return nodeTable.get( treeNode );
	}
	
	
	public DVNode refreshAndGetViewNodeForDocTreeNode(DocTreeNode treeNode)
	{
		refresh();
		return nodeTable.get( treeNode );
	}
	
	
	
	private void performRefresh()
	{
		elementChangeListener.reset( this );
		getRootView().refresh();
		
		// Clear unused entries from the node table
		nodeTable.clearUnused();
	}
	
	
	public void refresh()
	{
		profile_startJava();
		if ( bRefreshRequired )
		{
			bRefreshRequired = false;
			performRefresh();
		}
		profile_stopJava();
	}
	

	
	public void onNodeRequestRefresh(DVNode node)
	{
		if ( !bRefreshRequired )
		{
			bRefreshRequired = true;
			
			if ( refreshListener != null )
			{
				refreshListener.onViewRequestRefresh( this );
			}
		}
	}
	
	
	
	
	
	public void profile_startPython()
	{
		if ( bProfilingEnabled )
		{
			pythonTimer.start();
		}
	}
	
	public void profile_stopPython()
	{
		if ( bProfilingEnabled )
		{
			pythonTimer.stop();
		}
	}

	
	public void profile_startJava()
	{
		if ( bProfilingEnabled )
		{
			javaTimer.start();
		}
	}
	
	public void profile_stopJava()
	{
		if ( bProfilingEnabled )
		{
			javaTimer.stop();
		}
	}
	
	
	public void profile_startElement()
	{
		if ( bProfilingEnabled )
		{
			elementTimer.start();
		}
	}
	
	public void profile_stopElement()
	{
		if ( bProfilingEnabled )
		{
			elementTimer.stop();
		}
	}
	
	
	public void profile_startContentChange()
	{
		if ( bProfilingEnabled )
		{
			contentChangeTimer.start();
		}
	}
	
	public void profile_stopContentChange()
	{
		if ( bProfilingEnabled )
		{
			contentChangeTimer.stop();
		}
	}
	
	
	
	public void profile_startUpdateNodeElement()
	{
		if ( bProfilingEnabled )
		{
			updateNodeElementTimer.start();
		}
	}
	
	public void profile_stopUpdateNodeElement()
	{
		if ( bProfilingEnabled )
		{
			updateNodeElementTimer.stop();
		}
	}
	
	
	
	
	public void beginProfiling()
	{
		bProfilingEnabled = true;
		pythonTimer.reset();
		javaTimer.reset();
		elementTimer.reset();
		contentChangeTimer.reset();
		updateNodeElementTimer.reset();
	}

	public void endProfiling()
	{
		bProfilingEnabled = false;
	}
	
	public double getPythonTime()
	{
		return pythonTimer.getTime();
	}

	public double getJavaTime()
	{
		return javaTimer.getTime();
	}

	public double getElementTime()
	{
		return elementTimer.getTime();
	}
	
	public double getContentChangeTime()
	{
		return contentChangeTimer.getTime();
	}
	
	public double getUpdateNodeElementTime()
	{
		return updateNodeElementTimer.getTime();
	}
}
