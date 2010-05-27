//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.DebugParseResultView;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.HashMap;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.ScrolledViewport;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.DebugParseResultInterface;

public class ParseView implements FragmentContext
{
	private static class ParseViewInteractor extends ElementInteractor
	{
		private ParseView parseView;
		
		public ParseViewInteractor(ParseView parseView)
		{
			this.parseView = parseView;
		}
		
		
		
		public void drawBackground(DPElement element, Graphics2D graphics)
		{
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
				DPElement wa = a.getNodeElement();
				DPElement wb = b.getNodeElement();
				
				Vector2 sizeA = wa.getSize();
				Point2 p1 = wa.getLocalPointRelativeTo( parseView.viewBin, new Point2( sizeA.x, sizeA.y * 0.5 ) );
				Point2 p4 = wb.getLocalPointRelativeTo( parseView.viewBin, new Point2( 0.0, wb.getHeight() * 0.5 ) );
				
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
	
	
	
	public static interface ParseViewListener
	{
		public void onSelectionChanged(DebugNode selection);
	}
	
	private DPElement element; 
	private ScrolledViewport viewport;
	private DPBin viewBin;
	private HashMap<DebugNode, NodeView> nodeTable;
	private ArrayList<Edge> callEdges, memoEdges;
	private NodeView root;
	private NodeView selection;
	private ParseViewListener listener;
	
	
	
	
	private ParseView(DebugParseResultInterface result, PersistentState viewportState)
	{
		selection = null;
		
		nodeTable = new HashMap<DebugNode, NodeView>();
		callEdges = new ArrayList<Edge>();
		memoEdges = new ArrayList<Edge>();
		
		root = buildNodeView( result.getDebugNode() );
		
		root.registerEdges();
		
		viewBin = PrimitiveStyleSheet.instance.bin( root.getElement() );
		viewBin.addInteractor( new ParseViewInteractor( this ) );
		
		viewport = ControlsStyleSheet.instance.aspectRatioScrolledViewport( viewBin, 0.0, 1.333, viewportState );
		element = viewportBorderStyle.border( viewport.getElement().alignHExpand().alignVExpand() ).alignHExpand().alignVExpand();
	}
	
	
	
	public void setListener(ParseViewListener listener)
	{
		this.listener = listener;
	}
	
	
	
	protected NodeView buildNodeView(DebugNode node)
	{
		NodeView view = nodeTable.get( node );
		
		if ( view == null )
		{
			view = new NodeView( this, node );
			nodeTable.put( node, view );
		}
		else
		{
			throw new RuntimeException( "View for debug node " + node + " already built" );
		}
		
		return view;
	}
	

	protected NodeView getNodeView(DebugNode node)
	{
		return nodeTable.get( node );
	}
	

	protected void addCallEdge(NodeView a, NodeView b)
	{
		callEdges.add( new Edge( this, a, b ) );
	}

	protected void addMemoEdge(NodeView a, NodeView b)
	{
		memoEdges.add( new Edge( this, a, b ) );
	}
	
	
	
	protected void onNodeSelected(NodeView node)
	{
		if ( node != selection )
		{
			if ( selection != null )
			{
				selection.unselect();
			}
			
			selection = node;
			DebugNode d = null;

			if ( selection != null )
			{
				selection.select();
				d = selection.getDebugNode();
			}
			
			
			if ( listener != null )
			{
				listener.onSelectionChanged( d );
			}
		}
	}
	
	
	
	public static DPElement presentDebugParseResult(DebugParseResultInterface x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		ParseView v = new ParseView( x, ctx.persistentState( "viewport " ) );
		return v.element.alignHExpand().alignVExpand();
	}



	private final static PrimitiveStyleSheet viewportBorderStyle = PrimitiveStyleSheet.instance.withBorder( new SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.black, null ) );
}
