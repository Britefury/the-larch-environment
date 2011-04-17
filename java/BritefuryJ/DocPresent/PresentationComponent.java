//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.DocPresent.Clipboard.DataTransfer;
import BritefuryJ.DocPresent.Input.DndController;
import BritefuryJ.DocPresent.Input.DndDropLocal;
import BritefuryJ.DocPresent.Input.DndDropSwing;
import BritefuryJ.DocPresent.Input.InputTable;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Input.Pointer;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.Keyboard.Keyboard;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeRootElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Selection.SelectionListener;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.DocPresent.Selection.TextSelectionManager;
import BritefuryJ.DocPresent.Target.Target;
import BritefuryJ.DocPresent.Target.TargetListener;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class PresentationComponent extends JComponent implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener
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
		private boolean bOpen;
		
		private PresentationPopup(PopupChain popupChain, Window ownerWindow, PresentationComponent parentComponent, DPElement popupContents, int x, int y,
				boolean bCloseOnLoseFocus, boolean bRequestFocus)
		{
			chain = popupChain;
			chain.addPopup( this );
			bOpen = true;
			
			
			// Create the popup window
			popupWindow = new JWindow( ownerWindow );
			if ( bRequestFocus )
			{
				popupWindow.setAlwaysOnTop( true );
				popupWindow.setFocusable( true );
			}
			
			// Create a presentation component for the popup contents, and add them
			popupComponent = new PresentationComponent( this );
			popupComponent.getRootElement().setChild( popupContents.layoutWrap() );
			
			popupWindow.add( popupComponent );
			popupWindow.setLocation( x, y );
			popupWindow.pack();
			popupWindow.setVisible( true );
			if ( bRequestFocus )
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
					if ( bOpen )
					{
						if ( !chain.popupHasChild( PresentationPopup.this ) )
						{
							chain.closeChainNotContainingPointers();
						}
					}
				}
			};
			
			if ( bCloseOnLoseFocus )
			{
				popupWindow.addWindowFocusListener( focusListener );
			}
		}
		
		
		public void closePopup()
		{
			bOpen = false;
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
		
		
		
		
		public int getSourceActions(JComponent component)
		{
			if ( drop != null )
			{
				return drop.getSourceDropActions();
			}
			else
			{
				DPRegion region = rootElement.getSelectionRegion();
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
		
		public Transferable createTransferable(JComponent component)
		{
			if ( drop != null )
			{
				return drop.getTransferable();
			}
			else
			{
				DPRegion region = rootElement.getSelectionRegion();
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
		
		public void exportDone(JComponent component, Transferable data, int action)
		{
			if ( drop != null )
			{
				drop.getSourceElement().getDndHandler().exportDone( drop.getSourceElement(), data, action );
			}
			else
			{
				DPRegion region = rootElement.getSelectionRegion();
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
					DPRegion region = rootElement.getCaretRegion();
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
					DPRegion region = rootElement.getCaretRegion();
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

		public void beginExportDnd(DndDropLocal drop)
		{
			this.drop = drop;
		}

		public void endExportDnd()
		{
			drop = null;
		}
	}
	
	
	

	
	
	public static class RootElement extends DPBin implements SelectionListener, DndController
	{
		private PresentationComponent component;
		
		private Vector2 windowSize;
		
		private Pointer rootSpaceMouse;
		private Keyboard keyboard;
		private InputTable inputTable;
		
		private Runnable immediateEventDispatcher;
		
		private boolean bAllocationRequired;
		
		
		protected WeakHashMap<DPContentLeafEditable, WeakHashMap<Marker, Object>> markersByLeaf = new WeakHashMap<DPContentLeafEditable, WeakHashMap<Marker, Object>>();
		
		private Caret caret;
		private DPContentLeafEditable currentCaretLeaf = null;
		private boolean bLastMousePressPositionedCaret = false;
		
		private Target target;
		
		private TextSelectionManager selectionManager;
		private Selection selection;
		
		
		private boolean bStructureRefreshQueued;
		private DPElement ensureVisibilityElement;
		
		
		protected ArrayList<Runnable> waitingImmediateEvents;			// only initialised when non-empty; otherwise null

		
		
		protected PresentationComponent metaElementComponent;
		
			
		protected PageController pageController;
		
		
		private Log log;

		
		
		
		public RootElement(PresentationComponent component)
		{
			super();
			
			this.component = component;
			
			layoutNode = new LayoutNodeRootElement( this );
			
			rootElement = this;
			
			windowSize = new Vector2();
			
			inputTable = new InputTable( this, this, component );
			rootSpaceMouse = inputTable.getMouse();
			
			bAllocationRequired = true;
			
			TargetListener caretTargetListener = new TargetListener()
			{
				@Override
				public void targetChanged(Target t)
				{
					caretChanged( (Caret)t );
				}
			};
			
			caret = new Caret();
			caret.addTargetListener( caretTargetListener );
			
			target = null;
			
			selectionManager = new TextSelectionManager( this );
			
			keyboard = new Keyboard( caret, selectionManager );

			bStructureRefreshQueued = false;
		}
		
		
		
		//
		//
		// Presentation tree cloning
		//
		//
		
		public DPElement clonePresentationSubtree()
		{
			DPElement child = getChild();
			return child != null  ?  child.clonePresentationSubtree()  :  null;
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
		
		
		
		public void setLog(Log log)
		{
			this.log = log;
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
			target = t;
			queueFullRedraw();
		}
		
		public void setCaretAsTarget()
		{
			target = null;
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
			DPContainer commonRootContainer = s.getCommonRootContainer();
			ArrayList<DPElement> startPath = s.getStartPathFromCommonRoot();
			ArrayList<DPElement> endPath = s.getEndPathFromCommonRoot();
			
			if ( commonRootContainer != null )
			{
				StringBuilder builder = new StringBuilder();

				commonRootContainer.getTextRepresentationBetweenPaths( builder, s.getStartMarker(), startPath, 0, s.getEndMarker(), endPath, 0 );
			
				return builder.toString();
			}
			else
			{
				DPContentLeafEditable commonRoot = (DPContentLeafEditable)s.getCommonRoot();
				return commonRoot.getTextRepresentationBetweenMarkers( s.getStartMarker(), s.getEndMarker() );
			}
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
				waitingImmediateEvents = new ArrayList<Runnable>();
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
		
		@SuppressWarnings("unchecked")
		private void emitImmediateEvents()
		{
			if ( waitingImmediateEvents != null )
			{
				List<Runnable> events = (List<Runnable>)waitingImmediateEvents.clone();
				
				for (Runnable event: events)
				{
					event.run();
				}
				
				waitingImmediateEvents = null;
			}
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
				double prevWidth = rootLayout.getAllocationX();
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
				if ( log != null  &&  log.isRecording() )
				{
					log.log( new LogEntry( "PresentationTypesetPerformance" ).hItem( "typesetTime", typesetTime ) );
				}
				
				if ( ensureVisibilityElement != null )
				{
					ensureVisibilityElement.ensureVisible();
					ensureVisibilityElement = null;
				}
			}
		}
		

		
		
		private void performAllocationForSpaceRequirements()
		{
			if ( bAllocationRequired )
			{
				LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();
	
				// Get X requisition
				LReqBoxInterface reqX = rootLayout.refreshRequisitionX();
				
				// Allocate X
				double prevWidth = rootLayout.getAllocationX();
				rootLayout.allocateX( reqX, 0.0, windowSize.x );
				rootLayout.refreshAllocationX( prevWidth );
				
				// Get Y requisition
				LReqBoxInterface reqY = rootLayout.refreshRequisitionY();
				
				// Allocate Y
				LAllocV prevAllocV = rootLayout.getAllocV();
				//rootLayout.allocateY( reqY, 0.0, reqY.getReqHeight() );
				rootLayout.allocateY( reqY, 0.0, windowSize.y );
				rootLayout.refreshAllocationY( prevAllocV );
			}
		}
		
		private Dimension getMinimumSize()
		{
			performAllocationForSpaceRequirements();
			LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();
			return new Dimension( (int)( rootLayout.getReqMinWidth() + 1.0 ),  (int)( rootLayout.getReqHeight() + 1.0 ) );
		}
		
		private Dimension getPreferredSize()
		{
			performAllocationForSpaceRequirements();
			LayoutNodeRootElement rootLayout = (LayoutNodeRootElement)getLayoutNode();
			return new Dimension( (int)( rootLayout.getReqPrefWidth() + 1.0 ),  (int)( rootLayout.getReqHeight() + 1.0 ) );
		}
		
		private Dimension getMaximumSize()
		{
			return getPreferredSize();
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
				if ( !bStructureRefreshQueued )
				{
					final Runnable putCaretAtStart = new Runnable()
					{
						public void run()
						{
							if ( !caret.isValid() )
							{
								DPContentLeafEditable leaf = getLayoutNode().getLeftEditableContentLeaf();
								if ( leaf != null )
								{
									caret.moveTo( leaf.markerAtStart() );
								}
							}
							
							bStructureRefreshQueued = false;
						}
					};
				
					bStructureRefreshQueued = true;
					SwingUtilities.invokeLater( putCaretAtStart );
					
					
					// Handle selections
					if ( selection != null )
					{
						selection.onPresentationTreeStructureChanged();
					}
				}
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
			drawSelection( graphics );
			handleDraw( graphics, new AABox2( topLeftRootSpace, bottomRightRootSpace) );
			//graphics.setTransform( transform );
			drawTarget( graphics );
			
			// Emit any immediate events
			emitImmediateEvents();
		}
		
		
		private void drawTarget(Graphics2D graphics)
		{
			Target t = getTarget();
			t.draw( graphics );
		}
		
		
		private void drawSelection(Graphics2D graphics)
		{
			if ( selection != null )
			{
				selection.draw( graphics );
			}
		}
		
		
		public void drawTextRange(Graphics2D graphics, Marker startMarker, List<DPElement> startPath, Marker endMarker, List<DPElement> endPath)
		{
			startPath.get( 0 ).drawSubtreeSelection( graphics, startMarker, startPath, endMarker, endPath );
		}
		
		
		
		
		//
		//
		// MOUSE EVENTS
		//
		//
		
		
		protected void mouseDownEvent(int button, Point2 windowPos, int buttonModifiers, int keyModifiers)
		{
			selectionManager.mouseSelectionReset();
			
			component.grabFocus();
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			rootSpaceMouse.setKeyModifiers( keyModifiers );
			int modifiers = rootSpaceMouse.getModifiers();
			
			boolean bHandled = rootSpaceMouse.buttonDown( windowPos, button );

			bLastMousePressPositionedCaret = false;

			if ( !bHandled  &&  button == 1  &&  ( modifiers & ( Modifier.ALT | Modifier.ALT_GRAPH | Modifier.CTRL | Modifier.SHIFT ) )  ==  0 )
			{
				Marker editableMarker = getEditableMarkerClosestToLocalPoint( windowPos );
				if ( editableMarker != null )
				{
					caret.moveTo( editableMarker );
					caret.makeCurrentTarget();
					bLastMousePressPositionedCaret = true;
				}

				Marker selectableMarker = getSelectableMarkerClosestToLocalPoint( windowPos );
				if ( selectableMarker != null )
				{
					selectionManager.mouseSelectionBegin( selectableMarker );
				}
			}

			emitImmediateEvents();
		}
		
		protected void mouseUpEvent(int button, Point2 windowPos, int buttonModifiers, int keyModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			rootSpaceMouse.setKeyModifiers( keyModifiers );

			
			rootSpaceMouse.buttonUp( windowPos, button );

			if ( button == 1 )
			{
				selectionManager.mouseSelectionReset();
			}
			
			emitImmediateEvents();
		}
		
		
		protected void mouseClicked(int button, int clickCount, Point2 windowPos, int buttonModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			
			int modifiers = rootSpaceMouse.getModifiers();
			
			boolean bHandled = false;
			
			if ( bLastMousePressPositionedCaret  &&  button == 1  &&  ( modifiers & ( Modifier.ALT | Modifier.ALT_GRAPH | Modifier.CTRL | Modifier.SHIFT ) )  ==  0 )
			{
				DPContentLeafEditable selectableLeaf = (DPContentLeafEditable)getSelectableLeafClosestToLocalPoint( windowPos );
				if ( selectableLeaf != null )
				{
					DPElement elementToSelect = null;
					
					if ( clickCount == 2 )
					{
						elementToSelect = selectableLeaf;
					}
					else if ( clickCount >= 3 )
					{
						elementToSelect = selectableLeaf.getSegment();
					}
						
					if ( elementToSelect != null )
					{
						caret.moveTo( elementToSelect.markerAtEnd() );
						caret.makeCurrentTarget();
						selectionManager.mouseSelectionReset();
						selectionManager.selectElement( elementToSelect );
						bHandled = true;
					}
				}
			}

			if ( !bHandled )
			{
				rootSpaceMouse.buttonClicked( windowPos, button, clickCount );
			}
		}


		
		protected void mouseMotionEvent(Point2 windowPos, int buttonModifiers, int keyModifiers, MouseEvent mouseEvent)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			rootSpaceMouse.setKeyModifiers( keyModifiers );
			
			rootSpaceMouse.motion( windowPos, mouseEvent );

			emitImmediateEvents();
		}

		protected void mouseDragEvent(Point2 windowPos, int buttonModifiers, int keyModifiers, MouseEvent mouseEvent)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			rootSpaceMouse.setKeyModifiers( keyModifiers );
			
			if ( selectionManager.isMouseDragInProgress() )
			{
				Marker editableMarker = getEditableMarkerClosestToLocalPoint( windowPos );
				if ( editableMarker != null )
				{
					caret.moveTo( editableMarker );
					caret.makeCurrentTarget();
				}

				Marker selectableMarker = getSelectableMarkerClosestToLocalPoint( windowPos );
				if ( selectableMarker != null )
				{
					selectionManager.mouseSelectionDrag( selectableMarker );
				}
			}
			
			rootSpaceMouse.drag( windowPos, mouseEvent );

			emitImmediateEvents();
		}



		
		protected void mouseEnterEvent(Point2 windowPos, int buttonModifiers, int keyModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			rootSpaceMouse.setKeyModifiers( keyModifiers );
			
			rootSpaceMouse.enter( windowPos );
			
			emitImmediateEvents();
		}

		protected void mouseLeaveEvent(Point2 windowPos, int buttonModifiers, int keyModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			rootSpaceMouse.setKeyModifiers( keyModifiers );
			
			rootSpaceMouse.leave( windowPos );
			
			emitImmediateEvents();
		}


		protected void mouseWheelEvent(Point2 windowPos, int wheelClicks, int unitsToScroll, int buttonModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			
			rootSpaceMouse.scroll( 0, -wheelClicks );
			emitImmediateEvents();
		}
		
		
		
		
		protected boolean keyPressEvent(KeyEvent event, int keyModifiers)
		{
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
			PresentationComponent.PresAreaTransferHandler xferHandler = getDndTransferHandler();
			xferHandler.beginExportDnd( drop );
			xferHandler.exportAsDrag( component, mouseEvent, requestedAction );
			xferHandler.endExportDnd();
		}

		
		private boolean swingDndCanImport(TransferHandler.TransferSupport transfer)
		{
			Point windowPos = transfer.getDropLocation().getDropPoint();
			Point2 rootPos = new Point2( windowPos.x, windowPos.y );
			List<PointerInputElement.DndTarget> targets = PointerInputElement.getDndTargets( this, rootPos );
			for (PointerInputElement.DndTarget target: targets)
			{
				if ( target.isNonLocalDest() )
				{
					PointerInputElement targetElement = target.getElement();
					Point2 targetPos = target.getElementSpacePos();

					DndDropSwing drop = new DndDropSwing( targetElement, targetPos, transfer );
					return targetElement.getDndHandler().canDrop( targetElement, drop );
				}
			}
			return false;
		}
		
		private boolean swingDndImportData(TransferHandler.TransferSupport transfer)
		{
			Point windowPos = transfer.getDropLocation().getDropPoint();
			Point2 rootPos = new Point2( windowPos.x, windowPos.y );
			List<PointerInputElement.DndTarget> targets = PointerInputElement.getDndTargets( this, rootPos );
			for (PointerInputElement.DndTarget target: targets)
			{
				if ( target.isNonLocalDest() )
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
		
		public void elementUnrealised(DPElement element)
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
		// CARET METHODS
		//
		//
		
		public void caretChanged(Caret c)
		{
			assert c == caret;
			
			DPContentLeafEditable caretLeaf = c.getElement();
			
			if ( caretLeaf != currentCaretLeaf )
			{
				ArrayList<DPElement> prevPath = null, curPath = null;
				if ( currentCaretLeaf != null )
				{
					prevPath = currentCaretLeaf.getElementPathToRoot();
				}
				else
				{
					prevPath = new ArrayList<DPElement>();
				}
				
				if ( caretLeaf != null )
				{
					curPath = caretLeaf.getElementPathToRoot();
				}
				else
				{
					curPath = new ArrayList<DPElement>();
				}
				

				int prevPathDivergeIndex = prevPath.size() - 1, curPathDivergeIndex = curPath.size() - 1;
				for (int i = prevPath.size() - 1, j = curPath.size() - 1; i >= 0  &&  j >= 0;  i--, j--)
				{
					DPElement prev = prevPath.get( i ), cur = curPath.get( j );
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
		
		
		protected void queueEnsureVisible(DPElement element)
		{
			ensureVisibilityElement = element;
		}
		
		protected void caretGrab(DPElement element)
		{
			caret.grab( element );
		}
		
		protected void caretUngrab(DPElement element)
		{
			caret.ungrab( element );
		}
		
		
		
		
		protected PresentationPopup createPopupPresentation(DPElement popupContents, Point2 localPos, boolean bCloseOnLoseFocus, boolean bRequestFocus)
		{
			return component.createPopupPresentation( popupContents, (int)( localPos.x + 0.5 ), (int)( localPos.y + 0.5 ), bCloseOnLoseFocus, bRequestFocus );
		}
		
		public PresentationPopup createPopupAtMousePosition(DPElement popupContents, boolean bCloseOnLoseFocus, boolean bRequestFocus)
		{
			Point mouse = component.getMousePosition();
			return component.createPopupPresentation( popupContents, mouse.x, mouse.y, bCloseOnLoseFocus, bRequestFocus );
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
		
		protected boolean deleteSelection()
		{
			DPRegion selectionRegion = getSelectionRegion();
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

		protected boolean replaceSelectionWithText(String replacement)
		{
			DPRegion selectionRegion = getSelectionRegion();
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
		
		protected DPRegion getCaretRegion()
		{
			if ( caret.isValid() )
			{
				return caret.getMarker().getElement().getRegion();
			}
			else
			{
				return null;
			}
		}
		
		protected DPRegion getSelectionRegion()
		{
			return selection != null  ?  selection.getRegion()  :  null;
		}
	}

	
	
	
	
	
	public RootElement rootElement;
	private boolean bShown, bConfigured;
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
	
	public void setChild(DPElement element)
	{
		rootElement.setChild( element );
	}
	
	public DPElement getChild()
	{
		return rootElement.getChild();
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
		if ( isPopup() )
		{
			Dimension s = rootElement.getMinimumSize();
			return s;
		}
		else
		{
			return super.getMinimumSize();
		}
	}
	
	public Dimension getPreferredSize()
	{
		if ( isPopup() )
		{
			Dimension s = rootElement.getPreferredSize();
			return s;
		}
		else
		{
			return super.getPreferredSize();
		}
	}
	
	public Dimension getMaximumSize()
	{
		if ( isPopup() )
		{
			Dimension s = rootElement.getMaximumSize();
			return s;
		}
		else
		{
			return super.getMaximumSize();
		}
	}
	
	
	private void notifyQueueReallocation()
	{
		if ( isPopup() )
		{
			revalidate();
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
			rootElement.configureEvent( new Vector2( (double)getWidth(), (double)getHeight() ) );
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
				rootElement.realiseEvent();
			}
			else
			{
				rootElement.unrealiseEvent();
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
	
	
	private PresentationPopup createPopupPresentation(DPElement popupContents, int x, int y, boolean bCloseOnLoseFocus, boolean bRequestFocus)
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
		
		if ( chain == null )
		{
			// No chain; create a new one, rooted here
			chain = createPopupChain();
			
			// Get the owning window of this presentation component
			Window ownerWindow = SwingUtilities.getWindowAncestor( this );

			return new PresentationPopup( chain, ownerWindow, this, popupContents, x, y, bCloseOnLoseFocus, bRequestFocus );
		}
		else
		{
			// Get the root presentation component
			PresentationComponent root = chain.owner;
			
			// Get the owning window of the root presentation component
			Window ownerWindow = SwingUtilities.getWindowAncestor( root );

			return new PresentationPopup( chain, ownerWindow, this, popupContents, x, y, bCloseOnLoseFocus, bRequestFocus );
		}
	}
}
