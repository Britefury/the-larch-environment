//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Canvas.DrawingNode;
import BritefuryJ.DocPresent.Canvas.GroupNode;
import BritefuryJ.DocPresent.Canvas.ShapeNode;
import BritefuryJ.DocPresent.Canvas.TextNode;
import BritefuryJ.DocPresent.Combinators.ElementRef;
import BritefuryJ.DocPresent.Combinators.ElementRef.RefSet;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Canvas;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.Math.Point2;

public class CanvasTestPage extends SystemPage
{
	private static StyleSheet2 textStyle = StyleSheet2.instance.withAttr( Primitive.fontSize, 12 );
	private static Color backgroundColour = new Color( 1.0f, 0.9f, 0.75f );
	private static StyleSheet2 backgroundStyle = StyleSheet2.instance.withAttr( Primitive.background, new FillPainter( backgroundColour ) );


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
		final Integer dragData = new Integer( index );
		ObjectDndHandler.SourceDataFn sourceDataFn = new ObjectDndHandler.SourceDataFn()
		{
			public Object createSourceData(PointerInputElement sourceElement, int aspect)
			{
				return dragData;
			}
		};
		
		ObjectDndHandler sourceDndHandler = ObjectDndHandler.instance.withDragSource( new ObjectDndHandler.DragSource( Integer.class, DndHandler.ASPECT_NORMAL, sourceDataFn ) );
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

	
	protected Pres makeDestElement(String title)
	{
		final Pres textElement = textStyle.applyTo( new StaticText( title ) );
		Pres destText = backgroundStyle.applyTo( new Bin( textElement.pad( 10.0, 10.0 ) ) ); 
		
		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				String text = data.toString();
				DPBin bin = (DPBin)destElement;
				DPBorder pad = (DPBorder)bin.getChild();
				DPText textElement = (DPText)pad.getChild();
				textElement.setText( text );
				return true;
			}
		};
		
		return destText.addDropDest( Integer.class, dropFn );
	}

	
	private static int textAsNumber(String text)
	{
		try
		{
			return Integer.parseInt( text );
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}
	
	protected Pres makeDestElement2(String title, final ElementRef firstElem)
	{
		final Pres textElement = textStyle.applyTo( new StaticText( title ) );
		Pres destText = backgroundStyle.applyTo( new Bin( textElement.pad( 10.0, 10.0 ) ) ); 
		final ElementRef.RefSet firstElementSet = firstElem.getRefs();
		
		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				String text = data.toString();
				DPBin bin = (DPBin)destElement;
				DPBorder pad = (DPBorder)bin.getChild();
				DPText textElement = (DPText)pad.getChild();
				textElement.setText( text );
				return true;
			}
		};
		
		ObjectDndHandler.CanDropFn canDropFn = new ObjectDndHandler.CanDropFn()
		{
			public boolean canDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				Iterator<DPElement> iter = firstElementSet.iterator();
				if ( iter.hasNext() )
				{
					DPElement firstElement = iter.next();
					DPBin box = (DPBin)firstElement;
					DPBin pad = (DPBin)box.getChild();
					DPText t = (DPText)pad.getChild();
					String firstText = t.getText();
					int firstNum = textAsNumber( firstText );
					int secondNum = (Integer)data;
					return secondNum >= firstNum;
				}
				else
				{
					System.out.println( "No ref" );
					return false;
				}
			}
		};
		
		return destText.addDropDest( Integer.class, canDropFn, dropFn );
	}

	
	protected DPElement createContents()
	{
		Pres canvas = new Canvas( createClockFace().translate( 320.0, 240.0 ), 640.0, 480.0, false, false );
		Pres diagram = StyleSheet2.instance.withAttr( Primitive.border, new SolidBorder( 1.0, 3.0, 2.0, 2.0, Color.black, null ) ).applyTo( new Border( canvas ) );
		
		Pres dest0 = makeDestElement( "Number" );
		ElementRef dest0Ref = new ElementRef( dest0 );
		Pres dest1 = makeDestElement2( "Number", dest0Ref );

		Pres hbox = StyleSheet2.instance.withAttr( Primitive.hboxSpacing, 20.0 ).applyTo( new HBox( new Pres[] { dest0Ref, dest1 } ) );
		
		return StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 20.0 ).applyTo( new VBox( new Pres[] { diagram, hbox } ) ).present();
	}
}
