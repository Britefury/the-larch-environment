//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Caret.CaretListener;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.InputTable;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Input.Pointer;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;





public class DPPresentationArea extends DPBin implements CaretListener
{
	public static class CannotGetGraphics2DException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static class InvalidMouseButtonException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	
	
	public static interface UndoListener
	{
		public void onUndo(DPPresentationArea area);
		public void onRedo(DPPresentationArea area);
	}
	
	
	
	
	
	static private class PresentationAreaComponent extends JComponent implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener
	{
		private static final long serialVersionUID = 1L;
		
		public DPPresentationArea area;
		private boolean bShown, bConfigured;
		
		
		public PresentationAreaComponent(DPPresentationArea area)
		{
			super();
			
			this.area = area;
			
			bShown = false;
			bConfigured = false;
			
			addComponentListener( this );
			addMouseListener( this );
			addMouseMotionListener( this );
			addMouseWheelListener( this );
			addKeyListener( this );
			addHierarchyListener( this );
			
			
			setFocusable( true );
			setRequestFocusEnabled( true );
		}
		
		
		public void paint(Graphics g)
		{
			Graphics2D g2;
			try
			{
				g2 = (Graphics2D)g;
			}
			catch (ClassCastException e)
			{
				throw new CannotGetGraphics2DException();
			}

			RenderingHints aa = new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			RenderingHints taa = new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.addRenderingHints( aa );
			g2.addRenderingHints( taa );
			area.exposeEvent( g2, new Rectangle2D.Double( 0.0, 0.0, (double)getWidth(), (double)getHeight()) );
		}
	
		

		public void mousePressed(MouseEvent e)
		{
			area.mouseDownEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		public void mouseReleased(MouseEvent e)
		{
			area.mouseUpEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		
		public void mouseClicked(MouseEvent e)
		{
			switch ( e.getClickCount() )
			{
			case 2:
				area.mouseDown2Event( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
				break;
			case 3:
				area.mouseDown3Event( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
				break;
			default:
				break;
			}
		}

		
		public void mouseMoved(MouseEvent e)
		{
			area.mouseMotionEvent( new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		public void mouseDragged(MouseEvent e)
		{
			area.mouseMotionEvent( new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		public void mouseEntered(MouseEvent e)
		{
			area.mouseEnterEvent( new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		public void mouseExited(MouseEvent e)
		{
			area.mouseLeaveEvent( new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}


		


		public void mouseWheelMoved(MouseWheelEvent e)
		{
			area.mouseWheelEvent( new Point2( (double)e.getX(), (double)e.getY() ), e.getWheelRotation(), e.getUnitsToScroll(), getModifiers( e ) );
		}
		
		
		

		public void keyPressed(KeyEvent e)
		{
			area.keyPressEvent( e, getModifiers( e ) );
		}


		public void keyReleased(KeyEvent e)
		{
			area.keyReleaseEvent( e, getModifiers( e ) );
		}


		public void keyTyped(KeyEvent e)
		{
			area.keyTypedEvent( e, getModifiers( e ) );
		}
		
		

		
		public void componentResized(ComponentEvent e)
		{
			area.configureEvent( new Vector2( (double)getWidth(), (double)getHeight() ) );
			bConfigured = true;
		}

		public void componentMoved(ComponentEvent e)
		{
		}


		public void componentShown(ComponentEvent e)
		{
			sendRealiseEvents();
		}

		public void componentHidden(ComponentEvent e)
		{
			sendRealiseEvents();
		}



		public void hierarchyChanged(HierarchyEvent e)
		{
			sendRealiseEvents();
		}
		
		
		private void initialise()
		{
			if ( !bConfigured )
			{
				area.configureEvent( new Vector2( (double)getWidth(), (double)getHeight() ) );
				bConfigured = true;
			}
		}
		
		
		private void sendRealiseEvents()
		{
			boolean bShownNow = isShowing();
			if ( bShownNow != bShown )
			{
				if ( bShownNow )
				{
					initialise();
					area.realiseEvent();
				}
				else
				{
					area.unrealiseEvent();
				}
				
				bShown = bShownNow;
			}
		}

		
		private static int getButton(MouseEvent e)
		{
			int b = e.getButton();
			
			switch ( b )
			{
			case MouseEvent.BUTTON1:
				return 1;
			case MouseEvent.BUTTON2:
				return 2;
			case MouseEvent.BUTTON3:
				return 3;
			default:
				throw new InvalidMouseButtonException();
			}
		}
		
		private static int getModifiers(InputEvent e)
		{
			int modifiers = 0;
			int m = e.getModifiersEx();
			
			if ( ( m & InputEvent.BUTTON1_DOWN_MASK )  !=  0 )
			{
				modifiers |= Modifier.BUTTON1;
			}
			
			if ( ( m & InputEvent.BUTTON2_DOWN_MASK )  !=  0 )
			{
				modifiers |= Modifier.BUTTON2;
			}
			
			if ( ( m & InputEvent.BUTTON3_DOWN_MASK )  !=  0 )
			{
				modifiers |= Modifier.BUTTON3;
			}
			
			if ( ( m & InputEvent.CTRL_DOWN_MASK )  !=  0 )
			{
				modifiers |= Modifier.CTRL;
			}
			
			if ( ( m & InputEvent.SHIFT_DOWN_MASK )  !=  0 )
			{
				modifiers |= Modifier.SHIFT;
			}
			
			if ( ( m & InputEvent.ALT_DOWN_MASK )  !=  0 )
			{
				modifiers |= Modifier.ALT;
			}
			
			if ( ( m & InputEvent.ALT_GRAPH_DOWN_MASK )  !=  0 )
			{
				modifiers |= Modifier.ALT_GRAPH;
			}
			
			return modifiers;
		}
	}
	
	
	
	private HashMap<PointerInterface, DndDrag> dndTable;
	
	private Point2 windowTopLeftCornerInRootSpace;
	private double rootScaleInWindowSpace;
	private Point2 dragStartPosInWindowSpace;
	private int dragButton;
	
	private Vector2 areaSize;
	
	private Pointer rootSpaceMouse;
	private InputTable inputTable;
	
	private WeakHashMap<StateKeyListener, Object> stateKeyListeners;
	
	private PresentationAreaComponent component;
	
	private Runnable immediateEventDispatcher;
	
	private UndoListener undoListener;
	
	private boolean bAllocationRequired;
	
	private Caret caret;
	private DPContentLeaf currentCaretLeaf;
	
	private boolean bHorizontalClamp;
	
	private boolean bStructureRefreshQueued;
	
	
		
	
	public DPPresentationArea()
	{
		super();
		
		dndTable = new HashMap<PointerInterface, DndDrag>();
		
		presentationArea = this;
		
		windowTopLeftCornerInRootSpace = new Point2();
		rootScaleInWindowSpace = 1.0;
		dragStartPosInWindowSpace = new Point2();
		dragButton = 0;
		
		areaSize = new Vector2();
		
		component = new PresentationAreaComponent( this );

		rootSpaceMouse = new Pointer();
		inputTable = new InputTable( rootSpaceMouse );
		
		stateKeyListeners = new WeakHashMap<StateKeyListener, Object>();
		
		bAllocationRequired = true;
		
		bHorizontalClamp = true;
		
		caret = new Caret();
		caret.setCaretListener( this );
		currentCaretLeaf = null;
		
		bStructureRefreshQueued = false;
	}
	
	
	
	
	public void disableHorizontalClamping()
	{
		bHorizontalClamp = false;
	}
	
	public void enableHorizontalClamping()
	{
		bHorizontalClamp = true;
	}
	
	
	public void setUndoListener(UndoListener l)
	{
		undoListener = l;
	}
	

	public JComponent getComponent()
	{
		return component;
	}

	
	public Caret getCaret()
	{
		return caret;
	}




	
	//
	// Space conversion and navigation methods
	//
	
	public Point2 windowSpaceToRootSpace(Point2 w)
	{
		return w.scale( 1.0 / rootScaleInWindowSpace ).add( windowTopLeftCornerInRootSpace.toVector2() );
	}
	
	public Vector2 windowSpaceToRootSpace(Vector2 w)
	{
		return w.mul( 1.0 / rootScaleInWindowSpace );
	}
	
	public Point2 rootSpaceToWindowSpace(Point2 r)
	{
		return r.sub( windowTopLeftCornerInRootSpace.toVector2() ).scale( rootScaleInWindowSpace );
	}
	
	public Vector2 rootSpaceToWindowSpace(Vector2 r)
	{
		return r.mul( rootScaleInWindowSpace );
	}
	
	
	public double windowSpaceSizeToRootSpace(double w)
	{
		return w / rootScaleInWindowSpace;
	}
	
	public double rootSpaceSizeToWindowSpace(double r)
	{
		return r * rootScaleInWindowSpace;
	}
	
	
	
	public void oneToOne()
	{
		rootScaleInWindowSpace = 1.0;
		queueFullRedraw();
	}
	
	public void reset()
	{
		windowTopLeftCornerInRootSpace = new Point2();
		rootScaleInWindowSpace = 1.0;
		queueFullRedraw();
	}
	
	public void zoomToFit()
	{
		performAllocation();
		
		double ax = allocation.x == 0.0  ?  1.0  :  allocation.x;
		double ay = allocation.y == 0.0  ?  1.0  :  allocation.y;
		
		windowTopLeftCornerInRootSpace = new Point2();
		rootScaleInWindowSpace = Math.min( areaSize.x / ax, areaSize.y / ay );
		rootScaleInWindowSpace = rootScaleInWindowSpace == 0.0  ?  1.0  :  rootScaleInWindowSpace;
		queueFullRedraw();
	}
	
	public void zoom(double zoomFactor, Point2 centreInWindowSpace)
	{
		Point2 centreInRootSpace = windowSpaceToRootSpace( centreInWindowSpace );
		rootScaleInWindowSpace *= zoomFactor;
		Point2 newCentreInRootSpace = windowSpaceToRootSpace( centreInWindowSpace );
		windowTopLeftCornerInRootSpace = windowTopLeftCornerInRootSpace.sub( newCentreInRootSpace.sub( centreInRootSpace ) );

		queueFullRedraw();
	}
	
	public void zoomAboutCentre(double zoomFactor)
	{
		// We want to zoom about the centre of the document, not the top left corner
		zoom( zoomFactor, new Point2( areaSize.mul( 0.5 ) ) );
	}
	
	public void panRootSpace(Vector2 pan)
	{
		windowTopLeftCornerInRootSpace = windowTopLeftCornerInRootSpace.add( pan );
		queueFullRedraw();
	}
	
	public void panWindowSpace(Vector2 pan)
	{
		panRootSpace( windowSpaceToRootSpace( pan ) );
	}
	
	
	
	
	//
	// Immediate event queue methods
	//
	
	public void queueImmediateEvent(Runnable event)
	{
		if ( waitingImmediateEvents.isEmpty()  &&  immediateEventDispatcher == null )
		{
			final DPPresentationArea presArea = this;
			// We will be adding the first event; create the dispatcher and queue it
			immediateEventDispatcher = new Runnable()
			{
				public void run()
				{
					presArea.dispatchQueuedImmediateEvents();
				}
			};
			
			SwingUtilities.invokeLater( immediateEventDispatcher );
		}
		
		if ( !waitingImmediateEvents.contains( event ) )
		{
			waitingImmediateEvents.add( event );
		}
	}

	public void dequeueImmediateEvent(Runnable event)
	{
		if ( waitingImmediateEvents.contains( event ) )
		{
			waitingImmediateEvents.remove( event );
		}
	}
	
	private void dispatchQueuedImmediateEvents()
	{
		emitImmediateEvents();
		immediateEventDispatcher = null;
	}
	
	@SuppressWarnings("unchecked")
	private void emitImmediateEvents()
	{
		List<Runnable> events = (List<Runnable>)waitingImmediateEvents.clone();
		
		for (Runnable event: events)
		{
			event.run();
		}
		
		waitingImmediateEvents.clear();
	}
	
	
	
	//
	// State key listeners
	//
	
	public void addStateKeyListener(StateKeyListener listener)
	{
		stateKeyListeners.put( listener, null );
	}

	public void removeStateKeyListener(StateKeyListener listener)
	{
		stateKeyListeners.remove( listener );
	}
	
	
	
	
	
	//
	// Queue redraw
	//
	
	protected void queueFullRedraw()
	{
		component.repaint( component.getVisibleRect() );
	}
	
	protected void queueRedraw(Point2 localPos, Vector2 localSize)
	{
		queueRedrawWindowSpace( rootSpaceToWindowSpace( localPos ), rootSpaceToWindowSpace( localSize ) );
	}
	
	protected void queueRedrawWindowSpace(Point2 pos, Vector2 size)
	{
		int x = (int)pos.x, y = (int)pos.y;
		int w = (int)(pos.x + size.x + 0.5) - x;
		int h = (int)(pos.y + size.y + 0.5) - y;
		component.repaint( new Rectangle( x, y, w, h ) ); 
	}
	
	
	

	
	//
	// Queue resize
	//
	
	protected void handleQueueResize()
	{
		super.handleQueueResize();
		bAllocationRequired = true;
		queueFullRedraw();
	}
	
	
	

	//
	// Allocation
	//
	
	private void performAllocation()
	{
		if ( bAllocationRequired )
		{
			long t1 = System.currentTimeMillis();
			refreshMinimumHMetrics();
			HMetrics h = refreshPreferredHMetrics();
			if ( bHorizontalClamp )
			{
				allocateX( areaSize.x / rootScaleInWindowSpace );
			}
			else
			{
				allocateX( h.width );
			}
			refreshMinimumVMetrics();
			VMetrics v = refreshPreferredVMetrics();
			allocateY( v.height );
			bAllocationRequired = false;
			
			// Send motion events; pointer hasn't moved, but the widgets have
			if ( !dndTable.containsKey( rootSpaceMouse.concretePointer() ) )
			{
				rootMotionEvent( new PointerMotionEvent( rootSpaceMouse, PointerMotionEvent.Action.MOTION ) );
			}			
			long t2 = System.currentTimeMillis();
			System.out.println( "DPPresentationArea.performAllocation(): Realloc time = " + (double)(t2-t1) * 0.001 );
		}
	}
	
	
	protected void allocateContentsX(double allocation)
	{
		if ( child != null )
		{
			allocateChildX( child, 0.0, allocation );
		}
	}
	
	
	protected void allocateContentsY(double allocation)
	{
		if ( child != null )
		{
			double height = Math.min( allocation, prefV.height );
			allocateChildY( child, 0.0, height );
		}
	}
	
	
	
	
	
	//
	// Hierarchy methods
	//
	
	protected void setPresentationArea(DPPresentationArea area)
	{
	}
	
	protected void unparent()
	{
	}
	
	
	protected void structureChanged()
	{
		if ( !caret.isValid() )
		{
			if ( !bStructureRefreshQueued )
			{
				final Runnable putCaretAtStart = new Runnable()
				{
					public void run()
					{
						if ( !caret.isValid() )
						{
							DPContentLeaf leaf = getLeftContentLeaf();
							if ( leaf != null )
							{
								leaf.moveMarkerToStart( caret.getMarker() );
							}
						}
						
						bStructureRefreshQueued = false;
					}
				};
			
				bStructureRefreshQueued = true;
				SwingUtilities.invokeLater( putCaretAtStart );
			}
		}
	}

	
	
	
	//
	// Event handling
	//
	
	protected void configureEvent(Vector2 size)
	{
		if ( !size.equals( areaSize ) )
		{
			areaSize = size;
			bAllocationRequired = true;
		}
		emitImmediateEvents();
	}
	
	
	protected void exposeEvent(Graphics2D graphics, Rectangle2D.Double exposeArea)
	{
		// Perform allocation if necessary
		performAllocation();
		
		// Clip to the exposed area
		graphics.clip( exposeArea );
		
		// Fill background
		graphics.setColor( Color.WHITE );
		graphics.fill( exposeArea );
		
		// Get the top-left and bottom-right corners of the exposed area in root space
		Point2 topLeftRootSpace = windowSpaceToRootSpace( new Point2( exposeArea.x, exposeArea.y ) );
		Point2 bottomRightRootSpace = windowSpaceToRootSpace( new Point2( exposeArea.x + exposeArea.width, exposeArea.y + exposeArea.height ) );
		
		// Save the current transform
		AffineTransform transform = graphics.getTransform();
		
		// Apply the transformation
		graphics.scale( rootScaleInWindowSpace, rootScaleInWindowSpace );
		graphics.translate( -windowTopLeftCornerInRootSpace.x, -windowTopLeftCornerInRootSpace.y );
		
		// Draw
		handleDraw( graphics, new AABox2( topLeftRootSpace, bottomRightRootSpace) );
		//graphics.setTransform( transform );
		drawCaret( graphics );
		
		// Restore transform
		graphics.setTransform( transform );
		
		// Emit any immediate events
		emitImmediateEvents();
	}
	
	
	private void drawCaret(Graphics2D graphics)
	{
		if ( caret.isValid() )
		{
			DPContentLeaf widget = caret.getWidget();
			
			if ( widget != null )
			{
				Xform2 x = widget.getTransformRelativeToRoot();
				AffineTransform current = graphics.getTransform();
				x.apply( graphics );
				Color prevColour = graphics.getColor();
				graphics.setColor( Color.blue );
				widget.drawCaret( graphics, caret );
				graphics.setColor( prevColour );
				graphics.setTransform( current );
			}
		}
	}
	
	
	
	protected void mouseDownEvent(int button, Point2 windowPos, int modifiers)
	{
		component.grabFocus();
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setModifiers( modifiers );
		if ( ( modifiers & ( Modifier.ALT | Modifier.ALT_GRAPH | Modifier.CTRL | Modifier.SHIFT ) )  ==  0 )
		{
			DPContentLeafEditable leaf = (DPContentLeafEditable)getLeafClosestToLocalPoint( rootPos, new DPContentLeafEditable.EditableLeafWidgetFilter() );
			Xform2 x = leaf.getTransformRelativeToRoot();
			x = x.inverse();
			leaf.placeCursor( x.transform( rootPos ) );
		}

		if ( ( modifiers & Modifier.ALT )  ==  0 )
		{
			PointerButtonEvent event = new PointerButtonEvent( rootSpaceMouse, button, PointerButtonEvent.Action.DOWN );
				
			dndButtonDownEvent( event );
			handleButtonDown( event );
		}
		else
		{
			dragButton = button;
			dragStartPosInWindowSpace = windowPos;
		}
		
		emitImmediateEvents();
	}
	
	protected void mouseUpEvent(int button, Point2 windowPos, int modifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setModifiers( modifiers );
		if ( ( modifiers & Modifier.ALT )  ==  0 )
		{
			PointerButtonEvent event = new PointerButtonEvent( rootSpaceMouse, button, PointerButtonEvent.Action.UP );
			
			dndButtonUpEvent( event );
			handleButtonUp( event );
		}
		
		if ( dragButton != 0 )
		{
			dragButton = 0;
			dragStartPosInWindowSpace = windowPos;
		}
		
		emitImmediateEvents();
	}
	
	
	
	protected void rootMotionEvent(PointerMotionEvent event)
	{
		boolean bHandled = dndMotionEvent( event );
		
		if ( !bHandled )
		{
			handleMotion( event );
		}
	}

	

	
	protected void mouseMotionEvent(Point2 windowPos, int modifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setModifiers( modifiers );
		
		if ( dragButton == 0 )
		{
			PointerMotionEvent event = new PointerMotionEvent( rootSpaceMouse, PointerMotionEvent.Action.MOTION );
			rootMotionEvent( event );
		}
		else
		{
			Vector2 delta = windowPos.sub( dragStartPosInWindowSpace );
			dragStartPosInWindowSpace = windowPos;
			
			if ( dragButton == 1  ||  dragButton == 2 )
			{
				windowTopLeftCornerInRootSpace = windowTopLeftCornerInRootSpace.sub( windowSpaceToRootSpace( delta ) );
				queueFullRedraw();
			}
			else if ( dragButton == 3 )
			{
				double scaleDeltaPixels = delta.x + delta.y;
				double scaleDelta = Math.pow( 2.0, scaleDeltaPixels / 200.0 );
				
				zoomAboutCentre( scaleDelta );
			}
		}
		
		emitImmediateEvents();
	}



	protected void mouseEnterEvent(Point2 windowPos, int modifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setModifiers( modifiers );
		
		if ( dragButton == 0 )
		{
			PointerMotionEvent event = new PointerMotionEvent( rootSpaceMouse, PointerMotionEvent.Action.ENTER );
			handleEnter( event );
		}
		
		emitImmediateEvents();
	}

	protected void mouseLeaveEvent(Point2 windowPos, int modifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setModifiers( modifiers );
		
		if ( dragButton == 0 )
		{
			PointerMotionEvent event = new PointerMotionEvent( rootSpaceMouse, PointerMotionEvent.Action.LEAVE );
			handleLeave( event );
		}
		
		emitImmediateEvents();
	}


	protected void mouseDown2Event(int button, Point2 windowPos, int modifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setModifiers( modifiers );
		handleButtonDown( new PointerButtonEvent( rootSpaceMouse, button, PointerButtonEvent.Action.DOWN2 ) );
	}


	protected void mouseDown3Event(int button, Point2 windowPos, int modifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setModifiers( modifiers );
		handleButtonDown( new PointerButtonEvent( rootSpaceMouse, button, PointerButtonEvent.Action.DOWN3 ) );
	}
	
	
	protected void mouseWheelEvent(Point2 windowPos, int wheelClicks, int unitsToScroll, int modifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setModifiers( modifiers );
		if ( ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.ALT )
		{
			double delta = (double)-wheelClicks;
			double scaleDelta = Math.pow( 2.0,  ( delta / 1.5 ) );
			
			zoom( scaleDelta, windowPos );
		}
		else if ( ( modifiers & Modifier._KEYS_MASK )  !=  0 )
		{
			handleScroll( new PointerScrollEvent( rootSpaceMouse, 0, -wheelClicks ) );
			emitImmediateEvents();
		}
		else
		{
			double delta = (double)wheelClicks;
			panWindowSpace( new Vector2( 0.0, delta * 75.0 ) );
		}
	}
	
	
	
	
	protected boolean keyPressEvent(KeyEvent event, int modifiers)
	{
		rootSpaceMouse.setModifiers( modifiers );
		
		boolean bCtrl = ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.CTRL;
		boolean bCtrlShift = ( modifiers & Modifier._KEYS_MASK )  ==  ( Modifier.CTRL | Modifier.SHIFT );
		char keyChar = event.getKeyChar();
		
		if ( bCtrl  &&  keyChar == 'z' )
		{
			emitImmediateEvents();
			return true;
		}
		else if ( bCtrlShift  &&  keyChar == 'z' )
		{
			emitImmediateEvents();
			return true;
		}
		else if ( handleNavigationKeyPress( event, modifiers ) )
		{
			emitImmediateEvents();
			return true;
		}
		else
		{
			if ( isModifierKey( event ) )
			{
				for (StateKeyListener listener: stateKeyListeners.keySet())
				{
					listener.onStateKeyPress( event );
				}
				return false;
			}
			else
			{
				if ( caret.isValid() )
				{
					DPContentLeaf leaf = caret.getMarker().getWidget();
					if ( leaf.isEditable() )
					{
						DPContentLeafEditable editable = (DPContentLeafEditable)leaf;
						editable.onKeyPress( caret, event );
					}
					emitImmediateEvents();
					return true;
				}
				else
				{
					emitImmediateEvents();
					return false;
				}
			}
		}
	}
	
	protected boolean keyReleaseEvent(KeyEvent event, int modifiers)
	{
		rootSpaceMouse.setModifiers( modifiers );
		
		boolean bCtrl = ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.CTRL;
		boolean bCtrlShift = ( modifiers & Modifier._KEYS_MASK )  ==  ( Modifier.CTRL | Modifier.SHIFT );
		char keyChar = event.getKeyChar();
		
		if ( bCtrl  &&  keyChar == 'z' )
		{
			emitImmediateEvents();
			return true;
		}
		else if ( bCtrlShift  &&  keyChar == 'z' )
		{
			emitImmediateEvents();
			return true;
		}
		else if ( isNavigationKey( event ) )
		{
			emitImmediateEvents();
			return true;
		}
		else
		{
			if ( isModifierKey( event ) )
			{
				for (StateKeyListener listener: stateKeyListeners.keySet())
				{
					listener.onStateKeyRelease( event );
				}
				return false;
			}
			else
			{
				if ( caret.isValid() )
				{
					DPContentLeaf leaf = caret.getMarker().getWidget();
					if ( leaf.isEditable() )
					{
						DPContentLeafEditable editable = (DPContentLeafEditable)leaf;
						editable.onKeyRelease( caret, event );
					}
					emitImmediateEvents();
					return true;
				}
				else
				{
					emitImmediateEvents();
					return false;
				}
			}
		}
	}
	
	
	
	protected boolean keyTypedEvent(KeyEvent event, int modifiers)
	{
		rootSpaceMouse.setModifiers( modifiers );
		
		boolean bCtrl = ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.CTRL;
		boolean bCtrlShift = ( modifiers & Modifier._KEYS_MASK )  ==  ( Modifier.CTRL | Modifier.SHIFT );
		char keyChar = event.getKeyChar();
		
		if ( bCtrl  &&  keyChar == 'z' )
		{
			if ( undoListener != null )
			{
				undoListener.onUndo( this );
			}
			emitImmediateEvents();
			return true;
		}
		else if ( bCtrlShift  &&  keyChar == 'z' )
		{
			if ( undoListener != null )
			{
				undoListener.onRedo( this );
			}
			emitImmediateEvents();
			return true;
		}
		else if ( isNavigationKey( event ) )
		{
			emitImmediateEvents();
			return true;
		}
		else
		{
			if ( isModifierKey( event ) )
			{
				for (StateKeyListener listener: stateKeyListeners.keySet())
				{
					listener.onStateKeyTyped( event );
				}
				return false;
			}
			else
			{
				if ( caret.isValid() )
				{
					DPContentLeaf leaf = caret.getMarker().getWidget();
					if ( leaf.isEditable() )
					{
						DPContentLeafEditable editable = (DPContentLeafEditable)leaf;
						editable.onKeyTyped( caret, event );
					}
					emitImmediateEvents();
					return true;
				}
				else
				{
					emitImmediateEvents();
					return false;
				}
			}
		}
	}
	
	
	
	
	protected boolean isNavigationKey(KeyEvent event)
	{
		int keyCode = event.getKeyCode();
		return keyCode == KeyEvent.VK_LEFT  ||  keyCode == KeyEvent.VK_RIGHT  ||  keyCode == KeyEvent.VK_UP  ||  keyCode == KeyEvent.VK_DOWN  ||  keyCode == KeyEvent.VK_HOME  ||  keyCode == KeyEvent.VK_END;
	}
	
	protected boolean isModifierKey(KeyEvent event)
	{
		int keyCode = event.getKeyCode();
		return keyCode == KeyEvent.VK_CONTROL  ||  keyCode == KeyEvent.VK_SHIFT  ||  keyCode == KeyEvent.VK_ALT  ||  keyCode == KeyEvent.VK_ALT_GRAPH;
	}
	
	protected boolean handleNavigationKeyPress(KeyEvent event, int modifiers)
	{
		if ( isNavigationKey( event ) )
		{
			if ( caret.isValid() )
			{
				DPContentLeaf leaf = caret.getMarker().getWidget();
				if ( event.getKeyCode() == KeyEvent.VK_LEFT )
				{
					leaf.moveMarkerLeft( caret.getMarker(), false, true );
				}
				else if ( event.getKeyCode() == KeyEvent.VK_RIGHT )
				{
					leaf.moveMarkerRight( caret.getMarker(), false, true );
				}
				else if ( event.getKeyCode() == KeyEvent.VK_UP )
				{
					leaf.moveMarkerUp( caret.getMarker(), true );
				}
				else if ( event.getKeyCode() == KeyEvent.VK_DOWN )
				{
					leaf.moveMarkerDown( caret.getMarker(), true );
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	

	protected void realiseEvent()
	{
		handleRealise();
		emitImmediateEvents();
	}
	
	
	protected void unrealiseEvent()
	{
		handleUnrealise( this );
		emitImmediateEvents();
	}
	


	private void dndButtonDownEvent(PointerButtonEvent event)
	{
		if ( !dndTable.containsKey( event.pointer.concretePointer() ) )
		{
			DndDrag drag = handleDndButtonDown( event );
			
			if ( drag != null )
			{
				dndTable.put( event.pointer.concretePointer(), drag );
			}
		}
	}
	
	private boolean dndMotionEvent(PointerMotionEvent event)
	{
		DndDrag drag = dndTable.get( event.pointer.concretePointer() );

		if ( drag != null )
		{
			if ( !drag.bInProgress )
			{
				drag.srcWidget.handleDndBegin( event, drag );
				drag.bInProgress = true;
				setCursorDrag( event.pointer );
			}
			
			handleDndMotion( event, drag );
			
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean dndButtonUpEvent(PointerButtonEvent event)
	{
		DndDrag drag = dndTable.get( event.pointer.concretePointer() );
		
		if ( drag != null )
		{
			handleDndButtonUp( event, drag );
			drag.bInProgress = false;
			dndTable.remove( event.pointer.concretePointer() );
			setCursorArrow( event.pointer );
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	
	private void setCursorDrag(PointerInterface pointer)
	{
		component.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
	}
	
	private void setCursorArrow(PointerInterface pointer)
	{
		component.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
	}
	
	
	public InputTable getInputTable()
	{
		return inputTable;
	}
	
	
	
	public Graphics2D getGraphics()
	{
		Graphics2D g2;
		try
		{
			g2 = (Graphics2D)component.getGraphics();
		}
		catch (ClassCastException e)
		{
			throw new CannotGetGraphics2DException();
		}
		
		return g2;
	}
	
	
	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public void caretChanged(Caret c)
	{
		assert c == caret;
		
		DPContentLeaf caretLeaf = c.getWidget();
		
		if ( caretLeaf != currentCaretLeaf )
		{
			if ( currentCaretLeaf != null )
			{
				currentCaretLeaf.handleCaretLeave( caret );
			}
			
			currentCaretLeaf = caretLeaf;
			
			if ( currentCaretLeaf != null )
			{
				currentCaretLeaf.handleCaretEnter( caret );
			}
		}
		
		queueFullRedraw();
	}
}
