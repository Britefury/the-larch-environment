//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.HashSet;

import org.python.core.PyObject;

import BritefuryJ.AttributeTable.AttributeValues;
import BritefuryJ.Cell.Cell;
import BritefuryJ.Cell.CellEvaluator;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPBox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPShape;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Incremental.IncrementalValue;
import BritefuryJ.Incremental.IncrementalValueListener;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class ControlsStyleSheet extends StyleSheet
{
	private static final Font defaultLinkFont = new Font( "Sans serif", Font.PLAIN, 14 );
	private static final Cursor defaultLinkCursor = new Cursor( Cursor.HAND_CURSOR );
	private static final Painter defaultScrollBarArrowPainter = new FilledOutlinePainter( new Color( 0.7f, 0.85f, 1.0f ), new Color( 0.0f, 0.5f, 1.0f ), new BasicStroke( 1.0f ) );
	private static final Painter defaultScrollBarDragBackgroundPainter = new FilledOutlinePainter( new Color( 0.9f, 0.9f, 0.9f ), new Color( 0.75f, 0.75f, 0.75f ), new BasicStroke( 1.0f ) );
	
	
	public static final ControlsStyleSheet instance = new ControlsStyleSheet();

	
	
	
	
	
	

	
	
	
	public ControlsStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyleSheet", PrimitiveStyleSheet.instance );

		initAttr( "linkAttrs", new AttributeValues( new String[] { "editable", "font", "foreground", "hoverForeground", "cursor" }, new Object[] { false, defaultLinkFont, Color.blue, Color.red, defaultLinkCursor } ) );
		
		initAttr( "buttonBorderThickness", 1.0 );
		initAttr( "buttonMargin", 3.0 );
		initAttr( "buttonRounding", 10.0 );
		initAttr( "buttonBorderPaint", new Color( 0.55f, 0.525f, 0.5f ) );
		initAttr( "buttonBorderHighlightPaint", new Color( 0.0f, 0.5f, 0.5f ) );
		initAttr( "buttonBackgPaint", new Color( 0.85f, 0.85f, 0.85f ) );
		initAttr( "buttonBackgHighlightPaint", new Color( 0.925f, 0.925f, 0.925f ) );
		
		initAttr( "textEntryTextAttrs", new AttributeValues() );
		initAttr( "textEntryBorderThickness", 3.0 );
		initAttr( "textEntryMargin", 3.0 );
		initAttr( "textEntryRounding", 10.0 );
		initAttr( "textEntryBorderPaint", new Color( 0.55f, 0.8f, 0.55f ) );
		initAttr( "textEntryBackgPaint", new Color( 0.9f, 1.0f, 0.9f ) );
		
		initAttr( "scrollBarArrowPainter", defaultScrollBarArrowPainter );
		initAttr( "scrollBarArrowHoverPainter", new FilledOutlinePainter( new Color( 0.8f, 0.9f, 1.0f ), new Color( 0.5f, 0.75f, 1.0f ), new BasicStroke( 1.0f ) ) );
		initAttr( "scrollBarArrowPadding", 1.0 );
		initAttr( "scrollBarArrowFilletSize", 4.0 );
		initAttr( "scrollBarDragBackgroundPainter", defaultScrollBarDragBackgroundPainter );
		initAttr( "scrollBarDragBackgroundHoverPainter", new FilledOutlinePainter( new Color( 0.85f, 0.85f, 0.85f ), new Color( 0.5f, 0.5f, 0.5f ), new BasicStroke( 1.0f ) ) );
		initAttr( "scrollBarDragBoxPainter", defaultScrollBarArrowPainter );
		initAttr( "scrollBarDragBoxHoverPainter", new FilledOutlinePainter( new Color( 0.8f, 0.9f, 1.0f ), new Color( 0.5f, 0.75f, 1.0f ), new BasicStroke( 1.0f ) ) );
		initAttr( "scrollBarDragBoxPadding", 2.0 );
		initAttr( "scrollBarDragBoxRounding", 4.0 );
		initAttr( "scrollBarSize", 24.0 );
	}

	
	protected StyleSheet newInstance()
	{
		return new ControlsStyleSheet();
	}
	

	
	public ControlsStyleSheet withPrimitiveStyleSheet(PrimitiveStyleSheet styleSheet)
	{
		return (ControlsStyleSheet)withAttr( "primitiveStyleSheet", styleSheet );
	}
	
	
	public ControlsStyleSheet withLinkAttrs(AttributeValues attrs)
	{
		return (ControlsStyleSheet)withAttr( "linkAttrs", attrs );
	}
	
	
	public ControlsStyleSheet withButtonBorderThickness(double thickness)
	{
		return (ControlsStyleSheet)withAttr( "buttonBorderThickness", thickness );
	}
	
	public ControlsStyleSheet withButtonBorderMargin(double margin)
	{
		return (ControlsStyleSheet)withAttr( "buttonMargin", margin );
	}
	
	public ControlsStyleSheet withButtonRounding(double rounding)
	{
		return (ControlsStyleSheet)withAttr( "buttonRounding", rounding );
	}
	
	public ControlsStyleSheet withButtonBorderPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "buttonBorderPaint", paint );
	}
	
	public ControlsStyleSheet withButtonBorderHighlightPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "buttonBorderHighlightPaint", paint );
	}
	
	public ControlsStyleSheet withButtonBackgPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "buttonBackgPaint", paint );
	}
	
	public ControlsStyleSheet withButtonBackgHighlightPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "buttonBackgHighlightPaint", paint );
	}
	
	
	public ControlsStyleSheet withTextEntryTextAttrs(AttributeValues attrs)
	{
		return (ControlsStyleSheet)withAttr( "textEntryTextAttrs", attrs );
	}
	
	public ControlsStyleSheet withTextEntryBorderThickness(double thickness)
	{
		return (ControlsStyleSheet)withAttr( "textEntryBorderThickness", thickness );
	}
	
	public ControlsStyleSheet withTextEntryBorderMargin(double margin)
	{
		return (ControlsStyleSheet)withAttr( "textEntryMargin", margin );
	}
	
	public ControlsStyleSheet withTextEntryRounding(double rounding)
	{
		return (ControlsStyleSheet)withAttr( "textEntryRounding", rounding );
	}
	
	public ControlsStyleSheet withTextEntryBorderPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "textEntryBorderPaint", paint );
	}
	
	public ControlsStyleSheet withTextEntryBackgPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "textEntryBackgPaint", paint );
	}
	
	
	public ControlsStyleSheet withScrollBarArrowPainter(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowPainter", painter );
	}
	
	public ControlsStyleSheet withScrollBarArrowHoverPainter(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowHoverPainter", painter );
	}
	
	public ControlsStyleSheet withScrollBarArrowPadding(double padding)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowPadding", padding );
	}
	
	public ControlsStyleSheet withScrollBarArrowFilletSize(double fillet)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowFilletSize", fillet );
	}
	
	public ControlsStyleSheet withScrollBarSize(double size)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarSize", size );
	}
	
	
	
	
	
	
	
	private PrimitiveStyleSheet linkStyleSheet = null;

	private PrimitiveStyleSheet getLinkStyleSheet()
	{
		if ( linkStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues linkAttrs = getNonNull( "linkAttrs", AttributeValues.class, AttributeValues.identity );
			linkStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( linkAttrs );
		}
		return linkStyleSheet;
	}
	
	
	
	private Border buttonBorder = null;

	private Border getButtonBorder()
	{
		if ( buttonBorder == null )
		{
			double thickness = getNonNull( "buttonBorderThickness", Double.class, 1.0 );
			double margin = getNonNull( "buttonMargin", Double.class, 5.0 );
			double rounding = getNonNull( "buttonRounding", Double.class, 10.0 );
			Paint borderPaint = getNonNull( "buttonBorderPaint", Paint.class, Color.black );
			Paint backgPaint = getNonNull( "buttonBackgPaint", Paint.class, new Color( 1.0f, 1.0f, 0.7f ) );

			buttonBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return buttonBorder;
	}
	
	
	
	private Border buttonHighlightBorder = null;

	private Border getButtonHighlightBorder()
	{
		if ( buttonHighlightBorder == null )
		{
			double thickness = getNonNull( "buttonBorderThickness", Double.class, 1.0 );
			double margin = getNonNull( "buttonMargin", Double.class, 5.0 );
			double rounding = getNonNull( "buttonRounding", Double.class, 10.0 );
			Paint borderPaint = getNonNull( "buttonBorderHighlightPaint", Paint.class, Color.black );
			Paint backgPaint = getNonNull( "buttonBackgHighlightPaint", Paint.class, new Color( 1.0f, 1.0f, 0.7f ) );

			buttonHighlightBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return buttonHighlightBorder;
	}
	

	
	private PrimitiveStyleSheet buttonStyleSheet = null;

	private PrimitiveStyleSheet getButtonStyleSheet()
	{
		if ( buttonStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			buttonStyleSheet = primitive.withBorder( getButtonBorder() );
		}
		return buttonStyleSheet;
	}
	
	
	
	private Border textEntryBorder = null;

	private Border getTextEntryBorder()
	{
		if ( textEntryBorder == null )
		{
			double thickness = getNonNull( "textEntryBorderThickness", Double.class, 3.0 );
			double margin = getNonNull( "textEntryMargin", Double.class, 3.0 );
			double rounding = getNonNull( "textEntryRounding", Double.class, 10.0 );
			Paint borderPaint = getNonNull( "textEntryBorderPaint", Paint.class, new Color( 0.55f, 0.8f, 0.55f ) );
			Paint backgPaint = getNonNull( "textEntryBackgPaint", Paint.class, new Color( 0.9f, 1.0f, 0.9f ) );

			textEntryBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return textEntryBorder;
	}
	

	
	private PrimitiveStyleSheet textEntryStyleSheet = null;

	private PrimitiveStyleSheet getTextEntryStyleSheet()
	{
		if ( textEntryStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues textAttrs = getNonNull( "textEntryTextAttrs", AttributeValues.class, AttributeValues.identity );
			textEntryStyleSheet = ((PrimitiveStyleSheet)primitive.withAttrValues( textAttrs )).withBorder( getTextEntryBorder() );
		}
		return textEntryStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet scrollBarArrowStyleSheet = null;
	
	private PrimitiveStyleSheet getScrollBarArrowStyleSheet()
	{
		if ( scrollBarArrowStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Painter arrowPainter = getNonNull( "scrollBarArrowPainter", Painter.class, defaultScrollBarArrowPainter );
			Painter arrowHoverPainter = get( "scrollBarArrowHoverPainter", Painter.class, null );
			scrollBarArrowStyleSheet = primitive.withShapePainter( arrowPainter ).withHoverShapePainter( arrowHoverPainter );
		}
		return scrollBarArrowStyleSheet;
	}
	
	
	
	private Path2D.Double scrollBarArrowPaths[] = null;
	
	private Path2D.Double[] getScrollBarArrowPaths()
	{
		if ( scrollBarArrowPaths == null )
		{
			double scrollBarSize = get( "scrollBarSize", Double.class, 24.0 );
			double arrowPadding = get( "scrollBarArrowPadding", Double.class, 1.0 );
			double arrowFilletSize = get( "scrollBarArrowFilletSize", Double.class, 4.0 );
			double arrowRadius = scrollBarSize * 0.5  -  arrowPadding;
			
			Point2 a = new Point2( 0.0, arrowRadius );
			Point2 b = new Point2( arrowRadius * Math.sin( Math.toRadians( 120.0 ) ), arrowRadius * Math.cos( Math.toRadians( 120.0 ) ) );
			Point2 c = new Point2( arrowRadius * Math.sin( Math.toRadians( 240.0 ) ), arrowRadius * Math.cos( Math.toRadians( 240.0 ) ) );
			
			Path2D.Double arrowShape = DPShape.filletedPath( new Point2[] { a, b, c }, true, arrowFilletSize );
			//Path2D.Double arrowShape = DPShape.path( new Point2[] { a, b, c }, true );
			
			Path2D.Double up = arrowShape;
			Path2D.Double right = (Path2D.Double)arrowShape.clone();
			Path2D.Double down = (Path2D.Double)arrowShape.clone();
			Path2D.Double left = (Path2D.Double)arrowShape.clone();
			
			right.transform( AffineTransform.getQuadrantRotateInstance( 3 ) );
			up.transform( AffineTransform.getQuadrantRotateInstance( 2 ) );
			left.transform( AffineTransform.getQuadrantRotateInstance( 1 ) );
			
			up.transform( AffineTransform.getTranslateInstance( -up.getBounds2D().getMinX(), -up.getBounds2D().getMinY() ) );
			right.transform( AffineTransform.getTranslateInstance( -right.getBounds2D().getMinX(), -right.getBounds2D().getMinY() ) );
			down.transform( AffineTransform.getTranslateInstance( -down.getBounds2D().getMinX(), -down.getBounds2D().getMinY() ) );
			left.transform( AffineTransform.getTranslateInstance( -left.getBounds2D().getMinX(), -left.getBounds2D().getMinY() ) );
			
			scrollBarArrowPaths = new Path2D.Double[] { up, right, down, left }; 
		}
		return scrollBarArrowPaths;
	}
	
	
	private PrimitiveStyleSheet scrollBarDragBackgroundStyleSheet = null;
	
	private PrimitiveStyleSheet getScrollBarDragBackgroundStyleSheet()
	{
		if ( scrollBarDragBackgroundStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Painter backgroundPainter = getNonNull( "scrollBarDragBackgroundPainter", Painter.class, defaultScrollBarDragBackgroundPainter );
			Painter backgroundHoverPainter = get( "scrollBarDragBackgroundHoverPainter", Painter.class, null );
			scrollBarDragBackgroundStyleSheet = primitive.withShapePainter( backgroundPainter ).withHoverShapePainter( backgroundHoverPainter );
		}
		return scrollBarDragBackgroundStyleSheet;
	}
	
	
	
	
	
	public Hyperlink link(String txt, Location targetLocation)
	{
		DPText element = getLinkStyleSheet().staticText( txt );
		return new Hyperlink( element, targetLocation );
	}
	
	public Hyperlink link(String txt, Hyperlink.LinkListener listener)
	{
		DPText element = getLinkStyleSheet().staticText( txt );
		return new Hyperlink( element, listener );
	}
	
	
	
	public Button button(DPElement child, Button.ButtonListener listener)
	{
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener );
	}

	public Button button(DPElement child, PyObject listener)
	{
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener );
	}

	public Button buttonWithLabel(String text, Button.ButtonListener listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		DPElement child = primitive.staticText( text );
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener );
	}

	public Button buttonWithLabel(String text, PyObject listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		DPElement child = primitive.staticText( text );
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener );
	}
	
	
	
	public TextEntry textEntry(String text, TextEntry.TextEntryListener listener)
	{
		PrimitiveStyleSheet textEntryStyle = getTextEntryStyleSheet();
		DPText textElement = textEntryStyle.text( text );
		DPElement line = textEntryStyle.hbox( Arrays.asList( new DPElement[] { textEntryStyle.segment( false, false, textElement ) } ) );
		DPRegion frame = textEntryStyle.region( line );
		DPBorder outerElement = textEntryStyle.border( PrimitiveStyleSheet.instance.vbox( Arrays.asList( new DPElement[] { frame } ) ) );
		return new TextEntry( outerElement, frame, textElement, listener );
	}

	public TextEntry textEntry(String text, PyObject accept, PyObject cancel)
	{
		PrimitiveStyleSheet textEntryStyle = getTextEntryStyleSheet();
		DPText textElement = textEntryStyle.text( text );
		DPElement line = textEntryStyle.hbox( Arrays.asList( new DPElement[] { textEntryStyle.segment( false, false, textElement ) } ) );
		DPRegion frame = textEntryStyle.region( line );
		DPBorder outerElement = textEntryStyle.border( PrimitiveStyleSheet.instance.vbox( Arrays.asList( new DPElement[] { frame } ) ) );
		return new TextEntry( outerElement, frame, textElement, accept, cancel );
	}
	
	
	
	private static class ScrollBarArrowInteractor extends ElementInteractor
	{
		public enum Direction
		{
			INCREASE,
			DECREASE
		};
		
		
		private Direction direction;
		private Range range;
		
		
		public ScrollBarArrowInteractor(Direction direction, Range range)
		{
			this.direction = direction;
			this.range = range;
		}
		
		
		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				if ( direction == Direction.INCREASE )
				{
					range.move( range.getStepSize() );
				}
				else if ( direction == Direction.DECREASE )
				{
					range.move( -range.getStepSize() );
				}
				return true;
			}
			else
			{
				return false;
			}
		}
		
		public boolean onButtonUp(DPElement element, PointerButtonEvent event)
		{
			return event.button == 1;
		}
	}
	
	private static class ScrollBarDragBarInteractor extends ElementInteractor implements IncrementalValueListener
	{
		public enum Direction
		{
			HORIZONTAL,
			VERTICAL
		};
		
		
		private DPElement element;
		private Direction direction;
		private Range range;
		private double padding, rounding;
		private Painter dragBoxPainter, dragBoxHoverPainter;
		private HashSet<PointerInterface> dragBoxHoverPointers = new HashSet<PointerInterface>();
		private PointerInterface dragPointer = null;
		private Point2 dragStartPos = null;
		private double dragStartValue = 0.0;
		private Cell dragBoxCell = new Cell();
		
		
		public ScrollBarDragBarInteractor(DPElement element, Direction direction, Range range, double padding, double rounding, Painter dragBoxPainter, Painter dragBoxHoverPainter)
		{
			this.element = element;
			this.direction = direction;
			this.range = range;
			this.padding = padding;
			this.rounding = rounding;
			this.dragBoxPainter = dragBoxPainter;
			this.dragBoxHoverPainter = dragBoxHoverPainter;
			
			CellEvaluator dragBoxCellEval = new CellEvaluator()
			{
				@Override
				public Object evaluate()
				{
					return computeDragBox( ScrollBarDragBarInteractor.this.element );
				}
			};
			dragBoxCell.setEvaluator( dragBoxCellEval );
			dragBoxCell.addListener( this );
		}
		
		

		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
		{
			if ( event.button == 1 )
			{
				AABox2 dragBox = (AABox2)dragBoxCell.getValue();
				
				Point2 localPos = event.getPointer().getLocalPos();
				
				if ( direction == Direction.HORIZONTAL  &&  localPos.x < dragBox.getLowerX()    ||  direction == Direction.VERTICAL  &&  localPos.y < dragBox.getLowerY() )
				{
					double pageSize = range.getEnd() - range.getBegin();
					range.move( -pageSize );
				}
				else if ( direction == Direction.HORIZONTAL  &&  localPos.x > dragBox.getUpperX()    ||  direction == Direction.VERTICAL  &&  localPos.y > dragBox.getUpperY() )
				{
					double pageSize = range.getEnd() - range.getBegin();
					range.move( pageSize );
				}
				else
				{
					dragPointer = event.getPointer().concretePointer();
					dragStartPos = event.getPointer().getLocalPos();
					dragStartValue = range.getBegin();
				}
				
				return true;
			}
			else
			{
				return false;
			}
		}
		
		public boolean onButtonUp(DPElement element, PointerButtonEvent event)
		{
			dragPointer = null;
			return event.button == 1;
		}
		
		public void onMotion(DPElement element, PointerMotionEvent event)
		{
			AABox2 dragBox = (AABox2)dragBoxCell.getValue();
			if ( dragBox.containsPoint( event.getPointer().getLocalPos() ) )
			{
				boolean bEmptyBefore = dragBoxHoverPointers.isEmpty();
				dragBoxHoverPointers.add( event.getPointer().concretePointer() );
				boolean bEmptyAfter = dragBoxHoverPointers.isEmpty();
				if ( bEmptyBefore != bEmptyAfter )
				{
					element.queueFullRedraw();
				}
			}
			else
			{
				boolean bEmptyBefore = dragBoxHoverPointers.isEmpty();
				dragBoxHoverPointers.remove( event.getPointer().concretePointer() );
				boolean bEmptyAfter = dragBoxHoverPointers.isEmpty();
				if ( bEmptyBefore != bEmptyAfter )
				{
					element.queueFullRedraw();
				}
			}
		}
		
		public void onDrag(DPElement element, PointerMotionEvent event)
		{
			if ( event.getPointer().concretePointer() == dragPointer )
			{
				AABox2 box = element.getLocalAABox();
				Point2 localPos = event.getPointer().getLocalPos();
				Vector2 deltaPos = localPos.sub( dragStartPos );
				
				double visibleRange, delta;
				if ( direction == Direction.HORIZONTAL )
				{
					visibleRange = box.getWidth()  -  padding * 2.0;
					delta = deltaPos.x;
				}
				else if ( direction == Direction.VERTICAL )
				{
					visibleRange = box.getHeight()  -  padding * 2.0;
					delta = deltaPos.y;
				}
				else
				{
					throw new RuntimeException( "Invalid direction" );
				}

				double scaleFactor = ( range.getUpper() - range.getLower() ) / visibleRange;
				range.moveBeginTo( dragStartValue  +  delta * scaleFactor );
			}
		}
		
		public void onEnter(DPElement element, PointerMotionEvent event)
		{
			AABox2 dragBox = (AABox2)dragBoxCell.getValue();
			if ( dragBox.containsPoint( event.getPointer().getLocalPos() ) )
			{
				boolean bEmptyBefore = dragBoxHoverPointers.isEmpty();
				dragBoxHoverPointers.add( event.getPointer().concretePointer() );
				boolean bEmptyAfter = dragBoxHoverPointers.isEmpty();
				if ( bEmptyBefore != bEmptyAfter )
				{
					element.queueFullRedraw();
				}
			}
		}
		
		public void onLeave(DPElement element, PointerMotionEvent event)
		{
			boolean bEmptyBefore = dragBoxHoverPointers.isEmpty();
			dragBoxHoverPointers.remove( event.getPointer().concretePointer() );
			boolean bEmptyAfter = dragBoxHoverPointers.isEmpty();
			if ( bEmptyBefore != bEmptyAfter )
			{
				element.queueFullRedraw();
			}
		}
		
		
		public void drawBackground(DPElement element, Graphics2D graphics)
		{
		}

		public void draw(DPElement element, Graphics2D graphics)
		{
			AABox2 dragBox = (AABox2)dragBoxCell.getValue();
			
			RoundRectangle2D.Double shape = new RoundRectangle2D.Double( dragBox.getLowerX(), dragBox.getLowerY(), dragBox.getWidth(), dragBox.getHeight(), rounding, rounding );
			
			Painter painter;
			if ( dragBoxHoverPainter != null )
			{
				painter = dragBoxHoverPointers.isEmpty()  ?  dragBoxPainter  :  dragBoxHoverPainter;
			}
			else
			{
				painter = dragBoxPainter;
			}
			
			painter.drawShape( graphics, shape );
		}
		
		
		private AABox2 computeDragBox(DPElement element)
		{
			AABox2 box = element.getLocalAABox();
			if ( direction == Direction.HORIZONTAL )
			{
				double visibleRange = box.getWidth()  -  padding * 2.0;
				double scaleFactor = visibleRange / ( range.getUpper() - range.getLower() );
				return new AABox2( padding + range.getBegin() * scaleFactor, padding, padding + range.getEnd() * scaleFactor, box.getUpperY() - padding );
			}
			else if ( direction == Direction.VERTICAL )
			{
				double visibleRange = box.getHeight()  -  padding * 2.0;
				double scaleFactor = visibleRange / ( range.getUpper() - range.getLower() );
				return new AABox2( padding, padding + range.getBegin() * scaleFactor, box.getUpperX() - padding, padding + range.getEnd() * scaleFactor );
			}
			else
			{
				throw new RuntimeException( "Invalid direction" );
			}
		}



		@Override
		public void onIncrementalValueChanged(IncrementalValue inc)
		{
			element.queueFullRedraw();
		}
	}
	
	
	public ScrollBar horizontalScrollBar(Range range)
	{
		PrimitiveStyleSheet arrowStyle = getScrollBarArrowStyleSheet();
		PrimitiveStyleSheet dragBackgroundStyle = getScrollBarDragBackgroundStyleSheet();
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		
		Path2D.Double leftPath = getScrollBarArrowPaths()[3];
		Path2D.Double rightPath = getScrollBarArrowPaths()[1];
		
		double arrowPadding = get( "scrollBarArrowPadding", Double.class, 1.0 );

		double scrollBarSize = get( "scrollBarSize", Double.class, 24.0 );
		Painter scrollBarDragBoxPainter = getNonNull( "scrollBarDragBoxPainter", Painter.class, defaultScrollBarArrowPainter );
		Painter scrollBarDragBoxHoverPainter = get( "scrollBarDragBoxPainter", Painter.class, null );
		double scrollBarDragBoxPadding = get( "scrollBarDragBoxPadding", Double.class, 2.0 );
		double scrollBarDragBoxRounding = get( "scrollBarDragBoxRounding", Double.class, 4.0 );
		
		
		DPShape leftArrow = arrowStyle.shape( leftPath );
		leftArrow.addInteractor( new ScrollBarArrowInteractor( ScrollBarArrowInteractor.Direction.DECREASE, range ) );
		
		DPShape rightArrow = arrowStyle.shape( rightPath );
		rightArrow.addInteractor( new ScrollBarArrowInteractor( ScrollBarArrowInteractor.Direction.INCREASE, range ) );
		
		DPBox dragBar = dragBackgroundStyle.box( 0.0, scrollBarSize );
		dragBar.addInteractor( new ScrollBarDragBarInteractor( dragBar, ScrollBarDragBarInteractor.Direction.HORIZONTAL, range, scrollBarDragBoxPadding, scrollBarDragBoxRounding,
				scrollBarDragBoxPainter, scrollBarDragBoxHoverPainter ) );
		
		DPElement element = primitive.hbox( Arrays.asList( new DPElement[] { leftArrow.pad( arrowPadding, arrowPadding ).alignVCentre(),
				dragBar.alignHExpand().alignVCentre(),
				rightArrow.pad( arrowPadding, arrowPadding ).alignVCentre() } ) );
		
		return new ScrollBar( range, element.alignHExpand() );
	}
	
	public ScrollBar verticalScrollBar(Range range)
	{
		PrimitiveStyleSheet arrowStyle = getScrollBarArrowStyleSheet();
		PrimitiveStyleSheet dragBackgroundStyle = getScrollBarDragBackgroundStyleSheet();
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		
		Path2D.Double upPath = getScrollBarArrowPaths()[0];
		Path2D.Double downPath = getScrollBarArrowPaths()[2];
		
		double arrowPadding = get( "scrollBarArrowPadding", Double.class, 1.0 );

		double scrollBarSize = get( "scrollBarSize", Double.class, 24.0 );
		Painter scrollBarDragBoxPainter = getNonNull( "scrollBarDragBoxPainter", Painter.class, defaultScrollBarArrowPainter );
		Painter scrollBarDragBoxHoverPainter = get( "scrollBarDragBoxPainter", Painter.class, null );
		double scrollBarDragBoxPadding = get( "scrollBarDragBoxPadding", Double.class, 2.0 );
		double scrollBarDragBoxRounding = get( "scrollBarDragBoxRounding", Double.class, 4.0 );
		
		
		DPShape upArrow = arrowStyle.shape( upPath );
		upArrow.addInteractor( new ScrollBarArrowInteractor( ScrollBarArrowInteractor.Direction.DECREASE, range ) );
		
		DPShape downArrow = arrowStyle.shape( downPath );
		downArrow.addInteractor( new ScrollBarArrowInteractor( ScrollBarArrowInteractor.Direction.INCREASE, range ) );
		
		DPBox dragBar = dragBackgroundStyle.box( scrollBarSize, 0.0 );
		dragBar.addInteractor( new ScrollBarDragBarInteractor( dragBar, ScrollBarDragBarInteractor.Direction.VERTICAL, range, scrollBarDragBoxPadding, scrollBarDragBoxRounding,
				scrollBarDragBoxPainter, scrollBarDragBoxHoverPainter ) );
		
		DPElement element = primitive.vbox( Arrays.asList( new DPElement[] { upArrow.pad( arrowPadding, arrowPadding ).alignHCentre(),
				dragBar.alignVExpand().alignHCentre(),
				downArrow.pad( arrowPadding, arrowPadding ).alignHCentre() } ) );
		
		return new ScrollBar( range, element.alignVExpand() );
	}
}
