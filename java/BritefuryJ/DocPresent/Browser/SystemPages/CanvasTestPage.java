//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPCanvas;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Canvas.DrawingNode;
import BritefuryJ.DocPresent.Canvas.GroupNode;
import BritefuryJ.DocPresent.Canvas.ShapeNode;
import BritefuryJ.DocPresent.Canvas.TextNode;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.SimpleDndHandler;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class CanvasTestPage extends SystemPage
{
	private static StaticTextStyleSheet textStyle = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
	private static Border destBorder = new EmptyBorder( 10.0, 10.0, 10.0, 10.0, new Color( 1.0f, 0.9f, 0.75f ) );


	protected CanvasTestPage()
	{
		register( "tests.canvas" );
	}
	
	
	public String getTitle()
	{
		return "Canvas test";
	}
	
	protected String getDescription()
	{
		return "Drag clock ticks to the drop box below. The second drop box will only accept drops of numbers greater than or equal to the number in the first box.";
	}
	
	
	protected DndHandler dndSource(int index)
	{
		final String dragData = new Integer( index ).toString();
		SimpleDndHandler.SourceDataFn sourceDataFn = new SimpleDndHandler.SourceDataFn()
		{
			public Object createSourceData(PointerInputElement sourceElement)
			{
				return dragData;
			}
		};
		
		SimpleDndHandler sourceDndHandler = new SimpleDndHandler();
		sourceDndHandler.registerSource( "text", sourceDataFn );
		return sourceDndHandler;
	}
	
	
	
	protected DrawingNode createMinuteTick(int index)
	{
		DrawingNode shape = ShapeNode.rectangle( -4.0, 0.0, 8.0, 20.0 );
		DrawingNode tick = shape.fillPaint( new Color( 144, 155, 196 ) ).hoverHighlight( shape.fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) );
		return new GroupNode( new DrawingNode[] { tick, new TextNode( new Integer( index ).toString() ).translate( 0.0, -20.0 ) } ).translate( 0.0, -220.0 );
	}
	
	protected DrawingNode create5MinuteTick(int index)
	{
		DrawingNode shape = ShapeNode.rectangle( -5.0, 0.0, 10.0, 30.0 );
		DrawingNode tick = shape.fillPaint( new Color( 142, 184, 196 ) ).hoverHighlight( shape.fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) );
		return new GroupNode( new DrawingNode[] { tick, new TextNode( new Integer( index ).toString() ).translate( 0.0, -20.0 ) } ).translate( 0.0, -220.0 );
	}
	
	protected DrawingNode create15MinuteTick(int index)
	{
		DrawingNode shape = ShapeNode.rectangle( -6.0, 0.0, 12.0, 48.0 );
		DrawingNode tick = shape.fillPaint( new Color( 155, 185, 171 ) ).hoverHighlight( shape.fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) );
		return new GroupNode( new DrawingNode[] { tick, new TextNode( new Integer( index ).toString() ).translate( 0.0, -20.0 ) } ).translate( 0.0, -220.0 );
	}
	
	protected DrawingNode createTicks4Minutes(int index)
	{
		ArrayList<DrawingNode> ticks = new ArrayList<DrawingNode>();
		double angle = 0.0;
		double deltaAngle = 6.0;
		for (int i = 0; i < 4; i++)
		{
			ticks.add( createMinuteTick( index + i ).rotateDegrees( angle ) );
			angle += deltaAngle;
		}
		return new GroupNode( ticks );
	}
	
	protected DrawingNode createTicks5Minutes(int index)
	{
		ArrayList<DrawingNode> ticks = new ArrayList<DrawingNode>();
		ticks.add( create5MinuteTick( index ) );
		ticks.add( createTicks4Minutes( index + 1 ).rotateDegrees( 6.0 ) );
		return new GroupNode( ticks );
	}
	
	protected DrawingNode createTicks15Minutes(int index)
	{
		ArrayList<DrawingNode> ticks = new ArrayList<DrawingNode>();
		ticks.add( create15MinuteTick( index ) );
		ticks.add( createTicks4Minutes( index + 1 ).rotateDegrees( 6.0 ) );
		ticks.add( createTicks5Minutes( index + 5 ).rotateDegrees( 30.0 ) );
		ticks.add( createTicks5Minutes( index + 10 ).rotateDegrees( 60.0 ) );
		return new GroupNode( ticks );
	}
	
	protected DrawingNode createClockFace()
	{
		ArrayList<DrawingNode> ticks = new ArrayList<DrawingNode>();
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
		
		SimpleDndHandler.DropFn dropFn = new SimpleDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Object data)
			{
				String text = (String)data;
				((DPStaticText)((DPBorder)destElement).getChild()).setText( text );
				return true;
			}
		};
		
		SimpleDndHandler destDndHandler = new SimpleDndHandler();
		destDndHandler.registerDest( "text", dropFn );

		destBorderElement.enableDnd( destDndHandler );

		return destBorderElement;
	}

	
	private static int textAsNumber(String text)
	{
		try
		{
			return new Integer( text ).intValue();
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}
	
	protected DPWidget makeDestElement2(String title, final DPWidget firstElement)
	{
		DPStaticText destText = new DPStaticText( textStyle, title );
		DPBorder destBorderElement = new DPBorder( destBorder );
		destBorderElement.setChild( destText );
		
		SimpleDndHandler.DropFn dropFn = new SimpleDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Object data)
			{
				String text = (String)data;
				((DPStaticText)((DPBorder)destElement).getChild()).setText( text );
				return true;
			}
		};
		
		SimpleDndHandler.CanDropFn canDropFn = new SimpleDndHandler.CanDropFn()
		{
			public boolean canDrop(PointerInputElement destElement, Object data)
			{
				String text = (String)data;
				String firstText = ((DPStaticText)((DPBorder)firstElement).getChild()).getText();
				int firstNum = textAsNumber( firstText );
				int secondNum = textAsNumber( text );
				return secondNum >= firstNum;
			}
		};
		
		SimpleDndHandler destDndHandler = new SimpleDndHandler();
		destDndHandler.registerDest( "text", dropFn, canDropFn );

		destBorderElement.enableDnd( destDndHandler );

		return destBorderElement;
	}

	
	protected DPWidget createContents()
	{
		DPCanvas diagramElement = new DPCanvas( createClockFace().translate( 320.0, 240.0 ), 640.0, 480.0, false, false );
		Border b = new SolidBorder( 1.0, 3.0, 2.0, 2.0, Color.black, null );
		DPBorder border = new DPBorder( b );
		border.setChild( diagramElement );
		
		DPWidget dest0 = makeDestElement( "Number" );
		DPWidget dest1 = makeDestElement2( "Number", dest0 );

		HBoxStyleSheet hboxS = new HBoxStyleSheet( 20.0 );
		DPHBox hbox = new DPHBox( hboxS );
		hbox.append( dest0 );
		hbox.append( dest1 );

		VBoxStyleSheet vboxS = new VBoxStyleSheet( VTypesetting.NONE, 20.0 );
		DPVBox vbox = new DPVBox( vboxS );
		vbox.append( border );
		vbox.append( hbox );

		return vbox;
	}
}
