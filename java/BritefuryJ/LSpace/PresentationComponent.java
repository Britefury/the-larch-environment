//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;

import org.python.core.Py;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Editor.Table.ObjectList.AttributeColumn;
import BritefuryJ.Editor.Table.ObjectList.ObjectListInterface;
import BritefuryJ.Editor.Table.ObjectList.ObjectListTableEditor;
import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.LSpace.Clipboard.DataTransfer;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.SelectionListener;
import BritefuryJ.LSpace.Focus.SelectionManager;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Focus.TargetListener;
import BritefuryJ.LSpace.Input.DndController;
import BritefuryJ.LSpace.Input.DndDropLocal;
import BritefuryJ.LSpace.Input.DndDropSwing;
import BritefuryJ.LSpace.Input.InputTable;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Input.Pointer;
import BritefuryJ.LSpace.Input.PointerInputElement;
import BritefuryJ.LSpace.Input.Keyboard.Keyboard;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeRootElement;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Util.WeakIdentityHashMap;

public class PresentationComponent extends JComponent implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener, FocusListener
{
	public static class CannotGetGraphics2DException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static class InvalidMouseButtonException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	private static final long serialVersionUID = 1L;
	
	
	
	
	
	
	public static class PresentationPopup
	{
		private JWindow popupWindow;
		private PresentationComponent popupComponent;
		private PopupChain chain;
		private boolean isOpen;
		
		private PresentationPopup(PopupChain popupChain, Window ownerWindow, PresentationComponent parentComponent, LSElement popupContents, int x, int y,
				Corner popupAnchor, boolean closeOnLoseFocus, boolean requestFocus)
		{
			chain = popupChain;
			chain.addPopup( this );
			isOpen = true;
			
			// Create the popup window
			popupWindow = new JWindow( ownerWindow );
			if ( requestFocus )
			{
				popupWindow.setAlwaysOnTop( true );
				popupWindow.setFocusable( true );
			}
			
			// Create a presentation component for the popup contents, and add them
			popupComponent = new PresentationComponent( this );
			popupComponent.getRootElement().setChild( popupContents.layoutWrap( HAlignment.EXPAND, VAlignment.EXPAND ) );
			
			popupWindow.add( popupComponent );
			
			
			popupWindow.pack();
			Dimension sz = popupWindow.getSize();
			AABox2 windowBox = new AABox2( 0.0, 0.0, sz.getWidth(), sz.getHeight() );
			Point2 windowAnchor = popupAnchor.getBoxCorner( windowBox );
			x -= (int)( windowAnchor.x + 0.5 );
			y -= (int)( windowAnchor.y + 0.5 );
			popupWindow.setLocation( x, y );

			
			popupWindow.setVisible( true );
			if ( requestFocus )
			{
				popupWindow.requestFocus();
			}

		
			WindowFocusListener focusListener = new WindowFocusListener()
			{
				public void windowGainedFocus(WindowEvent arg0)
				{
				}

				public void windowLostFocus(WindowEvent arg0)
				{
					// If the popup has no child
					if ( isOpen )
					{
						if ( !chain.popupHasChild( PresentationPopup.this ) )
						{
							chain.closeChainNotContainingPointers();
						}
					}
				}
			};
			
			if ( closeOnLoseFocus )
			{
				popupWindow.addWindowFocusListener( focusListener );
			}
		}
		
		
		public void closePopup()
		{
			isOpen = false;
			popupWindow.setVisible( false );
		}
	}
	
	
	private static class PopupChain
	{
		private ArrayList<PresentationPopup> popups = new ArrayList<PresentationPopup>();
		private PresentationComponent owner;
		
		
		public PopupChain(PresentationComponent owner)
		{
			this.owner = owner;
		}
		
		
		private void addPopup(PresentationPopup popup)
		{
			popups.add( 0, popup );
		}
		
		
		private boolean popupHasChild(PresentationPopup popup)
		{
			int index = popups.indexOf( popup );
			if ( index == -1 )
			{
				throw new RuntimeException( "Could not find popup in chain" );
			}
			return index  > 0;
		}


		private void closeAllChildrenOf(PresentationPopup popup)
		{
			int index = 0;
			for (PresentationPopup p: popups)
			{
				if ( p != popup )
				{
					p.closePopup();
				}
				else
				{
					ArrayList<PresentationPopup> ps = new ArrayList<PresentationPopup>();
					ps.addAll( popups.subList( index, popups.size() ) );
					popups = ps;
					break;
				}
				index++;
			}
		}
		
		private void closeChainNotContainingPointers()
		{
			int index = 0;
			for (PresentationPopup p: popups)
			{
				RootElement rootElement = p.popupComponent.getRootElement();
				if ( rootElement.getInputTable().arePointersWithinBoundsOfElement( rootElement ) )
				{
					ArrayList<PresentationPopup> ps = new ArrayList<PresentationPopup>();
					ps.addAll( popups.subList( index, popups.size() ) );
					popups = ps;
					return;
				}
				else
				{
					p.closePopup();
				}
				
				index++;
			}
			popups.clear();
		}


		public void closeChain()
		{
			for (PresentationPopup p: popups)
			{
				p.closePopup();
			}
			popups.clear();
		}
	}
	
	
	
	private class PresAreaTransferHandler extends TransferHandler
	{
		private static final long serialVersionUID = 1L;
		private DndDropLocal drop = null;
		
		
		
		
		@Override
		public int getSourceActions(JComponent component)
		{
			if ( drop != null )
			{
				return drop.getSourceDropActions();
			}
			else
			{
				LSRegion region = rootElement.getSelectionRegion();
				if ( region != null )
				{
					ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
					if ( clipboardHandler != null )
					{	
						return clipboardHandler.getExportActions( rootElement.selection );
					}
				}
				return NONE;
			}
		}
		
		@Override
		public Transferable createTransferable(JComponent component)
		{
			if ( drop != null )
			{
				return drop.getTransferable();
			}
			else
			{
				LSRegion region = rootElement.getSelectionRegion();
				if ( region != null )
				{
					ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
					if ( clipboardHandler != null )
					{
						return clipboardHandler.createExportTransferable( rootElement.selection );
					}
				}
				return null;
			}
		}
		
		@Override
		public void exportDone(JComponent component, Transferable data, int action)
		{
			if ( drop != null )
			{
				rootElement.detachElementPreview( data );
				drop.getSourceElement().getDndHandler().exportDone( drop.getSourceElement(), data, action );
				drop = null;
			}
			else
			{
				LSRegion region = rootElement.getSelectionRegion();
				if ( region != null )
				{
					ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
					if ( clipboardHandler != null )
					{
						clipboardHandler.exportDone( rootElement.selection, rootElement.getTarget(), data, action );
					}
				}
			}
		}

		
		
		
		@Override
		public boolean canImport(TransferHandler.TransferSupport transfer)
		{
			try
			{
				if ( transfer.isDrop() )
				{
					return swingDndCanImport( transfer );
				}
				else
				{
					LSRegion region = rootElement.getTargetRegion();
					if ( region != null )
					{
						ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
						if ( clipboardHandler != null )
						{
							return clipboardHandler.canImport( rootElement.getTarget(), rootElement.selection, new DataTransfer( transfer ) );
						}
					}
					return false;
				}
			}
			catch (Throwable e)
			{
				rootElement.notifyExceptionDuringEventHandler( "PresTransferHandler", "canImport", e );
				return false;
			}
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport transfer)
		{
			try
			{
				if ( transfer.isDrop() )
				{
					return swingDndImportData( transfer );
				}
				else
				{
					LSRegion region = rootElement.getTargetRegion();
					if ( region != null )
					{
						ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
						if ( clipboardHandler != null )
						{
							return clipboardHandler.importData( rootElement.getTarget(), rootElement.selection, new DataTransfer( transfer ) );
						}
					}
					return false;
				}
			}
			catch (Throwable e)
			{
				rootElement.notifyExceptionDuringEventHandler( "PresTransferHandler", "importData", e );
				return false;
			}
		}

		public void initiateExportDnd(DndDropLocal drop)
		{
			this.drop = drop;
		}
	}
	
	
	


	//
	//
	// TYPESETTING SYSTEM PROFILING
	//
	//
		
	public static class TypesetProfileMeasurement
	{
		private double typesetTime;
		
		public TypesetProfileMeasurement()
		{
		}
		
		public TypesetProfileMeasurement(double typesetTime)
		{
			this.typesetTime = typesetTime;
		}
		
		
		public double getTypesetTime()
		{
			return typesetTime;
		}
	}
	
	
	private static AttributeColumn typesetTimeColumn = null;
	
	private static ObjectListTableEditor typesetProfileTableEditor = null;
	
	
	
	private static ObjectListTableEditor getTypesetProfileTableEditor()
	{
		if ( typesetProfileTableEditor == null )
		{
			typesetTimeColumn = new AttributeColumn( "Typeset", Py.newString( "typesetTime" ) );
			
			typesetProfileTableEditor = new ObjectListTableEditor(
					Arrays.asList( new Object[] { typesetTimeColumn } ),
					TypesetProfileMeasurement.class, true, true, false, false );
		}
		return typesetProfileTableEditor;
	}

	
	public static class TypesetProfile implements ObjectListInterface, Presentable
	{
		private ArrayList<TypesetProfileMeasurement> measurements = new ArrayList<TypesetProfileMeasurement>();
		private IncrementalValueMonitor incr = new IncrementalValueMonitor();
		
		
		public TypesetProfile()
		{
		}
		
		
		public void addMeasurement(TypesetProfileMeasurement m)
		{
			measurements.add( m );
			incr.onChanged();
		}
		

		@Override
		public int size()
		{
			incr.onAccess();
			return measurements.size();
		}

		@Override
		public Object get(int i)
		{
			incr.onAccess();
			return measurements.get( i );
		}

		@Override
		public void append(Object x)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeRange(int start, int end)
		{
			throw new UnsupportedOperationException();
		}


		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return getTypesetProfileTableEditor().editTable( this );
		}
	}
	
	


	
	
	
	public static class RootElement extends LSBin implements SelectionListener, DndController
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
		private LSContentLeafEditable currentCaretLeaf = null;
		
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
		
		
		
		public RootElement(PresentationComponent component)
		{
			super();
			
			this.component = component;
			
			layoutNode = new LayoutNodeRootElement( this );
			
			rootElement = this;
			
			windowSize = new Vector2( 1.0, 1.0 );
			
			inputTable = new InputTable( this, this, component );
			rootSpaceMouse = inputTable.getMouse();
			
			bAllocationRequired = true;
			
			TargetListener caretTargetListener = new TargetListener()
			{
				public void targetChanged(Target t)
				{
					caretChanged( (Caret)t );
				}
			};
			
			caret = new Caret();
			caret.addTargetListener( caretTargetListener );
			
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
		// CARET METHODS
		//
		//

		public Caret getCaret()
		{
			return caret;
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
			boolean wasCaret = target == null;
			target = t;
			boolean isCaret = target == null;
			if ( isCaret && !wasCaret )
			{
				notifyCaretNowCurrentTarget();
			}
			else if ( !isCaret && wasCaret )
			{
				notifyCaretNoLongerCurrentTarget();
			}
			queueFullRedraw();
		}
		
		public void setCaretAsTarget()
		{
			boolean wasCaret = target == null;
			target = null;
			if ( !wasCaret )
			{
				notifyCaretNowCurrentTarget();
			}
			queueFullRedraw();
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

				
		
		
		//
		// Immediate event queue methods
		//
		
		public void queueImmediateEvent(Runnable event)
		{
			if ( waitingImmediateEvents == null  &&  immediateEventDispatcher == null )
			{
				final RootElement root = this;
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
		// Queue redraw
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
		
		
		

		
		//
		// Queue reallocation
		//
		
		public void queueReallocation()
		{
			bAllocationRequired = true;
			component.notifyQueueReallocation();
			queueFullRedraw();
		}
		
		
		

		//
		// Allocation
		//
		
		private void performAllocation()
		{
			if ( bAllocationRequired )
			{
				long t1 = System.nanoTime();
				
				LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();

				// Get X requisition
				LReqBoxInterface reqX = rootLayout.refreshRequisitionX();
				
				// Allocate X
				double prevWidth = rootLayout.getAllocWidth();
				rootLayout.allocateX( reqX, 0.0, windowSize.x );
				rootLayout.refreshAllocationX( prevWidth );
				
				// Get Y requisition
				LReqBoxInterface reqY = rootLayout.refreshRequisitionY();
				
				// Allocate Y
				LAllocV prevAllocV = rootLayout.getAllocV();
				//rootLayout.allocateY( reqY, 0.0, reqY.getReqHeight() );
				rootLayout.allocateY( reqY, 0.0, windowSize.y );
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
		

		
		
		private Dimension getMinimumSize()
		{
			if ( isRealised() )
			{
				//performAllocationForSpaceRequirements();
				performAllocation();
				LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();
				return new Dimension( (int)( rootLayout.getReqMinWidth() + 1.0 ),  (int)( rootLayout.getReqHeight() + 1.0 ) );
			}
			else
			{
				return new Dimension( 1, 1 );
			}
		}
		
		private Dimension getPreferredSize()
		{
			if ( isRealised() )
			{
				//performAllocationForSpaceRequirements();
				performAllocation();
				LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();
				return new Dimension( (int)( rootLayout.getReqPrefWidth() + 1.0 ),  (int)( rootLayout.getReqHeight() + 1.0 ) );
			}
			else
			{
				return new Dimension( 1, 1 );
			}
		}
		
		private Dimension getMaximumSize()
		{
			return getPreferredSize();
		}
		
		
		
		private void componentFocusGained()
		{
			hasComponentFocus = true;
			Target target = getTarget();
			if ( target == getCaret()  &&  !target.isValid() )
			{
				queueMoveCaretToStartOfDocument();
			}
			
			queueFullRedraw();
		}
		
		private void componentFocusLost()
		{
			hasComponentFocus = false;
			queueFullRedraw();
		}
		
		
		
		
		
		
		//
		// Hierarchy methods
		//
		
		protected void setRootElement(RootElement root)
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
							caret.moveToStartOfElement( RootElement.this );
						}
						
						caretMoveToStartQueued = false;
					}
				};
			
				SwingUtilities.invokeLater( putCaretAtStart );
				
				caretMoveToStartQueued = true;
			}
		}

		
		
		
		//
		// Event handling
		//
		
		protected void configureEvent(Vector2 size)
		{
			if ( !size.equals( windowSize ) )
			{
				windowSize = size;
				bAllocationRequired = true;
				queueReallocation();
			}
			emitImmediateEvents();
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
			
			// Fill background
			graphics.setColor( Color.WHITE );
			graphics.fill( exposeArea );
			
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
		// MOUSE EVENTS
		//
		//
		
		
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
		
		
		public void dndInitiateDrag(DndDropLocal drop, MouseEvent mouseEvent, int requestedAction)
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
		
		private void detachElementPreview(Transferable transferable)
		{
			// Attach element previews
			ElementPreview preview = getElementPreview( transferable );
			if ( preview != null )
			{
				preview.detach();
			}
		}
		
		private boolean swingDndCanImport(TransferHandler.TransferSupport transfer)
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
		
		private boolean swingDndImportData(TransferHandler.TransferSupport transfer)
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
		
		
		
		protected void setPointerCursor(Cursor cursor)
		{
			component.setCursor( cursor );
		}
		
		protected void setPointerCursorDefault()
		{
			component.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
		}
		
		
		public InputTable getInputTable()
		{
			return inputTable;
		}
		
		public Keyboard getKeyboard()
		{
			return keyboard;
		}
		
		public void elementUnrealised(LSElement element)
		{
			inputTable.onElementUnrealised( element );
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
		// CARET METHODS
		//
		//
		
		private void caretChanged(Caret c)
		{
			assert c == caret;
			
			if ( c.isValid()  &&  getTarget() == c )
			{
				LSContentLeafEditable caretLeaf = c.getElement();
				
				if ( caretLeaf != currentCaretLeaf )
				{
					ArrayList<LSElement> prevPath = null, curPath = null;
					if ( currentCaretLeaf != null )
					{
						prevPath = currentCaretLeaf.getElementPathToRoot();
					}
					else
					{
						prevPath = new ArrayList<LSElement>();
					}
					
					if ( caretLeaf != null )
					{
						curPath = caretLeaf.getElementPathToRoot();
					}
					else
					{
						curPath = new ArrayList<LSElement>();
					}
					
	
					int prevPathDivergeIndex = prevPath.size() - 1, curPathDivergeIndex = curPath.size() - 1;
					for (int i = prevPath.size() - 1, j = curPath.size() - 1; i >= 0  &&  j >= 0;  i--, j--)
					{
						LSElement prev = prevPath.get( i ), cur = curPath.get( j );
						if ( prev != cur )
						{
							// Found indices where paths diverge
							prevPathDivergeIndex = i;
							curPathDivergeIndex = j;
							
							break;
						}
					}
					
					
					// Send leave events
					for (int x = 0; x <= prevPathDivergeIndex; x++)
					{
						prevPath.get( x ).handleCaretLeave( c );
					}
					
					currentCaretLeaf = caretLeaf;
	
					for (int x = curPathDivergeIndex; x >= 0; x--)
					{
						curPath.get( x ).handleCaretEnter( c );
					}
				}
				
				
				queueFullRedraw();
			}
		}
		
		private void notifyCaretNowCurrentTarget()
		{
			if ( caret.isValid() )
			{
				LSContentLeafEditable caretLeaf = caret.getElement();
				
				ArrayList<LSElement> path = caretLeaf.getElementPathFromRoot();
				for (LSElement e: path)
				{
					e.handleCaretEnter( caret );
				}
				
				currentCaretLeaf = caretLeaf;
			}
		}
		
		private void notifyCaretNoLongerCurrentTarget()
		{
			if ( caret.isValid() )
			{
				LSContentLeafEditable caretLeaf = caret.getElement();
				
				ArrayList<LSElement> path = caretLeaf.getElementPathToRoot();
				for (LSElement e: path)
				{
					e.handleCaretLeave( caret );
				}
				
				currentCaretLeaf = null;
			}
		}
		
		
		protected void queueEnsureVisible(LSElement element)
		{
			ensureVisibilityElement = element;
		}
		
		protected void caretGrab(LSElement element)
		{
			caret.grab( element );
		}
		
		protected void caretUngrab(LSElement element)
		{
			caret.ungrab( element );
		}
		
		
		
		
		protected PresentationPopup createPopupPresentation(LSElement popupContents, Point2 localPos, Corner popupAnchor, boolean bCloseOnLoseFocus, boolean bRequestFocus)
		{
			return component.createPopupPresentation( popupContents, (int)( localPos.x + 0.5 ), (int)( localPos.y + 0.5 ), popupAnchor, bCloseOnLoseFocus, bRequestFocus );
		}
		
		public PresentationPopup createPopupAtMousePosition(LSElement popupContents, Corner popupAnchor, boolean bCloseOnLoseFocus, boolean bRequestFocus)
		{
			Point mouse = component.getMousePosition();
			return component.createPopupPresentation( popupContents, mouse.x, mouse.y, popupAnchor, bCloseOnLoseFocus, bRequestFocus );
		}

		
		
		//
		//
		// SELECTION METHODS (private)
		//
		//
		
		
		
		public void selectionChanged(Selection s)
		{
			if ( s == selection )
			{
				queueFullRedraw();
			}
		}
		
		
		
		//
		//
		// SELECTION EDIT METHODS
		//
		//
		
		public boolean deleteSelection()
		{
			LSRegion selectionRegion = getSelectionRegion();
			if ( selectionRegion != null )
			{
				ClipboardHandlerInterface clipboardHandler = selectionRegion.getClipboardHandler();
				if ( clipboardHandler != null )
				{
					if ( selection instanceof TextSelection )
					{
						TextSelection ts = (TextSelection)selection;
						if ( caret.getMarker().equals( ts.getEndMarker() ) )
						{
							caret.moveTo( ts.getStartMarker() );
						}
						caret.makeCurrentTarget();
					}
					return clipboardHandler.deleteSelection( selection, getTarget() );
				}
			}
			
			return false;
		}

		public boolean replaceSelectionWithText(String replacement)
		{
			LSRegion selectionRegion = getSelectionRegion();
			if ( selectionRegion != null )
			{
				ClipboardHandlerInterface clipboardHandler = selectionRegion.getClipboardHandler();
				if ( clipboardHandler != null )
				{
					return clipboardHandler.replaceSelectionWithText( selection, getTarget(), replacement );
				}
			}
			
			return false;
		}
		
		
		
		
		//
		//
		// REGION METHODS
		//
		//
		
		protected LSRegion getTargetRegion()
		{
			Target target = getTarget();
			if ( target.isValid() )
			{
				return target.getElement().getRegion();
			}
			else
			{
				return null;
			}
		}
		
		protected LSRegion getSelectionRegion()
		{
			Selection selection = getSelection();
			return selection != null  ?  selection.getRegion()  :  null;
		}
		
		
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

	
	
	
	
	
	public RootElement rootElement;
	private boolean realised, bConfigured;
	private PresentationPopup containingPopup = null;
	
	
	public PresentationComponent()
	{
		this( null );
	}
	
	private PresentationComponent(PresentationPopup containingPopup)
	{
		super();
		
		this.containingPopup = containingPopup;
		
		rootElement = new RootElement( this );
		
		realised = false;
		bConfigured = false;
		
		addComponentListener( this );
		addMouseListener( this );
		addMouseMotionListener( this );
		addMouseWheelListener( this );
		addKeyListener( this );
		addHierarchyListener( this );
		addFocusListener( this );
		
		
		setFocusable( true );
		setRequestFocusEnabled( true );
		setFocusTraversalKeysEnabled( false );
		
		setTransferHandler( new PresAreaTransferHandler() );
	}
	
	
	public RootElement getRootElement()
	{
		return rootElement;
	}
	
	
	
	public void setPageController(PageController pageController)
	{
		rootElement.setPageController( pageController );
	}
	
	public void setChild(LSElement element)
	{
		rootElement.setChild( element );
	}
	
	public LSElement getChild()
	{
		return rootElement.getChild();
	}
	
	
	@Override
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

		RenderingHints aa = new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		RenderingHints taa = new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		g2.addRenderingHints( aa );
		g2.addRenderingHints( taa );
		rootElement.exposeEvent( g2, new Rectangle2D.Double( 0.0, 0.0, (double)getWidth(), (double)getHeight()) );
	}

	public void mousePressed(MouseEvent e)
	{
		rootElement.mouseDownEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), getKeyModifiers( e ) );
	}

	public void mouseReleased(MouseEvent e)
	{
		rootElement.mouseUpEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), getKeyModifiers( e ) );
	}

	
	public void mouseClicked(MouseEvent e)
	{
		rootElement.mouseClicked( getButton( e ), e.getClickCount(), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
	}

	
	public void mouseMoved(MouseEvent e)
	{
		rootElement.mouseMotionEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), getKeyModifiers( e ), e );
	}

	public void mouseDragged(MouseEvent e)
	{
		rootElement.mouseDragEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), getKeyModifiers( e ), e );
	}

	public void mouseEntered(MouseEvent e)
	{
		rootElement.mouseEnterEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), getKeyModifiers( e ) );
	}

	public void mouseExited(MouseEvent e)
	{
		rootElement.mouseLeaveEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), getKeyModifiers( e ) );
	}


	


	public void mouseWheelMoved(MouseWheelEvent e)
	{
		rootElement.mouseWheelEvent( new Point2( (double)e.getX(), (double)e.getY() ), e.getWheelRotation(), e.getUnitsToScroll(), getButtonModifiers( e ) );
	}
	
	
	
	public Dimension getMinimumSize()
	{
		if ( isMinimumSizeSet() )
		{
			return super.getMinimumSize();
		}
		else
		{
			return rootElement.getMinimumSize();
		}
	}
	
	public Dimension getPreferredSize()
	{
		if ( isPreferredSizeSet() )
		{
			return super.getPreferredSize();
		}
		else
		{
			return rootElement.getPreferredSize();
		}
	}
	
	public Dimension getMaximumSize()
	{
		if ( isMaximumSizeSet() )
		{
			return super.getMaximumSize();
		}
		else
		{
			return rootElement.getMaximumSize();
		}
	}
	
	
	private void notifyQueueReallocation()
	{
		if ( !isMinimumSizeSet()  ||  !isPreferredSizeSet()  ||  !isMaximumSizeSet()  ||  isPopup() )
		{
			invalidate();
		}
	}
	
	
	

	private boolean swingDndCanImport(TransferHandler.TransferSupport transfer)
	{
		return rootElement.swingDndCanImport( transfer );
	}
	
	private boolean swingDndImportData(TransferHandler.TransferSupport transfer)
	{
		return rootElement.swingDndImportData( transfer );
	}
	

	
	public void keyPressed(KeyEvent e)
	{
		rootElement.keyPressEvent( e, getKeyModifiers( e ) );
	}


	public void keyReleased(KeyEvent e)
	{
		rootElement.keyReleaseEvent( e, getKeyModifiers( e ) );
	}


	public void keyTyped(KeyEvent e)
	{
		rootElement.keyTypedEvent( e, getKeyModifiers( e ) );
	}
	
	

	
	public void componentResized(ComponentEvent e)
	{
		rootElement.configureEvent( new Vector2( (double)getWidth(), (double)getHeight() ) );
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
		sendUnrealiseEvents();
	}
	
	
	public void focusGained(FocusEvent e)
	{
		rootElement.componentFocusGained();
	}
	
	public void focusLost(FocusEvent e)
	{
		rootElement.componentFocusLost();
	}



	public void hierarchyChanged(HierarchyEvent e)
	{
		sendRealiseEvents();
	}
	
	
	private void initialise()
	{
		if ( !bConfigured )
		{
			rootElement.configureEvent( new Vector2( (double)getWidth(), (double)getHeight() ) );
			bConfigured = true;
		}
	}
	
	
	private void sendRealiseEvents()
	{
		if ( !realised )
		{
			initialise();
			rootElement.realiseEvent();
			
			realised = true;
		}
	}

	
	private void sendUnrealiseEvents()
	{
		if ( realised )
		{
			rootElement.unrealiseEvent();
			
			realised = false;
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
	
	private static int getButtonModifiers(InputEvent e)
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
		
		return modifiers;
	}

	private static int getKeyModifiers(InputEvent e)
	{
		int modifiers = 0;
		
		if ( e.isControlDown() )
		{
			modifiers |= Modifier.CTRL;
		}
		
		if ( e.isShiftDown() )
		{
			modifiers |= Modifier.SHIFT;
		}
		
		if ( e.isAltDown() )
		{
			modifiers |= Modifier.ALT;
		}
		
		if ( e.isAltGraphDown() )
		{
			modifiers |= Modifier.ALT_GRAPH;
		}
		
		return modifiers;
	}
	
	
	
	//
	//
	// POPUP METHODS
	//
	//
	
	public boolean isPopup()
	{
		return containingPopup != null;
	}
	
	
	public void closeContainingPopupChain()
	{
		if ( isPopup() )
		{
			containingPopup.chain.closeChain();
		}
		else
		{
			throw new RuntimeException( "This presentation component is not contained within a popup" );
		}
	}
	
	private PopupChain getPopupChain()
	{
		if ( containingPopup != null )
		{
			return containingPopup.chain;
		}
		else
		{
			return null;
		}
	}
	
	private PopupChain createPopupChain()
	{
		return new PopupChain( this );
	}
	
	
	private PresentationPopup createPopupPresentation(LSElement popupContents, int x, int y, Corner popupAnchor, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		// Offset the popup position by the location of this presentation component on the screen
		Point locOnScreen = getLocationOnScreen();
		x += locOnScreen.x;
		y += locOnScreen.y;
		
		// Get the popup chain that this presentation component is a member of
		PopupChain chain = getPopupChain();
		if ( chain != null )
		{
			// Close any child popups
			chain.closeAllChildrenOf( containingPopup );
		}
		
		Window ownerWindow = null;
		if ( chain == null )
		{
			// No chain; create a new one, rooted here
			chain = createPopupChain();
			
			// Get the owning window of this presentation component
			ownerWindow = SwingUtilities.getWindowAncestor( this );
		}
		else
		{
			// Get the root presentation component
			PresentationComponent root = chain.owner;
			
			// Get the owning window of the root presentation component
			ownerWindow = SwingUtilities.getWindowAncestor( root );
		}

		return new PresentationPopup( chain, ownerWindow, this, popupContents, x, y, popupAnchor, bCloseOnLoseFocus, bRequestFocus );
	}
}
