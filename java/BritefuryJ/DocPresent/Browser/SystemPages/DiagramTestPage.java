//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPDiagram;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Diagram.DiagramNode;
import BritefuryJ.DocPresent.Diagram.GroupNode;
import BritefuryJ.DocPresent.Diagram.ShapeNode;

public class DiagramTestPage extends SystemPage
{
	protected DiagramTestPage()
	{
		register( "tests.diagram" );
	}
	
	
	protected String getTitle()
	{
		return "Diagram test";
	}
	
	protected DiagramNode createMinuteTick()
	{
		Rectangle2D.Double shape = new Rectangle2D.Double( -2.0, -5.0, 4.0, 10.0 );
		return new ShapeNode( shape ).fillPaint( new Color( 144, 155, 196 ) ).translate( 0.0, -200.0 );
	}
	
	protected DiagramNode create5MinuteTick()
	{
		Rectangle2D.Double shape = new Rectangle2D.Double( -3.0, -5.0, 6.0, 16.0 );
		return new ShapeNode( shape ).fillPaint( new Color( 142, 184, 196 ) ).translate( 0.0, -200.0 );
	}
	
	protected DiagramNode create15MinuteTick()
	{
		Rectangle2D.Double shape = new Rectangle2D.Double( -4.0, -5.0, 8.0, 20.0 );
		return new ShapeNode( shape ).fillPaint( new Color( 155, 185, 171 ) ).translate( 0.0, -200.0 );
	}
	
	protected DiagramNode createTicks4Minutes()
	{
		ArrayList<DiagramNode> ticks = new ArrayList<DiagramNode>();
		double angle = 0.0;
		double deltaAngle = 6.0;
		for (int i = 0; i < 4; i++)
		{
			ticks.add( createMinuteTick().rotateDegrees( angle ) );
			angle += deltaAngle;
		}
		return new GroupNode( ticks );
	}
	
	protected DiagramNode createTicks5Minutes()
	{
		ArrayList<DiagramNode> ticks = new ArrayList<DiagramNode>();
		ticks.add( create5MinuteTick() );
		ticks.add( createTicks4Minutes().rotateDegrees( 6.0 ) );
		return new GroupNode( ticks );
	}
	
	protected DiagramNode createTicks15Minutes()
	{
		ArrayList<DiagramNode> ticks = new ArrayList<DiagramNode>();
		ticks.add( create15MinuteTick() );
		ticks.add( createTicks4Minutes().rotateDegrees( 6.0 ) );
		ticks.add( createTicks5Minutes().rotateDegrees( 30.0 ) );
		ticks.add( createTicks5Minutes().rotateDegrees( 60.0 ) );
		return new GroupNode( ticks );
	}
	
	protected DiagramNode createClockFace()
	{
		ArrayList<DiagramNode> ticks = new ArrayList<DiagramNode>();
		ticks.add( createTicks15Minutes().rotateDegrees( 0.0 ) );
		ticks.add( createTicks15Minutes().rotateDegrees( 90.0 ) );
		ticks.add( createTicks15Minutes().rotateDegrees( 180.0 ) );
		ticks.add( createTicks15Minutes().rotateDegrees( 270.0 ) );
		return new GroupNode( ticks );
	}

	protected DPWidget createContents()
	{
		DPDiagram diagramElement = new DPDiagram( createClockFace().translate( 320.0, 240.0 ), 640.0, 480.0, false, false );
		
		Border b = new SolidBorder( 1.0, 3.0, 2.0, 2.0, Color.black, null );
		DPBorder border = new DPBorder( b );
		border.setChild( diagramElement );
		
		return border;
	}
}
