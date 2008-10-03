//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.DebugViewer;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.Parser.DebugNode;
import BritefuryJ.Parser.DebugParseResult;

public class ParseView
{
	private static class Edge
	{
		public NodeView a, b;
		
		
		public Edge(NodeView a, NodeView b)
		{
			this.a = a;
			this.b = b;
		}
	}
	
	private DPPresentationArea area;
	private HashMap<DebugNode, NodeView> nodeTable;
	private Vector<Edge> callEdges, memoEdges;
	private NodeView root;
	
	
	
	
	public ParseView(DebugParseResult result, String input)
	{
		area = new DPPresentationArea();
		
		nodeTable = new HashMap<DebugNode, NodeView>();
		callEdges = new Vector<Edge>();
		memoEdges = new Vector<Edge>();
		
		root = getNodeView( result.debugNode, input );
		
		root.registerEdges();
		
		area.setChild( root.getWidget() );
		area.disableHorizontalClamping();
	}
	
	
	DPPresentationArea getPresentationArea()
	{
		return area;
	}
	
	
	protected NodeView getNodeView(DebugNode node, String input)
	{
		NodeView view = nodeTable.get( node );
		
		if ( view == null )
		{
			view = new NodeView( this, node, input );
			nodeTable.put( node, view );
		}
		
		return view;
	}
	
	
	protected void addCallEdge(NodeView a, NodeView b)
	{
		callEdges.add( new Edge( a, b ) );
	}

	protected void addMemoEdge(NodeView a, NodeView b)
	{
		memoEdges.add( new Edge( a, b ) );
	}
	
	
	
	public void showInFrame()
	{
		JFrame frame = new JFrame( "Parse tree" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
