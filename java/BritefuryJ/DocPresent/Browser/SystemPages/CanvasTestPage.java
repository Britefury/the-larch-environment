//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPLabel;
import BritefuryJ.DocPresent.Canvas.DrawingNode;
import BritefuryJ.DocPresent.Canvas.GroupNode;
import BritefuryJ.DocPresent.Canvas.ShapeNode;
import BritefuryJ.DocPresent.Canvas.TextNode;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Canvas;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class CanvasTestPage extends SystemPage
{
	private static StyleSheet textStyle = StyleSheet.style( Primitive.fontSize.as( 12 ) );
	private static Color backgroundColour = new Color( 1.0f, 0.9f, 0.75f );
	private static StyleSheet backgroundStyle = StyleSheet.style( Primitive.background.as( new FillPainter( backgroundColour ) ) );


	protected CanvasTestPage()
	{
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
		final Integer dragData = index;
		ObjectDndHandler.SourceDataFn sourceDataFn = new ObjectDndHandler.SourceDataFn()
		{
			public Object createSourceData(PointerInputElement sourceElement, int aspect)
			{
				return dragData;
			}
		};
		
		return ObjectDndHandler.instance.withDragSource( new ObjectDndHandler.DragSource( Integer.class, DndHandler.ASPECT_NORMAL, sourceDataFn ) );
	}
	
	
	
	protected DrawingNode createMinuteTick(int index)
	{
		DrawingNode shape = ShapeNode.rectangle( -4.0, 0.0, 8.0, 20.0 );
		DrawingNode tick = shape.fillPaint( new Color( 144, 155, 196 ) ).hoverHighlight( shape.fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) );
		return new GroupNode( new DrawingNode[] { tick, new TextNode( Integer.toString( index ) ).translate( 0.0, -20.0 ) } ).translate( 0.0, -220.0 );
	}
	
	protected DrawingNode create5MinuteTick(int index)
	{
		DrawingNode shape = ShapeNode.rectangle( -5.0, 0.0, 10.0, 30.0 );
		DrawingNode tick = shape.fillPaint( new Color( 142, 184, 196 ) ).hoverHighlight( shape.fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) );
		return new GroupNode( new DrawingNode[] { tick, new TextNode( Integer.toString( index ) ).translate( 0.0, -20.0 ) } ).translate( 0.0, -220.0 );
	}
	
	protected DrawingNode create15MinuteTick(int index)
	{
		DrawingNode shape = ShapeNode.rectangle( -6.0, 0.0, 12.0, 48.0 );
		DrawingNode tick = shape.fillPaint( new Color( 155, 185, 171 ) ).hoverHighlight( shape.fillPaint( new Color( 255, 0, 0 ) ) ).enableDnd( dndSource( index ) );
		return new GroupNode( new DrawingNode[] { tick, new TextNode( Integer.toString( index ) ).translate( 0.0, -20.0 ) } ).translate( 0.0, -220.0 );
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

	

	private static class DataModel
	{
		Integer value = null;
	}
	
	protected Pres makeDestElement(final String title, final DataModel model)
	{
		final ElementRef text = textStyle.applyTo( new Label( title ) ).elementRef();
		Pres dest = backgroundStyle.applyTo( new Bin( text.pad( 10.0, 10.0 ) ) ); 
		
		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				String textValue = data.toString();
				for (DPElement textElem: text.getElements())
				{
					((DPLabel)textElem).setText( textValue );
				}
				model.value = (Integer)data;
				return true;
			}
		};
		
		return dest.withDropDest( Integer.class, dropFn );
	}

	
	protected Pres makeDestElement2(final String title, final DataModel model)
	{
		final ElementRef text = textStyle.applyTo( new Label( title ) ).elementRef();
		Pres dest = backgroundStyle.applyTo( new Bin( text.pad( 10.0, 10.0 ) ) ); 
		
		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				String textValue = data.toString();
				for (DPElement textElem: text.getElements())
				{
					((DPLabel)textElem).setText( textValue );
				}
				return true;
			}
		};
		
		ObjectDndHandler.CanDropFn canDropFn = new ObjectDndHandler.CanDropFn()
		{
			public boolean canDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				Integer firstNum = model.value;
				int secondNum = (Integer)data;
				return secondNum >= firstNum;
			}
		};
		
		return dest.withDropDest( Integer.class, canDropFn, dropFn );
	}

	
	protected Pres createContents()
	{
		Pres canvas = new Canvas( createClockFace().translate( 320.0, 240.0 ), 640.0, 480.0, false, false );
		Pres diagram = new SolidBorder( 1.0, 3.0, 2.0, 2.0, Color.black, null ).surround( canvas );
		
		DataModel model = new DataModel();
		
		Pres dest0 = makeDestElement( "Number", model );
		Pres dest1 = makeDestElement2( "Number", model );

		Pres row = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { dest0, dest1 } ) );
		
		return new Body( new Pres[] { diagram, row.padY( 10.0, 0.0 ) } );
	}
}
