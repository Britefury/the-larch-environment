//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Caret.CaretListener;
import BritefuryJ.DocPresent.Clipboard.DataTransfer;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Input.DndDropLocal;
import BritefuryJ.DocPresent.Input.DndDropSwing;
import BritefuryJ.DocPresent.Input.InputTable;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Input.MousePointer;
import BritefuryJ.DocPresent.Input.Pointer;
import BritefuryJ.DocPresent.Input.PointerDndController;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeRootElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Selection.SelectionListener;
import BritefuryJ.DocPresent.Selection.SelectionManager;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.DefaultPerspective.DefaultPerspectiveStyleSheet;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

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
	
	
	
	public static class TypesettingPerformanceLogEntry extends LogEntry
	{
		private static final List<String> tags = Arrays.asList( new String[] { "typeset_performance" } ); 
		
		private double typesetTime;

		
		private TypesettingPerformanceLogEntry(double typesetTime)
		{
			super( tags );
			this.typesetTime = typesetTime;
		}
		
		
		public String getLogEntryTitle()
		{
			return "Presentation typesetting performance";
		}
		
		public DPElement createLogEntryPresentationContent(GSymFragmentViewContext ctx, DefaultPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			return PrimitiveStyleSheet.instance.staticText( "Typesetting time: " + typesetTime );
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
					EditHandler editHandler = region.getEditHandler();
					if ( editHandler != null )
					{	
						return editHandler.getExportActions( rootElement.selection );
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
					EditHandler editHandler = region.getEditHandler();
					if ( editHandler != null )
					{
						return editHandler.createExportTransferable( rootElement.selection );
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
					EditHandler editHandler = region.getEditHandler();
					if ( editHandler != null )
					{
						editHandler.exportDone( rootElement.selection, data, action );
					}
				}
			}
		}

		
		
		
		public boolean canImport(TransferHandler.TransferSupport transfer)
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
					EditHandler editHandler = region.getEditHandler();
					if ( editHandler != null )
					{
						return editHandler.canImport( rootElement.caret, rootElement.selection, new DataTransfer( transfer ) );
					}
				}
				return false;
			}
		}

		public boolean importData(TransferHandler.TransferSupport transfer)
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
					EditHandler editHandler = region.getEditHandler();
					if ( editHandler != null )
					{
						return editHandler.importData( rootElement.caret, rootElement.selection, new DataTransfer( transfer ) );
					}
				}
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
	
	
	

	
	
	public static class RootElement extends DPBin implements CaretListener, SelectionListener, PointerDndController
	{
		private PresentationComponent component;
		
		private Vector2 windowSize;
		
		private MousePointer rootSpaceMouse;
		private InputTable inputTable;
		
		private WeakHashMap<StateKeyListener, Object> stateKeyListeners;
		
		private Runnable immediateEventDispatcher;
		
		private boolean bAllocationRequired;
		
		protected WeakHashMap<DPContentLeafEditable, WeakHashMap<Marker, Object>> markersByLeaf = new WeakHashMap<DPContentLeafEditable, WeakHashMap<Marker, Object>>();
		private Caret caret;
		private DPContentLeafEditable currentCaretLeaf;
		
		private SelectionManager selectionManager;
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
			
			stateKeyListeners = new WeakHashMap<StateKeyListener, Object>();
			
			bAllocationRequired = true;
			
			caret = new Caret();
			caret.setCaretListener( this );
			
			currentCaretLeaf = null;

			selection = new Selection();
			selectionManager = new SelectionManager( selection );
			
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
		
		
		
		public JComponent getComponent()
		{
			return component;
		}
		
		public ImageObserver getImageObserver()
		{
			return component;
		}

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


		public void clearSelection()
		{
			selection.clear();
		}
		
		
		public boolean isSelectionValid()
		{
			return !selection.isEmpty();
		}
		
		
		
		public String getTextRepresentationInSelection(Selection s)
		{
			if ( s.isEmpty() )
			{
				return null;
			}
			else
			{
				DPContainer commonRoot = s.getCommonRoot();
				ArrayList<DPElement> startPath = s.getStartPathFromCommonRoot();
				ArrayList<DPElement> endPath = s.getEndPathFromCommonRoot();
				
				if ( commonRoot != null )
				{
					StringBuilder builder = new StringBuilder();

					commonRoot.getTextRepresentationBetweenPaths( builder, s.getStartMarker(), startPath, 0, s.getEndMarker(), endPath, 0 );
				
					return builder.toString();
				}
				else
				{
					return ((DPContentLeafEditable)startPath.get( 0 )).getTextRepresentationBetweenMarkers( s.getStartMarker(), s.getEndMarker() );
				}
			}
		}

		
		public ItemStream getLinearRepresentationInSelection(Selection s)
		{
			if ( s.isEmpty() )
			{
				return null;
			}
			else
			{
				DPContainer commonRoot = s.getCommonRoot();
				ArrayList<DPElement> startPath = s.getStartPathFromCommonRoot();
				ArrayList<DPElement> endPath = s.getEndPathFromCommonRoot();
				
				if ( commonRoot != null )
				{
					ItemStreamBuilder builder = new ItemStreamBuilder();

					commonRoot.getLinearRepresentationBetweenPaths( builder, s.getStartMarker(), startPath, 0, s.getEndMarker(), endPath, 0 );
				
					return builder.stream();
				}
				else
				{
					return ((DPContentLeafEditable)startPath.get( 0 )).getLinearRepresentationBetweenMarkers( s.getStartMarker(), s.getEndMarker() );
				}
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
				rootLayout.allocateX( reqX, 0.0, Math.max( reqX.getReqMinWidth(), windowSize.x ) );
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
				System.out.println( "DPPresentationArea.performAllocation(): TYPESET TIME = " + typesetTime  +  ", used memory = "  + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) );
				if ( log != null  &&  log.isRecording() )
				{
					log.log( new TypesettingPerformanceLogEntry( typesetTime ) );
				}
				
				if ( ensureVisibilityElement != null )
				{
					ensureVisibilityElement.ensureVisible();
					ensureVisibilityElement = null;
				}
			}
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
					selection.onStructureChanged();
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
			drawCaret( graphics );
			
			// Emit any immediate events
			emitImmediateEvents();
		}
		
		
		private void drawCaret(Graphics2D graphics)
		{
			if ( caret.isValid() )
			{
				DPContentLeafEditable element = caret.getElement();
				
				if ( element != null )
				{
					Color prevColour = graphics.getColor();
					graphics.setColor( Color.blue );
					element.drawCaret( graphics, caret );
					graphics.setColor( prevColour );
				}
			}
		}
		
		
		private void drawSelection(Graphics2D graphics)
		{
			if ( selection != null  &&  !selection.isEmpty() )
			{
				Marker startMarker = selection.getStartMarker();
				Marker endMarker = selection.getEndMarker();
				List<DPElement> startPath = selection.getStartPathFromCommonRoot();
				List<DPElement> endPath = selection.getEndPathFromCommonRoot();

				Color prevColour = graphics.getColor();
				graphics.setColor( Color.yellow );
				startPath.get( 0 ).drawSubtreeSelection( graphics, startMarker, startPath, endMarker, endPath );
				graphics.setColor( prevColour );
			}
		}
		
		
		
		
		//
		//
		// MOUSE EVENTS
		//
		//
		
		
		protected void mouseDownEvent(int button, Point2 windowPos, int buttonModifiers)
		{
			selectionManager.mouseSelectionReset();
			
			component.grabFocus();
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			int modifiers = rootSpaceMouse.getModifiers();
			
			boolean bHandled = rootSpaceMouse.buttonDown( windowPos, button );

			if ( !bHandled  &&  button == 1  &&  ( modifiers & ( Modifier.ALT | Modifier.ALT_GRAPH | Modifier.CTRL | Modifier.SHIFT ) )  ==  0 )
			{
				DPContentLeafEditable leaf = (DPContentLeafEditable)getLeafClosestToLocalPoint( windowPos, new DPContentLeafEditable.EditableLeafElementFilter() );
				if ( leaf != null )
				{
					Xform2 x = leaf.getLocalToRootXform();
					x = x.inverse();
					
					Marker marker = leaf.markerAtPoint( x.transform( windowPos ) );
					caret.moveTo( marker );
					selectionManager.mouseSelectionBegin( marker );
				}
			}

			emitImmediateEvents();
		}
		
		protected void mouseUpEvent(int button, Point2 windowPos, int buttonModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );

			
			rootSpaceMouse.buttonUp( windowPos, button );

			if ( button == 1 )
			{
				selectionManager.mouseSelectionReset();
			}
			
			emitImmediateEvents();
		}
		
		
		
		protected void mouseMotionEvent(Point2 windowPos, int buttonModifiers, MouseEvent mouseEvent)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			
			rootSpaceMouse.motion( windowPos, mouseEvent );

			emitImmediateEvents();
		}

		protected void mouseDragEvent(Point2 windowPos, int buttonModifiers, MouseEvent mouseEvent)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			
			if ( selectionManager.isMouseDragInProgress() )
			{
				DPContentLeafEditable leaf = (DPContentLeafEditable)getLeafClosestToLocalPoint( windowPos, new DPContentLeafEditable.EditableLeafElementFilter() );
				Xform2 x = leaf.getLocalToRootXform();
				x = x.inverse();

				Marker marker = leaf.markerAtPoint( x.transform( windowPos ) );
				
				caret.moveTo( marker );
				
				selectionManager.mouseSelectionDrag( marker );
			}
			
			rootSpaceMouse.drag( windowPos, mouseEvent );

			emitImmediateEvents();
		}



		
		protected void mouseEnterEvent(Point2 windowPos, int buttonModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			
			rootSpaceMouse.enter( windowPos );
			
			emitImmediateEvents();
		}

		protected void mouseLeaveEvent(Point2 windowPos, int buttonModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			
			rootSpaceMouse.leave( windowPos );
			
			emitImmediateEvents();
		}


		protected void mouseDown2Event(int button, Point2 windowPos, int buttonModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			rootSpaceMouse.buttonDown2( windowPos, button );
		}


		protected void mouseDown3Event(int button, Point2 windowPos, int buttonModifiers)
		{
			rootSpaceMouse.setLocalPos( windowPos );
			rootSpaceMouse.setButtonModifiers( buttonModifiers );
			rootSpaceMouse.buttonDown3( windowPos, button );
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
			
			if ( handleNavigationKeyPress( event ) )
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
						DPContentLeafEditable leaf = caret.getElement();
						if ( leaf.onKeyPress( event ) )
						{
							return true;
						}
						
						if ( leaf.isEditable() )
						{
							leaf.onContentKeyPress( caret, event );
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
		
		protected boolean keyReleaseEvent(KeyEvent event, int keyModifiers)
		{
			rootSpaceMouse.setKeyModifiers( keyModifiers );
			
			if ( isNavigationKey( event ) )
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
						DPContentLeafEditable leaf = caret.getElement();
						if ( leaf.onKeyRelease( event ) )
						{
							return true;
						}
						
						if ( leaf.isEditable() )
						{
							leaf.onContentKeyRelease( caret, event );
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
		
		
		
		protected boolean keyTypedEvent(KeyEvent event, int keyModifiers)
		{
			rootSpaceMouse.setKeyModifiers( keyModifiers );
			int modifiers = rootSpaceMouse.getModifiers();
			
			boolean bCtrl = ( modifiers & Modifier.KEYS_MASK )  ==  Modifier.CTRL;
			boolean bAlt = ( modifiers & Modifier.KEYS_MASK )  ==  Modifier.ALT;

			if ( isNavigationKey( event ) )
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
					if ( caret.isValid()  &&  !bCtrl  &&  !bAlt )
					{
						DPContentLeafEditable leaf = caret.getElement();
						if ( leaf.onKeyTyped( event ) )
						{
							return true;
						}
						
						if ( leaf.isEditable() )
						{
							leaf.onContentKeyTyped( caret, event );
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
			return keyCode == KeyEvent.VK_LEFT  ||  keyCode == KeyEvent.VK_RIGHT  ||  keyCode == KeyEvent.VK_UP  ||  keyCode == KeyEvent.VK_DOWN  ||
						keyCode == KeyEvent.VK_HOME  ||  keyCode == KeyEvent.VK_END;
		}
		
		protected boolean isModifierKey(KeyEvent event)
		{
			int keyCode = event.getKeyCode();
			return keyCode == KeyEvent.VK_CONTROL  ||  keyCode == KeyEvent.VK_SHIFT  ||  keyCode == KeyEvent.VK_ALT  ||  keyCode == KeyEvent.VK_ALT_GRAPH;
		}
		
		protected boolean handleNavigationKeyPress(KeyEvent event)
		{
			if ( isNavigationKey( event ) )
			{
				int modifiers = rootSpaceMouse.getModifiers();
				if ( caret.isValid() )
				{
					Marker prevPos = caret.getMarker().copy();
					if ( event.getKeyCode() == KeyEvent.VK_LEFT )
					{
						caret.moveLeft();
					}
					else if ( event.getKeyCode() == KeyEvent.VK_RIGHT )
					{
						caret.moveRight();
					}
					else if ( event.getKeyCode() == KeyEvent.VK_UP )
					{
						caret.moveUp();
					}
					else if ( event.getKeyCode() == KeyEvent.VK_DOWN )
					{
						caret.moveDown();
					}
					else if ( event.getKeyCode() == KeyEvent.VK_HOME )
					{
						caret.moveToHome();
					}
					else if ( event.getKeyCode() == KeyEvent.VK_END )
					{
						caret.moveToEnd();
					}
					
					if ( !caret.getMarker().equals( prevPos ) )
					{
						selectionManager.onCaretMove( caret, prevPos, ( modifiers & Modifier.SHIFT ) != 0 );
					}
					
					caret.ensureVisible();
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
		
		
		public void pointerDndInitiateDrag(Pointer pointer, DndDropLocal drop, MouseEvent mouseEvent, int requestedAction)
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
			Point2 targetPos[] = new Point2[] { null };
			PointerInputElement targetElement = getDndElement( rootPos, targetPos );
			if ( targetElement != null )
			{
				DndDropSwing drop = new DndDropSwing( targetElement, targetPos[0], transfer );
				return targetElement.getDndHandler().canDrop( targetElement, drop );
			}
			else
			{
				return false;
			}
		}
		
		private boolean swingDndImportData(TransferHandler.TransferSupport transfer)
		{
			Point windowPos = transfer.getDropLocation().getDropPoint();
			Point2 rootPos = new Point2( windowPos.x, windowPos.y );
			Point2 targetPos[] = new Point2[] { null };
			PointerInputElement targetElement = getDndElement( rootPos, targetPos );
			if ( targetElement != null )
			{
				DndDropSwing drop = new DndDropSwing( targetElement, targetPos[0], transfer );
				return targetElement.getDndHandler().acceptDrop( targetElement, drop );
			}
			else
			{
				return false;
			}
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
			
			
			/*if ( caretLeaf != null )
			{
				caretLeaf.ensureVisible();
			}
			
			queueEnsureCaretVisible();*/
			
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
		
		protected void deleteSelection()
		{
			DPRegion selectionRegion = getSelectionRegion();
			if ( selectionRegion != null  &&  !selection.isEmpty() )
			{
				EditHandler editHandler = selectionRegion.getEditHandler();
				if ( editHandler != null )
				{
					if ( caret.getMarker().equals( selection.getEndMarker() ) )
					{
						caret.moveTo( selection.getStartMarker() );
					}
					editHandler.deleteSelection( selection );
				}
			}
		}

		protected void replaceSelection(String replacement)
		{
			DPRegion selectionRegion = getSelectionRegion();
			if ( selectionRegion != null )
			{
				EditHandler editHandler = selectionRegion.getEditHandler();
				if ( editHandler != null )
				{
					editHandler.replaceSelection( selection, replacement );
				}
			}
		}
		
		
		
		
		//
		//
		// FRAME METHODS
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
			if ( !selection.isEmpty() )
			{
				return selection.getRegion();
			}
			else
			{
				return null;
			}
		}
	}

	
	
	
	
	
	public RootElement rootElement;
	private boolean bShown, bConfigured;
	
	
	public PresentationComponent()
	{
		super();
		
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
		rootElement.mouseDownEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
	}

	public void mouseReleased(MouseEvent e)
	{
		rootElement.mouseUpEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
	}

	
	public void mouseClicked(MouseEvent e)
	{
		switch ( e.getClickCount() )
		{
		case 2:
			rootElement.mouseDown2Event( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
			break;
		case 3:
			rootElement.mouseDown3Event( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
			break;
		default:
			break;
		}
	}

	
	public void mouseMoved(MouseEvent e)
	{
		rootElement.mouseMotionEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), e );
	}

	public void mouseDragged(MouseEvent e)
	{
		rootElement.mouseDragEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), e );
	}

	public void mouseEntered(MouseEvent e)
	{
		rootElement.mouseEnterEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
	}

	public void mouseExited(MouseEvent e)
	{
		rootElement.mouseLeaveEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
	}


	


	public void mouseWheelMoved(MouseWheelEvent e)
	{
		rootElement.mouseWheelEvent( new Point2( (double)e.getX(), (double)e.getY() ), e.getWheelRotation(), e.getUnitsToScroll(), getButtonModifiers( e ) );
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
}
