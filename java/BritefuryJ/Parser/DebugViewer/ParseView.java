//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.DebugViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Parser.DebugNode;
import BritefuryJ.Parser.DebugParseResult;

public class ParseView
{
	private static class DPViewBin extends DPBin
	{
		private ParseView parseView;
		
		public DPViewBin(ContainerStyleSheet styleSheet, ParseView parseView)
		{
			super( styleSheet );
			
			this.parseView = parseView;
		}
		
		
		
		protected void drawBackground(Graphics2D graphics)
		{
			super.drawBackground( graphics );
			
			graphics.setColor( Color.black );
			for (Edge e: parseView.callEdges)
			{
				e.draw( graphics );
			}

			graphics.setColor( new Color( 0.0f, 0.5f, 0.0f ) );
			for (Edge e: parseView.memoEdges)
			{
				e.draw( graphics );
			}
		}
	}
	
	private static class Edge
	{
		private ParseView parseView;
		public NodeView a, b;
		public CubicCurve2D.Double curve;
		
		
		public Edge(ParseView parseView, NodeView a, NodeView b)
		{
			this.parseView = parseView;
			this.a = a;
			this.b = b;
		}
		
		
		private void refreshCurve()
		{
			if ( curve == null )
			{
				DPWidget wa = a.getNodeWidget();
				DPWidget wb = b.getNodeWidget();
				
				Point2 p1 = wa.getLocalPointRelativeTo( parseView.bin, new Point2( wa.getAllocation().x, wa.getAllocation().y * 0.5 ) );
				Point2 p4 = wb.getLocalPointRelativeTo( parseView.bin, new Point2( 0.0, wb.getAllocation().y * 0.5 ) );
				
				// dx is the x-difference from p1 to p4, clamped to >=20, divided by 3; space the control points evenly
				double dx = Math.max( Math.abs( p4.x - p1.x ), 20.0 )  /  3.0;
				
				if ( p4.x > p1.x )
				{
					double deltaX = p4.x - p1.x;
					double deltaY = p4.y - p1.y;
					
					if ( Math.abs( deltaY )  >  Math.abs( deltaX ) * 5.0 )
					{
						// Scale dx up to avoid almost vertical lines
						dx *= Math.abs( deltaY ) / ( Math.abs( deltaX ) * 5.0 );
					}
				}

				// Compute the y-offsets for p2 and p3
				double dy2 = ( p4.y - p1.y ) * 0.05;
				double dy3 = -dy2;

				// Compute the control points p2 and p3
				Point2 p2 = p1.add( new Vector2( dx, dy2 ) );
				Point2 p3 = p4.add( new Vector2( -dx, dy3 ) );

				curve = new CubicCurve2D.Double( p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y );
			}
		}
		
		
		public void draw(Graphics2D graphics)
		{
			refreshCurve();
			graphics.draw( curve );
		}
	}
	
	private DPPresentationArea area;
	private DPViewBin bin;
	private HashMap<DebugNode, NodeView> nodeTable;
	private Vector<Edge> callEdges, memoEdges;
	private NodeView root;
	
	
	
	
	public ParseView(DebugParseResult result, String input)
	{
		area = new DPPresentationArea();
		
		bin = new DPViewBin( ContainerStyleSheet.defaultStyleSheet, this );
		
		nodeTable = new HashMap<DebugNode, NodeView>();
		callEdges = new Vector<Edge>();
		memoEdges = new Vector<Edge>();
		
		root = getNodeView( result.debugNode, input );
		
		root.registerEdges();
		
		bin.setChild( root.getWidget() );
		area.setChild( bin );
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
		callEdges.add( new Edge( this, a, b ) );
	}

	protected void addMemoEdge(NodeView a, NodeView b)
	{
		memoEdges.add( new Edge( this, a, b ) );
	}
	
	
	
	public void showInFrame()
	{
		JFrame frame = new JFrame( "Parse tree" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
		area.zoomToFit();
	}
}
