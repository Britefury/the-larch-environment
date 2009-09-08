//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import java.util.Arrays;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.Utils.Profile.ProfileTimer;

public class DocView implements DVNode.NodeRefreshListener
{
	//
	//
	// PROFILING
	//
	//
	
	static boolean ENABLE_PROFILING = false;
	
	
	public interface RefreshListener
	{
		public void onViewRequestRefresh(DocView view);
	}
	
	
	private DMNode root;
	private DVNode.NodeElementFactory rootElementFactory;
	protected DocViewNodeTable nodeTable;
	private DVNode rootView;
	private DVNode.NodeElementChangeListener elementChangeListener;
	private boolean bRefreshRequired;
	private RefreshListener refreshListener;
	private DPVBox rootBox;
	
	
	private boolean bProfilingEnabled;
	private ProfileTimer pythonTimer, javaTimer, elementTimer, contentChangeTimer, updateNodeElementTimer;
	
	private static VBoxStyleSheet rootBoxStyle = VBoxStyleSheet.defaultStyleSheet;
	
	
	
	
	
	
	
	
	public DocView(DMNode root, DVNode.NodeElementFactory rootElementFactory)
	{
		this.root = root;
		this.rootElementFactory = rootElementFactory;
		
		nodeTable = new DocViewNodeTable();
		
		elementChangeListener = null;
		
		bRefreshRequired = false;
		
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
			rootView = buildNodeView( root, rootElementFactory );
			rootView.setRefreshListener( this );
		}
		return rootView;
	}
	
	
	public DPWidget getRootViewElement()
	{
		if ( rootBox == null )
		{
			performRefresh();
			DVNode rootView = getRootView();
			rootView.getElement().alignHExpand();
			rootView.getElement().alignVExpand();
			rootBox = new DPVBox( rootBoxStyle );
			rootBox.setChildren( Arrays.asList( new DPWidget[] { rootView.getElement() } ) );
		}
		return rootBox;
	}
	
	
	
	public DVNode buildNodeView(DMNode node, DVNode.NodeElementFactory elementFactory)
	{
		if ( node == null )
		{
			return null;
		}
		else
		{
			// Try asking the table for an unused view node for the document node
			DVNode viewNode = nodeTable.takeUnusedViewNodeFor( node, elementFactory );
			
			if ( viewNode == null )
			{
				// No existing view node could be acquired.
				// Create a new one and add it to the table
				viewNode = new DVNode( this, node, elementChangeListener );
				nodeTable.put( node, viewNode );
			}
			
			viewNode.setNodeElementFactory( elementFactory );
			
			return viewNode;
		}
	}
	
	
	
	
	private void performRefresh()
	{
		// >>> PROFILING
		long t1 = 0;
		if ( ENABLE_PROFILING )
		{
			t1 = System.nanoTime();
			ProfileTimer.initProfiling();
			beginProfiling();
		}

		
		profile_startJava();
		// <<< PROFILING
		
		
		elementChangeListener.reset( this );
		getRootView().refresh();
		
		// Clear unused entries from the node table
		nodeTable.clean();

	
		// >>> PROFILING
		profile_stopJava();
	
	
		if ( ENABLE_PROFILING )
		{
			long t2 = System.nanoTime();
			endProfiling();
			ProfileTimer.shutdownProfiling();
			double deltaT = ( t2 - t1 )  *  1.0e-9;
			System.out.println( "DocView: REFRESH VIEW TIME = " + deltaT );
			System.out.println( "DocView: REFRESH VIEW PROFILE: JAVA TIME = " + getJavaTime() + ", ELEMENT CREATE TIME = " + getElementTime() +
					", PYTHON TIME = " + getPythonTime() + ", CONTENT CHANGE TIME = " + getContentChangeTime() +
					", UPDATE NODE ELEMENT TIME = " + getUpdateNodeElementTime() );
		}
		// <<< PROFILING
	}
	
	
	public void refresh()
	{
		if ( bRefreshRequired )
		{
			bRefreshRequired = false;
			performRefresh();
		}
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
