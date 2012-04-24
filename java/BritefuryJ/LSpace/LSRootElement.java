//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;

import BritefuryJ.LSpace.PresentationComponent.CannotGetGraphics2DException;
import BritefuryJ.LSpace.PresentationComponent.PresentationPopup;
import BritefuryJ.LSpace.PresentationComponent.TypesetProfile;
import BritefuryJ.LSpace.PresentationComponent.TypesetProfileMeasurement;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.SelectionListener;
import BritefuryJ.LSpace.Focus.SelectionManager;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Input.DndController;
import BritefuryJ.LSpace.Input.DndDragSwing;
import BritefuryJ.LSpace.Input.DndDropSwing;
import BritefuryJ.LSpace.Input.InputTable;
import BritefuryJ.LSpace.Input.Pointer;
import BritefuryJ.LSpace.Input.PointerInputElement;
import BritefuryJ.LSpace.Input.Keyboard.Keyboard;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeRootElement;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Util.WeakIdentityHashMap;

public class LSRootElement extends LSBin implements SelectionListener, DndController
{
	private PresentationComponent component;
	
	private Vector2 windowSize;
	
	private boolean hasComponentFocus = false;
	
	private Pointer rootSpaceMouse;
	private Keyboard keyboard;
	private InputTable inputTable;
	
	private Runnable immediateEventDispatcher;
	
	private boolean bAllocationRequired;
	
	
	protected HashMap<LSContentLeafEditable, WeakIdentityHashMap<Marker, Object>> markersByLeaf =
		new HashMap<LSContentLeafEditable, WeakIdentityHashMap<Marker, Object>>();
	
	private Caret caret;
	
	private Target target;
	
	private SelectionManager selectionManager;
	private Selection selection;
	
	
	private boolean caretMoveToStartQueued;
	private LSElement ensureVisibilityElement;
	
	
	protected LinkedList<Runnable> waitingImmediateEvents;			// only initialised when non-empty; otherwise null
	
	
	private ElementValueCacheManager valueCacheManager = new ElementValueCacheManager( this );
	private DefaultTextRepresentationManager defaultTextRepresentationManager = new DefaultTextRepresentationManager();
	

	
	private ArrayList<ElementPreview> elementPreviews = new ArrayList<ElementPreview>();

	protected PresentationComponent metaElementComponent;
	
		
	protected PageController pageController = null;
	
	
	private TypesetProfile profile;
	
	
	
	public LSRootElement(PresentationComponent component)
	{
		super( ContainerStyleParams.defaultStyleParams, null );
		
		this.component = component;
		
		layoutNode = new LayoutNodeRootElement( this );
		
		rootElement = this;
		
		windowSize = null;
		
		inputTable = new InputTable( this, this, component );
		rootSpaceMouse = inputTable.getMouse();
		
		bAllocationRequired = true;
		
		caret = new Caret();
		
		target = null;
		
		selectionManager = new SelectionManager( this );
		
		keyboard = new Keyboard( this );

		caretMoveToStartQueued = false;
	}
	
	
	
	//
	//
	// Focus
	//
	//
	
	public void grabFocus()
	{
		component.grabFocus();
	}

	
	
	//
	//
	// Page controller
	//
	//
	
	public void setPageController(PageController pageController)
	{
		this.pageController = pageController;
	}
	
	public PageController getPageController()
	{
		return pageController;
	}
	
	
	//
	//
	// COMPONENT
	//
	//
	
	public PresentationComponent getComponent()
	{
		return component;
	}
	
	public ImageObserver getImageObserver()
	{
		return component;
	}
	
	
	
	//
	//
	// TARGET  METHODS
	//
	//
	
	public Target getTarget()
	{
		return target == null  ?  caret : target;
	}
	
	public void setTarget(Target t)
	{
		Target prev = getTarget();
		if ( prev != null )
		{
			prev.notifyDeactivate();
		}
		
		target = t;

		Target cur = getTarget();
		if ( cur != null )
		{
			cur.notifyActivate();
		}

		queueFullRedraw();
	}
	
	public void setCaretAsTarget()
	{
		setTarget( null );
	}
	
	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public Caret getCaret()
	{
		return caret;
	}
	
	

	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public Selection getSelection()
	{
		return selection;
	}
	
	public SelectionManager getSelectionManager()
	{
		return selectionManager;
	}
	
	public void setSelection(Selection s)
	{
		if ( selection != null ) 
		{
			selection.removeSelectionListener( this );
		}
		selection = s;
		if ( selection != null ) 
		{
			selection.addSelectionListener( this );
		}
		queueFullRedraw();
	}

	public String getTextRepresentationInSelection(TextSelection s)
	{
		return defaultTextRepresentationManager.getTextRepresentationInTextSelection( s );
	}

	
	public void selectionChanged(Selection s)
	{
		if ( s == selection )
		{
			queueFullRedraw();
		}
	}
	
	
	

	//
	//
	// Immediate event queue methods
	//
	//
	
	public void queueImmediateEvent(Runnable event)
	{
		if ( waitingImmediateEvents == null  &&  immediateEventDispatcher == null )
		{
			final LSRootElement root = this;
			// We will be adding the first event; create the dispatcher and queue it
			immediateEventDispatcher = new Runnable()
			{
				public void run()
				{
					root.dispatchQueuedImmediateEvents();
				}
			};
			
			SwingUtilities.invokeLater( immediateEventDispatcher );
		}
		
		if ( waitingImmediateEvents == null )
		{
			waitingImmediateEvents = new LinkedList<Runnable>();
		}
		
		if ( !waitingImmediateEvents.contains( event ) )
		{
			waitingImmediateEvents.add( event );
		}
	}

	public void dequeueImmediateEvent(Runnable event)
	{
		if ( waitingImmediateEvents != null )
		{
			if ( waitingImmediateEvents.contains( event ) )
			{
				waitingImmediateEvents.remove( event );
				if ( waitingImmediateEvents.isEmpty() )
				{
					waitingImmediateEvents = null;
				}
			}
		}
	}
	
	private void dispatchQueuedImmediateEvents()
	{
		emitImmediateEvents();
		immediateEventDispatcher = null;
	}
	
	private void emitImmediateEvents()
	{
		while ( waitingImmediateEvents != null  &&  !waitingImmediateEvents.isEmpty() )
		{
			Runnable event = waitingImmediateEvents.removeFirst();
			event.run();
		}
		
		waitingImmediateEvents = null;
	}
	
	
	
	//
	//
	// PAINTING METHODS
	//
	//
	
	public void queueFullRedraw()
	{
		component.repaint( component.getVisibleRect() );
	}
	
	protected void queueRedraw(AABox2 box)
	{
		queueRedrawWindowSpace( box );
	}
	
	protected void queueRedrawWindowSpace(AABox2 box)
	{
		int x = (int)( box.getLowerX() - 1.0 ), y = (int)( box.getLowerY() - 1.0 );
		int w = (int)( box.getUpperX() + 1.0 ) - x;
		int h = (int)( box.getUpperY() + 1.0 ) - y;
		component.repaint( new Rectangle( x, y, w, h ) ); 
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
	
	
	
	
	protected void exposeEvent(Graphics2D graphics, Rectangle2D.Double exposeArea)
	{
		// Perform allocation if necessary
		performAllocation();
		
		// Enable anti-aliasing
		//graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		//graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		
		// Clip to the exposed area
		graphics.clip( exposeArea );
		
		// Get the top-left and bottom-right corners of the exposed area in root space
		Point2 topLeftRootSpace = new Point2( exposeArea.x, exposeArea.y );
		Point2 bottomRightRootSpace = new Point2( exposeArea.x + exposeArea.width, exposeArea.y + exposeArea.height );
		
		// Draw
		handleDrawBackground( graphics, new AABox2( topLeftRootSpace, bottomRightRootSpace) );
		//drawSelection( graphics );
		handleDraw( graphics, new AABox2( topLeftRootSpace, bottomRightRootSpace) );
		drawSelection( graphics );
		//graphics.setTransform( transform );
		if ( hasComponentFocus )
		{
			drawTarget( graphics );
		}
		
		// Draw any element previews
		drawElementPreviews( graphics );
		
		// Emit any immediate events
		emitImmediateEvents();
	}
	
	
	private void drawTarget(Graphics2D graphics)
	{
		final Target t = getTarget();
		if ( t != null  &&  t.isValid() )
		{
			t.draw( graphics );

			if ( t.isAnimated() )
			{
				// Repeat
				ActionListener onTimer = new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						if ( t != null  &&  t.isValid()  &&  t.isAnimated() )
						{
							t.getElement().queueFullRedraw();
						}
					}
				};
				
				Timer timer = new Timer( 40, onTimer );
				timer.setRepeats( false );
				timer.start();
			}
		}
	}
	
	
	private void drawSelection(Graphics2D graphics)
	{
		if ( selection != null )
		{
			Paint prevPaint = graphics.getPaint();
			graphics.setPaint( new Color( 1.0f, 0.9f, 0.0f, 0.4f ) );
			selection.draw( graphics );
			graphics.setPaint( prevPaint );
		}
	}
	
	
	
	
	
	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	public void queueReallocation()
	{
		bAllocationRequired = true;
		component.notifyQueueReallocation();
		queueFullRedraw();
	}
	
	private void performAllocation()
	{
		if ( bAllocationRequired )
		{
			long t1 = System.nanoTime();
			
			Dimension screenSize = null;
			if ( windowSize == null )
			{
				screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			}
			
			LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();

			// Get X requisition
			LReqBoxInterface reqX = rootLayout.refreshRequisitionX();
			
			// Allocate X
			double prevWidth = rootLayout.getAllocWidth();
			double windowWidth = windowSize != null  ?  windowSize.x  :  Math.min( reqX.getReqPrefWidth(), screenSize.getWidth() );
			rootLayout.allocateX( reqX, 0.0, windowWidth );
			rootLayout.refreshAllocationX( prevWidth );
			
			// Get Y requisition
			LReqBoxInterface reqY = rootLayout.refreshRequisitionY();
			
			// Allocate Y
			LAllocV prevAllocV = rootLayout.getAllocV();
			double windowHeight = windowSize != null  ?  windowSize.y  :  Math.min( reqX.getReqHeight(), screenSize.getHeight() );
			//rootLayout.allocateY( reqY, 0.0, reqY.getReqHeight() );
			rootLayout.allocateY( reqY, 0.0, windowHeight );
			rootLayout.refreshAllocationY( prevAllocV );
			
			bAllocationRequired = false;
			
			// Send motion events; pointer hasn't moved, but the elements have
			inputTable.onRootElementReallocate();
			long t2 = System.nanoTime();
			double typesetTime = (double)(t2-t1) * 1.0e-9;
			System.out.println( "RootElement.performAllocation(): TYPESET TIME = " + typesetTime  +  ", used memory = "  + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) );
			if ( profile != null )
			{
				profile.addMeasurement( new TypesetProfileMeasurement( typesetTime ) );
			}
			
			if ( ensureVisibilityElement != null )
			{
				ensureVisibilityElement.ensureVisible();
				ensureVisibilityElement = null;
			}
		}
	}
	

	// Configure event
	
	protected void configureEvent(Vector2 size)
	{
		if ( windowSize == null  ||  !size.equals( windowSize ) )
		{
			windowSize = size;
			bAllocationRequired = true;
			queueReallocation();
		}
		emitImmediateEvents();
	}
	
	
	// Swing size requirement methods
	
	public Dimension allocateAndGetPreferredSize()
	{
		if ( isRealised() )
		{
			//performAllocationForSpaceRequirements();
			performAllocation();
			LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();
			return new Dimension( (int)Math.ceil( rootLayout.getReqPrefWidth() ),  (int)Math.ceil( rootLayout.getReqHeight() ) );
		}
		else
		{
			return new Dimension( 1, 1 );
		}
	}
	
	Dimension getMinimumSize()
	{
		if ( isRealised() )
		{
			//performAllocationForSpaceRequirements();
			performAllocation();
			LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();
			return new Dimension( (int)Math.ceil( rootLayout.getReqMinWidth() ),  (int)Math.ceil( rootLayout.getReqHeight() ) );
		}
		else
		{
			return new Dimension( 1, 1 );
		}
	}
	
	Dimension getPreferredSize()
	{
		if ( isRealised() )
		{
			//performAllocationForSpaceRequirements();
			performAllocation();
			LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();
			return new Dimension( (int)Math.ceil( rootLayout.getReqPrefWidth() ),  (int)Math.ceil( rootLayout.getReqHeight() ) );
		}
		else
		{
			return new Dimension( 1, 1 );
		}
	}
	
	Dimension getMaximumSize()
	{
		return getPreferredSize();
	}
	
	
	// Ensure element visibility
	
	protected void queueEnsureVisible(LSElement element)
	{
		ensureVisibilityElement = element;
	}
	
	
	
	
	//
	// Hierarchy methods
	//
	
	protected void setRootElement(LSRootElement root)
	{
	}
	
	protected void unparent()
	{
	}
	
	
	protected void onSubtreeStructureChanged()
	{
		if ( !caret.isValid() )
		{
			queueMoveCaretToStartOfDocument();
		}

		// Handle selections
		if ( selection != null )
		{
			selection.onPresentationTreeStructureChanged();
		}
	}
	
	
	
	private void queueMoveCaretToStartOfDocument()
	{
		if ( !caretMoveToStartQueued )
		{
			final Runnable putCaretAtStart = new Runnable()
			{
				public void run()
				{
					if ( !caret.isValid() )
					{
						caret.moveToStartOfElement( LSRootElement.this );
					}
					
					caretMoveToStartQueued = false;
				}
			};
		
			SwingUtilities.invokeLater( putCaretAtStart );
			
			caretMoveToStartQueued = true;
		}
	}

	
	
	
	
	
	//
	//
	// COMPONENT FOCUS GAIN / LOSS METHODS
	//
	//
	
	void componentFocusGained()
	{
		hasComponentFocus = true;
		Target target = getTarget();
		if ( target == getCaret()  &&  !target.isValid() )
		{
			queueMoveCaretToStartOfDocument();
		}
		
		queueFullRedraw();
	}
	
	void componentFocusLost()
	{
		hasComponentFocus = false;
		queueFullRedraw();
	}
	
	
	
	

	//
	//
	// REALISE / UNREALISE METHODS
	//
	//
	
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
	
	
	protected void elementUnrealised(LSElement element)
	{
		inputTable.onElementUnrealised( element );
	}
	
	
	
	
	
	//
	//
	// INPUT METHODS
	//
	//
	
	public InputTable getInputTable()
	{
		return inputTable;
	}
	

	

	//
	//
	// MOUSE METHODS
	//
	//

	protected void setPointerCursor(Cursor cursor)
	{
		component.setCursor( cursor );
	}
	
	protected void setPointerCursorDefault()
	{
		component.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
	}
	
	
	protected void mouseDownEvent(int button, Point2 windowPos, int buttonModifiers, int keyModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		component.grabFocus();
		rootSpaceMouse.setLocalPos( windowPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		rootSpaceMouse.setKeyModifiers( keyModifiers );
		
		rootSpaceMouse.buttonDown( windowPos, button );

		emitImmediateEvents();
	}
	
	protected void mouseUpEvent(int button, Point2 windowPos, int buttonModifiers, int keyModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setLocalPos( windowPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		rootSpaceMouse.setKeyModifiers( keyModifiers );

		
		rootSpaceMouse.buttonUp( windowPos, button );


		emitImmediateEvents();
	}
	
	
	protected void mouseClicked(int button, int clickCount, Point2 windowPos, int buttonModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setLocalPos( windowPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		
		rootSpaceMouse.buttonClicked( windowPos, button, clickCount );
	}


	
	protected void mouseMotionEvent(Point2 windowPos, int buttonModifiers, int keyModifiers, MouseEvent mouseEvent)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setLocalPos( windowPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		rootSpaceMouse.setKeyModifiers( keyModifiers );
		
		rootSpaceMouse.motion( windowPos, mouseEvent );

		emitImmediateEvents();
	}

	protected void mouseDragEvent(Point2 windowPos, int buttonModifiers, int keyModifiers, MouseEvent mouseEvent)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setLocalPos( windowPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		rootSpaceMouse.setKeyModifiers( keyModifiers );
		
		rootSpaceMouse.drag( windowPos, mouseEvent );

		emitImmediateEvents();
	}



	
	protected void mouseEnterEvent(Point2 windowPos, int buttonModifiers, int keyModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setLocalPos( windowPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		rootSpaceMouse.setKeyModifiers( keyModifiers );
		
		rootSpaceMouse.enter( windowPos );
		
		emitImmediateEvents();
	}

	protected void mouseLeaveEvent(Point2 windowPos, int buttonModifiers, int keyModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setLocalPos( windowPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		rootSpaceMouse.setKeyModifiers( keyModifiers );
		
		rootSpaceMouse.leave( windowPos );
		
		emitImmediateEvents();
	}


	protected void mouseWheelEvent(Point2 windowPos, int wheelClicks, int unitsToScroll, int buttonModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setLocalPos( windowPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		
		rootSpaceMouse.scroll( 0, -wheelClicks );
		emitImmediateEvents();
	}
	
	
	
	
	//
	//
	// KEYBOARD METHODS
	//
	//
	
	public Keyboard getKeyboard()
	{
		return keyboard;
	}

	
	protected boolean keyPressEvent(KeyEvent event, int keyModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setKeyModifiers( keyModifiers );

		boolean bHandled = false;
		try
		{
			bHandled = keyboard.keyPressed( event );
		}
		catch (Throwable e)
		{
			notifyExceptionDuringEventHandler( keyboard, "keyPressed", e );
		}
		emitImmediateEvents();
		return bHandled;
	}
	
	protected boolean keyReleaseEvent(KeyEvent event, int keyModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setKeyModifiers( keyModifiers );

		boolean bHandled = false;
		try
		{
			bHandled = keyboard.keyReleased( event );
		}
		catch (Throwable e)
		{
			notifyExceptionDuringEventHandler( keyboard, "keyReleased", e );
		}
		emitImmediateEvents();
		return bHandled;
	}
	
	
	
	protected boolean keyTypedEvent(KeyEvent event, int keyModifiers)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		rootSpaceMouse.setKeyModifiers( keyModifiers );

		boolean bHandled = false;
		try
		{
			bHandled = keyboard.keyTyped( event );
		}
		catch (Throwable e)
		{
			notifyExceptionDuringEventHandler( keyboard, "keyTyped", e );
		}
		emitImmediateEvents();
		return bHandled;
	}
	
	
	
	

	//
	//
	// DRAG AND DROP PROTOCOL
	//
	// 1. The user presses a mouse button:
	//	onButtonDown is sent.
	//	onDndButtonDown is also sent. If a element with DnD enabled can be found, it creates and returns a DndDrop structure that will be used to track information
	//	on the drag.
	// 2. The first motion event after the button press:
	//	The getSourceRequestedAction() method of the DnD handler for the source element is invoked to get the requested initial action.
	//	The createTransferable() method of the DnD handler is invoked, to create the data to be 'dragged'.
	//	These values are placed into the DndDrop structure.
	// 3. Motion events:
	//	The (target) element under the pointer is retrieved.
	//	The canDrop() method of the DnD handler for the target element is invoked to see if it can accept the drop
	// 4. The user releases the mouse button
	//	The canDrop() method of the DnD handler for the target element is invoked to see if it can accept the drop
	//	If so, the importDrop() method of the DnD handler for the target element is invoked to accept the drop
	//
	//
	
	protected PresentationComponent.PresAreaTransferHandler getDndTransferHandler()
	{
		return (PresentationComponent.PresAreaTransferHandler)component.getTransferHandler();
	}
	
	
	public void dndInitiateDrag(DndDragSwing drop, MouseEvent mouseEvent, int requestedAction)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		PresentationComponent.PresAreaTransferHandler xferHandler = getDndTransferHandler();
		xferHandler.initiateExportDnd( drop );
		xferHandler.exportAsDrag( component, mouseEvent, requestedAction );
	}

	
	
	private ElementPreview getElementPreview(Transferable transferable)
	{
		// Attach element previews
		if ( transferable.isDataFlavorSupported( ElementPreview.flavor ) )
		{
			try
			{
				return (ElementPreview)transferable.getTransferData( ElementPreview.flavor );
			}
			catch (UnsupportedFlavorException e)
			{
			}
			catch (IOException e)
			{
			}
		}
		return null;
	}
	
	private ElementPreview getElementPreview(TransferHandler.TransferSupport transfer)
	{
		// Attach element previews
		Transferable transferable = transfer.getTransferable();
		return getElementPreview( transferable );
	}
	
	private void attachElementPreview(TransferHandler.TransferSupport transfer, Point2 pos, boolean success)
	{
		// Attach element previews
		ElementPreview preview = getElementPreview( transfer );
		if ( preview != null )
		{
			preview.attachTo( this, pos, success );
		}
	}
	
	private void detachElementPreview(TransferHandler.TransferSupport transfer)
	{
		// Attach element previews
		ElementPreview preview = getElementPreview( transfer );
		if ( preview != null )
		{
			preview.detach();
		}
	}
	
	void detachElementPreview(Transferable transferable)
	{
		// Attach element previews
		ElementPreview preview = getElementPreview( transferable );
		if ( preview != null )
		{
			preview.detach();
		}
	}
	
	boolean swingDndCanImport(TransferHandler.TransferSupport transfer)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		Point windowPos = transfer.getDropLocation().getDropPoint();
		Point2 rootPos = new Point2( windowPos.x, windowPos.y );

		boolean success = false;
		List<PointerInputElement.DndTarget> targets = PointerInputElement.getDndTargets( this, rootPos );
		for (PointerInputElement.DndTarget target: targets)
		{
			if ( target.isDest() )
			{
				PointerInputElement targetElement = target.getElement();
				Point2 targetPos = target.getElementSpacePos();

				DndDropSwing drop = new DndDropSwing( targetElement, targetPos, transfer );
				if ( targetElement.getDndHandler().canDrop( targetElement, drop ) )
				{
					success = true;
					break;
				}
			}
		}

		// Attach element preview
		attachElementPreview( transfer, rootPos, success );
		
		return success;
	}
	
	boolean swingDndImportData(TransferHandler.TransferSupport transfer)
	{
		// Ensure layout is up to date so that event handling will work correctly
		performAllocation();

		Point windowPos = transfer.getDropLocation().getDropPoint();
		Point2 rootPos = new Point2( windowPos.x, windowPos.y );

		// Detach element preview
		detachElementPreview( transfer );
		
		List<PointerInputElement.DndTarget> targets = PointerInputElement.getDndTargets( this, rootPos );
		for (PointerInputElement.DndTarget target: targets)
		{
			if ( target.isDest() )
			{
				PointerInputElement targetElement = target.getElement();
				Point2 targetPos = target.getElementSpacePos();

				DndDropSwing drop = new DndDropSwing( targetElement, targetPos, transfer );
				return targetElement.getDndHandler().acceptDrop( targetElement, drop );
			}
		}
		return false;
	}
	
	
	
	//
	//
	// ELEMENT PREVIEW METHODS
	//
	//
	
	protected void addElementPreview(ElementPreview preview)
	{
		elementPreviews.add( preview );
		queueFullRedraw();
	}
	
	protected void removeElementPreview(ElementPreview preview)
	{
		elementPreviews.remove( preview );
		queueFullRedraw();
	}
	
	private void drawElementPreviews(Graphics2D graphics)
	{
		for (ElementPreview preview: elementPreviews)
		{
			preview.draw( graphics );
		}
	}
	
	
	
	
	//
	//
	// POPUP METHODS
	//
	//
	
	protected PresentationPopup createPopupPresentation(LSElement popupContents, Point2 localPos, Anchor popupAnchor, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		return component.createPopupPresentation( popupContents, (int)( localPos.x + 0.5 ), (int)( localPos.y + 0.5 ),
				popupAnchor, bCloseOnLoseFocus, bRequestFocus );
	}
	
	public PresentationPopup createPopupAtMousePosition(LSElement popupContents, Anchor popupAnchor, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		Point mouse = component.getMousePosition();
		return component.createPopupPresentation( popupContents, mouse.x, mouse.y, popupAnchor, bCloseOnLoseFocus, bRequestFocus );
	}

	
	
	//
	//
	// VALUE CACHE METHODS
	//
	//
	
	protected ElementValueCacheManager getElementValueCacheManager()
	{
		return valueCacheManager;
	}
	
	public DefaultTextRepresentationManager getDefaultTextRepresentationManager()
	{
		return defaultTextRepresentationManager;
	}
	
	
	
	//
	//
	// TYPESET PROFILE METHODS
	//
	//
	
	public void enableTypesetProfiling()
	{
		profile = new TypesetProfile();
	}
	
	public void disableTypesetProfiling()
	{
		profile = null;
	}
	
	public TypesetProfile getTypesetProfile()
	{
		return profile;
	}
}