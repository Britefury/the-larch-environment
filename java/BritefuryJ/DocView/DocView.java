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
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.Utils.Profile.ProfileTimer;

public class DocView extends IncrementalTree implements IncrementalTreeNode.NodeResultChangeListener
{
	//
	//
	// PROFILING
	//
	//
	
	static boolean ENABLE_PROFILING = false;
	static boolean ENABLE_DISPLAY_TREESIZES = false;
	
	
	public interface NodeElementChangeListener
	{
		public void reset(DocView view);
		public void elementChangeFrom(DVNode node, DPWidget e);
		public void elementChangeTo(DVNode node, DPWidget e);
	}
	
	
	private NodeElementChangeListener elementChangeListener;
	private DPVBox rootBox;
	
	
	private boolean bProfilingEnabled;
	private ProfileTimer pythonTimer, javaTimer, elementTimer, contentChangeTimer, updateNodeElementTimer;
	
	private static VBoxStyleParams rootBoxStyle = VBoxStyleParams.defaultStyleParams;
	
	
	
	
	
	
	
	
	public DocView(DMNode root, DVNode.NodeResultFactory rootElementFactory)
	{
		super( root, rootElementFactory, DuplicatePolicy.ALLOW_DUPLICATES );
		elementChangeListener = null;
		
		rootBox = null;
		
		bProfilingEnabled = false;
		pythonTimer = new ProfileTimer();
		javaTimer = new ProfileTimer();
		elementTimer = new ProfileTimer();
		contentChangeTimer = new ProfileTimer();
		updateNodeElementTimer = new ProfileTimer();
	}
	
	
	public void setElementChangeListener(NodeElementChangeListener elementChangeListener)
	{
		this.elementChangeListener = elementChangeListener;
		if ( elementChangeListener != null )
		{
			setNodeResultChangeListener( this );
		}
		else
		{
			setNodeResultChangeListener( null );
		}
	}
	
	
	public DPWidget getRootViewElement()
	{
		if ( rootBox == null )
		{
			performRefresh();
			DVNode rootView = (DVNode)getRootIncrementalTreeNode();
			rootView.getElement().alignHExpand();
			rootView.getElement().alignVExpand();
			rootBox = new DPVBox( rootBoxStyle );
			rootBox.setChildren( Arrays.asList( new DPWidget[] { rootView.getElement() } ) );
		}
		return rootBox;
	}
	
	
	
	protected void performRefresh()
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
		
		
		super.performRefresh();
		
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
		
		if ( ENABLE_DISPLAY_TREESIZES )
		{
			DVNode rootView = (DVNode)getRootIncrementalTreeNode();
			int presTreeSize = rootView.getElement().computeSubtreeSize();
			int numFragments = rootView.computeSubtreeSize();
			System.out.println( "DocView.performRefresh(): presentation tree size=" + presTreeSize + ", # fragments=" + numFragments );
		}
		// <<< PROFILING
	}
	
	
	
	protected IncrementalTreeNode createIncrementalTreeNode(DMNode node, IncrementalTreeNode.NodeResultChangeListener changeListener)
	{
		return new DVNode( this, node, changeListener );
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

	
	

	public void reset(IncrementalTree view)
	{
		elementChangeListener.reset( this );
	}

	public void resultChangeFrom(IncrementalTreeNode node, Object result)
	{
		profile_startContentChange();
		elementChangeListener.elementChangeFrom( (DVNode)node, (DPWidget)result );
		profile_stopContentChange();
	}

	public void resultChangeTo(IncrementalTreeNode node, Object result)
	{
		profile_startContentChange();
		elementChangeListener.elementChangeTo( (DVNode)node, (DPWidget)result );
		profile_stopContentChange();
	}
}
