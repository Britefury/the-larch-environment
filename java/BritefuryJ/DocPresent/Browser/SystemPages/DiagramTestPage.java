//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPDiagram;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Diagram.DiagramNode;
import BritefuryJ.DocPresent.Diagram.GroupNode;
import BritefuryJ.DocPresent.Diagram.ShapeNode;
import BritefuryJ.DocPresent.Input.DndDrop;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DiagramTestPage extends SystemPage
{
	private static StaticTextStyleSheet textStyle = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
	private static Border destBorder = new EmptyBorder( 10.0, 10.0, 10.0, 10.0, new Color( 1.0f, 0.9f, 0.75f ) );


	protected DiagramTestPage()
	{
		register( "tests.diagram" );
	}
	
	
	protected String getTitle()
	{
		return "Diagram test";
	}
	
	
	protected DndHandler dndSource(int index)
	{
		final String dragData = new Integer( index ).toString();
		DndHandler sourceHandler = new DndHandler()
		{
			public int getSourceRequestedAction(PointerInputElement sourceElement, PointerInterface pointer, int button)
			{
				return COPY;
			}

			public Transferable createTransferable(PointerInputElement sourceElement)
			{
				return new StringSelection( dragData );
			}

			public void exportDone(PointerInputElement sourceElement, Transferable data, int action)
			{
			}
		};
		return sourceHandler;
	}
	
	
	
	protected DiagramNode createMinuteTick(int index)
	{
		Rectangle2D.Double shape = new Rectangle2D.Double( -4.0, 0.0, 8.0, 20.0 );
		return new ShapeNode( shape ).fillPaint( new Color( 144, 155, 196 ) ).hoverHighlight( new ShapeNode( shape ).fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) ).translate( 0.0, -220.0 );
	}
	
	protected DiagramNode create5MinuteTick(int index)
	{
		Rectangle2D.Double shape = new Rectangle2D.Double( -5.0, 0.0, 10.0, 30.0 );
		return new ShapeNode( shape ).fillPaint( new Color( 142, 184, 196 ) ).hoverHighlight( new ShapeNode( shape ).fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) ).translate( 0.0, -220.0 );
	}
	
	protected DiagramNode create15MinuteTick(int index)
	{
		Rectangle2D.Double shape = new Rectangle2D.Double( -6.0, 0.0, 12.0, 48.0 );
		return new ShapeNode( shape ).fillPaint( new Color( 155, 185, 171 ) ).hoverHighlight( new ShapeNode( shape ).fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) ).translate( 0.0, -220.0 );
	}
	
	protected DiagramNode createTicks4Minutes(int index)
	{
		ArrayList<DiagramNode> ticks = new ArrayList<DiagramNode>();
		double angle = 0.0;
		double deltaAngle = 6.0;
		for (int i = 0; i < 4; i++)
		{
			ticks.add( createMinuteTick( index + i ).rotateDegrees( angle ) );
			angle += deltaAngle;
		}
		return new GroupNode( ticks );
	}
	
	protected DiagramNode createTicks5Minutes(int index)
	{
		ArrayList<DiagramNode> ticks = new ArrayList<DiagramNode>();
		ticks.add( create5MinuteTick( index ) );
		ticks.add( createTicks4Minutes( index + 1 ).rotateDegrees( 6.0 ) );
		return new GroupNode( ticks );
	}
	
	protected DiagramNode createTicks15Minutes(int index)
	{
		ArrayList<DiagramNode> ticks = new ArrayList<DiagramNode>();
		ticks.add( create15MinuteTick( index ) );
		ticks.add( createTicks4Minutes( index + 1 ).rotateDegrees( 6.0 ) );
		ticks.add( createTicks5Minutes( index + 5 ).rotateDegrees( 30.0 ) );
		ticks.add( createTicks5Minutes( index + 10 ).rotateDegrees( 60.0 ) );
		return new GroupNode( ticks );
	}
	
	protected DiagramNode createClockFace()
	{
		ArrayList<DiagramNode> ticks = new ArrayList<DiagramNode>();
		ticks.add( createTicks15Minutes( 0 ).rotateDegrees( 0.0 ) );
		ticks.add( createTicks15Minutes( 15 ).rotateDegrees( 90.0 ) );
		ticks.add( createTicks15Minutes( 30 ).rotateDegrees( 180.0 ) );
		ticks.add( createTicks15Minutes( 45 ).rotateDegrees( 270.0 ) );
		return new GroupNode( ticks );
	}

	
	protected DPWidget makeDestElement(String title)
	{
		DPStaticText destText = new DPStaticText( textStyle, title );
		DPBorder destBorderElement = new DPBorder( destBorder );
		destBorderElement.setChild( destText );
		
		DndHandler destHandler = new DndHandler()
		{
			public boolean canDrop(PointerInputElement destElement, DndDrop drop)
			{
				return true;
			}

			public boolean acceptDrop(PointerInputElement destElement, DndDrop drop)
			{
				Transferable x = drop.getTransferable();
				String text = null;
				try
				{
					text = (String)x.getTransferData( DataFlavor.stringFlavor );
				}
				catch (UnsupportedFlavorException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				((DPStaticText)((DPBorder)destElement).getChild()).setText( text );
				return true;
			}
		};
		
		destBorderElement.enableDnd( destHandler );

		return destBorderElement;
	}

	
	protected DPWidget createContents()
	{
		StaticTextStyleSheet instructionsStyle = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.BLACK );
		DPStaticText instructions = new DPStaticText( instructionsStyle, "Drag clock ticks to the drop box below." );

		DPDiagram diagramElement = new DPDiagram( createClockFace().translate( 320.0, 240.0 ), 640.0, 480.0, false, false );
		Border b = new SolidBorder( 1.0, 3.0, 2.0, 2.0, Color.black, null );
		DPBorder border = new DPBorder( b );
		border.setChild( diagramElement );
		
		DPWidget dest0 = makeDestElement( "Number" );

		VBoxStyleSheet vboxS = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 20.0, false, 0.0 );
		DPVBox vbox = new DPVBox( vboxS );
		vbox.append( instructions );
		vbox.append( border );
		vbox.append( dest0 );

		return vbox;
	}
}
