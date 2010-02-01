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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Caret.CaretListener;
import BritefuryJ.DocPresent.Input.DndDropLocal;
import BritefuryJ.DocPresent.Input.DndDropSwing;
import BritefuryJ.DocPresent.Input.InputTable;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Input.Pointer;
import BritefuryJ.DocPresent.Input.PointerDndController;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeRootElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Selection.SelectionListener;
import BritefuryJ.DocPresent.TreeExplorer.ElementTreeExplorer;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;





public class DPPresentationArea extends DPFrame implements CaretListener, SelectionListener, PointerDndController
{
	public static class CannotGetGraphics2DException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static class InvalidMouseButtonException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	public static class ViewTransformation
	{
		private Point2 windowTopLeftCornerInRootSpace;
		private double rootScaleInWindowSpace;
		
		
		public ViewTransformation()
		{
			windowTopLeftCornerInRootSpace = new Point2();
			rootScaleInWindowSpace = 1.0;
		}
	}
	
	
	
	
	static private class PresentationAreaComponent extends JComponent implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener
	{
		private static final long serialVersionUID = 1L;
		
		
		
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
					DPFrame frame = area.getSelectionFrame();
					if ( frame != null )
					{
						EditHandler editHandler = frame.getEditHandler();
						if ( editHandler != null )
						{	
							return editHandler.getSourceActions();
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
					DPFrame frame = area.getSelectionFrame();
					if ( frame != null )
					{
						EditHandler editHandler = frame.getEditHandler();
						if ( editHandler != null )
						{
							return editHandler.createTransferable();
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
					DPFrame frame = area.getSelectionFrame();
					if ( frame != null )
					{
						EditHandler editHandler = frame.getEditHandler();
						if ( editHandler != null )
						{
							editHandler.exportDone( data, action );
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
					DPFrame frame = area.getCaretFrame();
					if ( frame != null )
					{
						EditHandler editHandler = frame.getEditHandler();
						if ( editHandler != null )
						{
							return editHandler.canImport( transfer );
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
					DPFrame frame = area.getCaretFrame();
					if ( frame != null )
					{
						EditHandler editHandler = frame.getEditHandler();
						if ( editHandler != null )
						{
							return editHandler.importData( transfer );
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
			setFocusTraversalKeysEnabled( false );
			
			setTransferHandler( new PresAreaTransferHandler() );
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
			area.mouseDownEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
		}

		public void mouseReleased(MouseEvent e)
		{
			area.mouseUpEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
		}

		
		public void mouseClicked(MouseEvent e)
		{
			switch ( e.getClickCount() )
			{
			case 2:
				area.mouseDown2Event( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
				break;
			case 3:
				area.mouseDown3Event( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
				break;
			default:
				break;
			}
		}

		
		public void mouseMoved(MouseEvent e)
		{
			area.mouseMotionEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), e );
		}

		public void mouseDragged(MouseEvent e)
		{
			area.mouseDragEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), e );
		}

		public void mouseEntered(MouseEvent e)
		{
			area.mouseEnterEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
		}

		public void mouseExited(MouseEvent e)
		{
			area.mouseLeaveEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
		}


		


		public void mouseWheelMoved(MouseWheelEvent e)
		{
			area.mouseWheelEvent( new Point2( (double)e.getX(), (double)e.getY() ), e.getWheelRotation(), e.getUnitsToScroll(), getButtonModifiers( e ) );
		}
		
		
		

		private boolean swingDndCanImport(TransferHandler.TransferSupport transfer)
		{
			return area.swingDndCanImport( transfer );
		}
		
		private boolean swingDndImportData(TransferHandler.TransferSupport transfer)
		{
			return area.swingDndImportData( transfer );
		}
		

		
		public void keyPressed(KeyEvent e)
		{
			area.keyPressEvent( e, getKeyModifiers( e ) );
		}


		public void keyReleased(KeyEvent e)
		{
			area.keyReleaseEvent( e, getKeyModifiers( e ) );
		}


		public void keyTyped(KeyEvent e)
		{
			area.keyTypedEvent( e, getKeyModifiers( e ) );
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
	
	
	
	public static class ScrollablePresentationAreaComponent extends JPanel implements ChangeListener
	{
		private static final long serialVersionUID = 1L;

		private DPPresentationArea area;
		private PresentationAreaComponent presentationComponent;
		private JScrollBar horizScroll, vertScroll;
		private boolean bIgnoreChanges;
		
		public ScrollablePresentationAreaComponent(DPPresentationArea area)
		{
			this.area = area;
			
			presentationComponent = new PresentationAreaComponent( area );
			
			horizScroll = new JScrollBar( JScrollBar.HORIZONTAL );
			vertScroll = new JScrollBar( JScrollBar.VERTICAL );
			
			GridBagLayout grid = new GridBagLayout();
			setLayout( grid );
			
			GridBagConstraints c;
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = c.weighty = 1.0;
			grid.setConstraints( presentationComponent, c );
			add( presentationComponent );
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.VERTICAL;
			c.gridwidth = GridBagConstraints.REMAINDER;
			grid.setConstraints( vertScroll, c );
			add( vertScroll );

			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			grid.setConstraints( horizScroll, c );
			add( horizScroll );
			
			horizScroll.getModel().addChangeListener( this );
			vertScroll.getModel().addChangeListener( this );
			
			bIgnoreChanges = false;
		}
		
		
		public JComponent getPresentationComponent()
		{
			return presentationComponent;
		}
		
		
		
		private void setRange(Vector2 rootSize, Vector2 extents, Point2 value)
		{
			bIgnoreChanges = true;
			BoundedRangeModel x = horizScroll.getModel();
			BoundedRangeModel y = vertScroll.getModel();
			
			double maxX = Math.max( rootSize.x, extents.x );
			double maxY = Math.max( rootSize.y, extents.y );
			double valueX = Math.max( Math.min( value.x, maxX - extents.x ), 0.0 );
			double valueY = Math.max( Math.min( value.y, maxY - extents.y ), 0.0 );
			
			x.setRangeProperties( (int)valueX, (int)extents.x, 0, (int)maxX, false );
			y.setRangeProperties( (int)valueY, (int)extents.y, 0, (int)maxY, false );
			
			horizScroll.setBlockIncrement( (int)extents.x );
			vertScroll.setBlockIncrement( (int)extents.y );
			bIgnoreChanges = false;
		}



		public void stateChanged(ChangeEvent event)
		{
			if ( event.getSource() == horizScroll.getModel() )
			{
				if ( !bIgnoreChanges )
				{
					BoundedRangeModel x = horizScroll.getModel();
					area.scrollBarX( (double)x.getValue() );
				}
			}
			else if ( event.getSource() == vertScroll.getModel() )
			{
				if ( !bIgnoreChanges )
				{
					BoundedRangeModel y = vertScroll.getModel();
					area.scrollBarY( (double)y.getValue() );
				}
			}
			else
			{
				throw new RuntimeException( "Invalid event source" );
			}
		}
	}
	
	
	
	private ViewTransformation viewXform;
	private Point2 dragStartPosInWindowSpace;
	private int dragButton;
	
	private boolean bMouseSelectionInProgress;
	
	private Vector2 windowSize;
	
	private Pointer rootSpaceMouse;
	private InputTable inputTable;
	
	private WeakHashMap<StateKeyListener, Object> stateKeyListeners;
	
	private ScrollablePresentationAreaComponent component;
	
	private Runnable immediateEventDispatcher;
	
	private boolean bAllocationRequired;
	
	private Caret caret;
	private DPContentLeaf currentCaretLeaf;
	private Selection selection;
	
	
	private boolean bHorizontalClamp;
	
	private boolean bStructureRefreshQueued;
	
	
	protected ArrayList<Runnable> waitingImmediateEvents;			// only initialised when non-empty; otherwise null

	
	
	protected DPPresentationArea metaArea;
	protected ElementTreeExplorer explorer;
	
	
	protected PageController pageController;

	
	
	
	public DPPresentationArea(ElementContext context)
	{
		this( context, new ViewTransformation() );
	}
	
	public DPPresentationArea(ElementContext context, ViewTransformation viewXform)
	{
		super( context );
		
		layoutNode = new LayoutNodeRootElement( this );
		
		presentationArea = this;
		
		this.viewXform = viewXform;
		dragStartPosInWindowSpace = new Point2();
		dragButton = 0;
		
		windowSize = new Vector2();
		
		component = new ScrollablePresentationAreaComponent( this );

		inputTable = new InputTable( this, this );
		rootSpaceMouse = inputTable.getMouse();
		
		stateKeyListeners = new WeakHashMap<StateKeyListener, Object>();
		
		bAllocationRequired = true;
		
		bHorizontalClamp = true;
		
		caret = new Caret();
		caret.setCaretListener( this );
		
		currentCaretLeaf = null;

		selection = new Selection();
		
		bStructureRefreshQueued = false;
		
		bMouseSelectionInProgress = false;
	}
	
	
	
	public void setPageController(PageController pageController)
	{
		this.pageController = pageController;
	}
	
	public PageController getPageController()
	{
		return pageController;
	}
	
	
	
	
	public void disableHorizontalClamping()
	{
		bHorizontalClamp = false;
	}
	
	public void enableHorizontalClamping()
	{
		bHorizontalClamp = true;
	}
	
	
	public JComponent getComponent()
	{
		return component;
	}

	public JComponent getPresentationComponent()
	{
		return component.getPresentationComponent();
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
			ArrayList<DPWidget> startPath = s.getStartPathFromCommonRoot();
			ArrayList<DPWidget> endPath = s.getEndPathFromCommonRoot();
			
			if ( commonRoot != null )
			{
				StringBuilder builder = new StringBuilder();

				commonRoot.getTextRepresentationBetweenPaths( builder, s.getStartMarker(), startPath, 0, s.getEndMarker(), endPath, 0 );
			
				return builder.toString();
			}
			else
			{
				return ((DPContentLeaf)startPath.get( 0 )).getTextRepresentationBetweenMarkers( s.getStartMarker(), s.getEndMarker() );
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
			ArrayList<DPWidget> startPath = s.getStartPathFromCommonRoot();
			ArrayList<DPWidget> endPath = s.getEndPathFromCommonRoot();
			
			if ( commonRoot != null )
			{
				ItemStreamBuilder builder = new ItemStreamBuilder();

				commonRoot.getLinearRepresentationBetweenPaths( builder, s.getStartMarker(), startPath, 0, s.getEndMarker(), endPath, 0 );
			
				return builder.stream();
			}
			else
			{
				return ((DPContentLeaf)startPath.get( 0 )).getLinearRepresentationBetweenMarkers( s.getStartMarker(), s.getEndMarker() );
			}
		}
	}

	
	
	
	//
	// Space conversion and navigation methods
	//
	
	public Point2 windowSpaceToRootSpace(Point2 w)
	{
		return w.scale( 1.0 / viewXform.rootScaleInWindowSpace ).add( viewXform.windowTopLeftCornerInRootSpace.toVector2() );
	}
	
	public Vector2 windowSpaceToRootSpace(Vector2 w)
	{
		return w.mul( 1.0 / viewXform.rootScaleInWindowSpace );
	}
	
	public Point2 rootSpaceToWindowSpace(Point2 r)
	{
		return r.sub( viewXform.windowTopLeftCornerInRootSpace.toVector2() ).scale( viewXform.rootScaleInWindowSpace );
	}
	
	public Vector2 rootSpaceToWindowSpace(Vector2 r)
	{
		return r.mul( viewXform.rootScaleInWindowSpace );
	}
	
	
	public double windowSpaceSizeToRootSpace(double w)
	{
		return w / viewXform.rootScaleInWindowSpace;
	}
	
	public double rootSpaceSizeToWindowSpace(double r)
	{
		return r * viewXform.rootScaleInWindowSpace;
	}
	
	
	
	
	public ViewTransformation getViewTransformation()
	{
		return viewXform;
	}
	
	public void setViewTransformation(ViewTransformation viewXform)
	{
		this.viewXform = viewXform;
		updateRange();
		bAllocationRequired = true;
		queueFullRedraw();
	}
	
	public void oneToOne()
	{
		viewXform.rootScaleInWindowSpace = 1.0;
		updateRange();
		
		bAllocationRequired = true;
		queueFullRedraw();
	}
	
	public void reset()
	{
		viewXform.windowTopLeftCornerInRootSpace = new Point2();
		viewXform.rootScaleInWindowSpace = 1.0;
		updateRange();
		
		bAllocationRequired = true;
		queueFullRedraw();
	}
	
	public void focusOn(DPWidget widget)
	{
		viewXform.rootScaleInWindowSpace = 1.0;
		Point2 topLeft = widget.getLocalPointRelativeToRoot( new Point2( 0.0, 0.0 ) );
		Point2 bottomRight = widget.getLocalPointRelativeToRoot( new Point2( widget.getAllocation() ) );
		Point2 centre = Point2.average( topLeft, bottomRight );
		viewXform.windowTopLeftCornerInRootSpace = centre.sub( windowSize.mul( 0.5 ) );
		updateRange();
		queueFullRedraw();
	}
	
	public void zoomToFit()
	{
		performAllocation();
		
		double allocationX = getAllocationX();
		double allocationY = getAllocationY();

		double ax = allocationX == 0.0  ?  1.0  :  allocationX;
		double ay = allocationY == 0.0  ?  1.0  :  allocationY;
		
		viewXform.windowTopLeftCornerInRootSpace = new Point2();
		viewXform.rootScaleInWindowSpace = Math.min( windowSize.x / ax, windowSize.y / ay );
		viewXform.rootScaleInWindowSpace = viewXform.rootScaleInWindowSpace == 0.0  ?  1.0  :  viewXform.rootScaleInWindowSpace;
		
		updateRange();
		bAllocationRequired = true;
		queueFullRedraw();
	}
	
	public void zoom(double zoomFactor, Point2 centreInWindowSpace)
	{
		Point2 centreInRootSpace = windowSpaceToRootSpace( centreInWindowSpace );
		viewXform.rootScaleInWindowSpace *= zoomFactor;
		Point2 newCentreInRootSpace = windowSpaceToRootSpace( centreInWindowSpace );
		viewXform.windowTopLeftCornerInRootSpace = viewXform.windowTopLeftCornerInRootSpace.sub( newCentreInRootSpace.sub( centreInRootSpace ) );

		updateRange();
		bAllocationRequired = true;
		queueFullRedraw();
	}
	
	public void zoomAboutCentre(double zoomFactor)
	{
		// We want to zoom about the centre of the document, not the top left corner
		zoom( zoomFactor, new Point2( windowSize.mul( 0.5 ) ) );
	}
	
	public void panRootSpace(Vector2 pan)
	{
		viewXform.windowTopLeftCornerInRootSpace = viewXform.windowTopLeftCornerInRootSpace.add( pan );
		updateRange();
		queueFullRedraw();
	}
	
	public void scrollRootSpace(Vector2 pan)
	{

		if ( pan.x < 0.0 )
		{
			viewXform.windowTopLeftCornerInRootSpace.x = Math.max( viewXform.windowTopLeftCornerInRootSpace.x + pan.x,  Math.min( viewXform.windowTopLeftCornerInRootSpace.x, 0.0 ) );
		}
		else if ( pan.x > 0.0 )
		{
			double windowWidthInRootSpace = windowSize.x / viewXform.rootScaleInWindowSpace;
			double allocationX = getAllocationX();
			double ax = allocationX == 0.0  ?  1.0  :  allocationX;
			double maxX = Math.max( ax - windowWidthInRootSpace, 0.0 );
			viewXform.windowTopLeftCornerInRootSpace.x = Math.min( viewXform.windowTopLeftCornerInRootSpace.x + pan.x,  Math.max( viewXform.windowTopLeftCornerInRootSpace.x, maxX ) );
		}
		if ( pan.y < 0.0 )
		{
			viewXform.windowTopLeftCornerInRootSpace.y = Math.max( viewXform.windowTopLeftCornerInRootSpace.y + pan.y,  Math.min( viewXform.windowTopLeftCornerInRootSpace.y, 0.0 ) );
		}
		else if ( pan.y > 0.0 )
		{
			double windowHeightInRootSpace = windowSize.y / viewXform.rootScaleInWindowSpace;
			double allocationY = getAllocationY();
			double ay = allocationY == 0.0  ?  1.0  :  allocationY;
			double maxY = Math.max( ay - windowHeightInRootSpace, 0.0 );
			viewXform.windowTopLeftCornerInRootSpace.y = Math.min( viewXform.windowTopLeftCornerInRootSpace.y + pan.y,  Math.max( viewXform.windowTopLeftCornerInRootSpace.y, maxY ) );
		}
		updateRange();
		queueFullRedraw();
	}
	
	public void panWindowSpace(Vector2 pan)
	{
		panRootSpace( windowSpaceToRootSpace( pan ) );
	}
	
	public void scrollWindowSpace(Vector2 pan)
	{
		
		scrollRootSpace( windowSpaceToRootSpace( pan ) );
	}
	
	private void updateRange()
	{
		double allocationX = getAllocationX();
		double allocationY = getAllocationY();

		double ax = allocationX == 0.0  ?  1.0  :  allocationX;
		double ay = allocationY == 0.0  ?  1.0  :  allocationY;

		component.setRange( new Vector2( ax * viewXform.rootScaleInWindowSpace, ay * viewXform.rootScaleInWindowSpace ),
				windowSize,
				viewXform.windowTopLeftCornerInRootSpace.scale( viewXform.rootScaleInWindowSpace ) );
		
	}
	
	private void scrollBarX(double x)
	{
		viewXform.windowTopLeftCornerInRootSpace.x = x / viewXform.rootScaleInWindowSpace;
		queueFullRedraw();
	}
	
	private void scrollBarY(double y)
	{
		viewXform.windowTopLeftCornerInRootSpace.y = y / viewXform.rootScaleInWindowSpace;
		queueFullRedraw();
	}
	
	
	
	
	//
	// Immediate event queue methods
	//
	
	public void queueImmediateEvent(Runnable event)
	{
		if ( waitingImmediateEvents == null  &&  immediateEventDispatcher == null )
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
	
	protected void queueFullRedraw()
	{
		component.presentationComponent.repaint( component.presentationComponent.getVisibleRect() );
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
		component.presentationComponent.repaint( new Rectangle( x, y, w, h ) ); 
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
			if ( bHorizontalClamp )
			{
				rootLayout.allocateX( reqX, 0.0, Math.max( reqX.getMinWidth(), windowSize.x ) );
			}
			else
			{
				rootLayout.allocateX( reqX, 0.0, Math.max( reqX.getPrefWidth(), windowSize.x ) );
			}
			rootLayout.refreshAllocationX( prevWidth );
			
			// Get Y requisition
			LReqBoxInterface reqY = rootLayout.refreshRequisitionY();
			
			// Allocate Y
			LAllocV prevAllocV = rootLayout.getAllocV();
			rootLayout.allocateY( reqY, 0.0, reqY.getReqHeight() );
			rootLayout.refreshAllocationY( prevAllocV );
			
			updateRange();
			bAllocationRequired = false;
			
			// Send motion events; pointer hasn't moved, but the widgets have
			inputTable.onRootElementReallocate();
			long t2 = System.nanoTime();
			System.out.println( "DPPresentationArea.performAllocation(): TYPESET TIME = " + (double)(t2-t1) * 1.0e-9  +  ", used memory = "  + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) );
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
							DPContentLeaf leaf = getLayoutNode().getLeftContentLeaf();
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
			updateRange();
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
		graphics.scale( viewXform.rootScaleInWindowSpace, viewXform.rootScaleInWindowSpace );
		graphics.translate( -viewXform.windowTopLeftCornerInRootSpace.x, -viewXform.windowTopLeftCornerInRootSpace.y );
		
		// Draw
		handleDrawBackground( graphics, new AABox2( topLeftRootSpace, bottomRightRootSpace) );
		drawSelection( graphics );
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
				Color prevColour = graphics.getColor();
				graphics.setColor( Color.blue );
				widget.drawCaret( graphics, caret );
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
			List<DPWidget> startPath = selection.getStartPathFromCommonRoot();
			List<DPWidget> endPath = selection.getEndPathFromCommonRoot();

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
		bMouseSelectionInProgress = false;
		component.presentationComponent.grabFocus();
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		int modifiers = rootSpaceMouse.getModifiers();
		if ( button == 1  &&  ( modifiers & ( Modifier.ALT | Modifier.ALT_GRAPH | Modifier.CTRL | Modifier.SHIFT ) )  ==  0 )
		{
			DPContentLeafEditableEntry leaf = (DPContentLeafEditableEntry)getLeafClosestToLocalPoint( rootPos, new DPContentLeafEditableEntry.EditableEntryLeafElementFilter() );
			if ( leaf != null )
			{
				Xform2 x = leaf.getLocalToRootXform();
				x = x.inverse();
				
				if ( caret.isValid() )
				{
					Marker prevPos = caret.getMarker().copy();
					leaf.moveMarkerToPoint( caret.getMarker(), x.transform( rootPos ) );
				
					onCaretMove( prevPos, true );
					bMouseSelectionInProgress = true;
				}
				else
				{
					leaf.moveMarkerToPoint( caret.getMarker(), x.transform( rootPos ) );
				}
			}
		}

		if ( ( modifiers & Modifier.ALT )  ==  0 )
		{
			rootSpaceMouse.buttonDown( rootPos, button );
		}
		else
		{
			System.out.println( "Initiating drag" );
			dragButton = button;
			dragStartPosInWindowSpace = windowPos;
		}
		
		emitImmediateEvents();
	}
	
	protected void mouseUpEvent(int button, Point2 windowPos, int buttonModifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		int modifiers = rootSpaceMouse.getModifiers();
		if ( ( modifiers & Modifier.ALT )  ==  0 )
		{
			rootSpaceMouse.buttonUp( rootPos, button );
		}
		
		if ( dragButton != 0 )
		{
			dragButton = 0;
			dragStartPosInWindowSpace = windowPos;
		}
		
		if ( button == 1 )
		{
			bMouseSelectionInProgress = false;
		}
		
		emitImmediateEvents();
	}
	
	
	
	protected void mouseMotionEvent(Point2 windowPos, int buttonModifiers, MouseEvent mouseEvent)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		
		rootSpaceMouse.motion( rootPos, mouseEvent );

		emitImmediateEvents();
	}

	protected void mouseDragEvent(Point2 windowPos, int buttonModifiers, MouseEvent mouseEvent)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		
		if ( bMouseSelectionInProgress )
		{
			DPContentLeafEditableEntry leaf = (DPContentLeafEditableEntry)getLeafClosestToLocalPoint( rootPos, new DPContentLeafEditableEntry.EditableEntryLeafElementFilter() );
			Xform2 x = leaf.getLocalToRootXform();
			x = x.inverse();

			Marker prevPos = caret.getMarker().copy();
			leaf.moveMarkerToPoint( caret.getMarker(), x.transform( rootPos ) );
			
			onCaretMove( prevPos, false );
		}
		
		if ( dragButton == 0 )
		{
			rootSpaceMouse.drag( rootPos, mouseEvent );
		}
		else
		{
			Vector2 delta = windowPos.sub( dragStartPosInWindowSpace );
			dragStartPosInWindowSpace = windowPos;
			
			if ( dragButton == 1  ||  dragButton == 2 )
			{
				viewXform.windowTopLeftCornerInRootSpace = viewXform.windowTopLeftCornerInRootSpace.sub( windowSpaceToRootSpace( delta ) );
				updateRange();
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



	
	protected void mouseEnterEvent(Point2 windowPos, int buttonModifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		
		if ( dragButton == 0 )
		{
			rootSpaceMouse.enter( rootPos );
		}
		
		emitImmediateEvents();
	}

	protected void mouseLeaveEvent(Point2 windowPos, int buttonModifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		
		if ( dragButton == 0 )
		{
			rootSpaceMouse.leave( rootPos );
		}
		
		emitImmediateEvents();
	}


	protected void mouseDown2Event(int button, Point2 windowPos, int buttonModifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		rootSpaceMouse.buttonDown2( rootPos, button );
	}


	protected void mouseDown3Event(int button, Point2 windowPos, int buttonModifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		rootSpaceMouse.buttonDown3( rootPos, button );
	}
	
	
	protected void mouseWheelEvent(Point2 windowPos, int wheelClicks, int unitsToScroll, int buttonModifiers)
	{
		Point2 rootPos = windowSpaceToRootSpace( windowPos );
		rootSpaceMouse.setLocalPos( rootPos );
		rootSpaceMouse.setButtonModifiers( buttonModifiers );
		int modifiers = rootSpaceMouse.getModifiers();
		if ( ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.ALT )
		{
			double delta = (double)-wheelClicks;
			double scaleDelta = Math.pow( 2.0,  ( delta / 1.5 ) );
			
			zoom( scaleDelta, windowPos );
		}
		else if ( ( modifiers & Modifier._KEYS_MASK )  !=  0 )
		{
			rootSpaceMouse.scroll( 0, -wheelClicks );
			emitImmediateEvents();
		}
		else
		{
			double delta = (double)wheelClicks;
			scrollWindowSpace( new Vector2( 0.0, delta * 75.0 ) );
		}
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
					DPContentLeaf leaf = caret.getMarker().getElement();
					if ( leaf.isEditableEntry() )
					{
						DPContentLeafEditableEntry editable = (DPContentLeafEditableEntry)leaf;
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
					DPContentLeaf leaf = caret.getMarker().getElement();
					if ( leaf.isEditableEntry() )
					{
						DPContentLeafEditableEntry editable = (DPContentLeafEditableEntry)leaf;
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
	
	
	
	protected boolean keyTypedEvent(KeyEvent event, int keyModifiers)
	{
		rootSpaceMouse.setKeyModifiers( keyModifiers );
		int modifiers = rootSpaceMouse.getModifiers();
		
		boolean bCtrl = ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.CTRL;
		boolean bAlt = ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.ALT;

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
					DPContentLeaf leaf = caret.getMarker().getElement();
					if ( leaf.isEditableEntry() )
					{
						DPContentLeafEditableEntry editable = (DPContentLeafEditableEntry)leaf;
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
				DPContentLeaf leaf = caret.getMarker().getElement();
				Marker prevPos = caret.getMarker().copy();
				if ( event.getKeyCode() == KeyEvent.VK_LEFT )
				{
					leaf.moveMarkerLeft( caret.getMarker(), true );
				}
				else if ( event.getKeyCode() == KeyEvent.VK_RIGHT )
				{
					leaf.moveMarkerRight( caret.getMarker(), true );
				}
				else if ( event.getKeyCode() == KeyEvent.VK_UP )
				{
					leaf.moveMarkerUp( caret.getMarker(), true );
				}
				else if ( event.getKeyCode() == KeyEvent.VK_DOWN )
				{
					leaf.moveMarkerDown( caret.getMarker(), true );
				}
				else if ( event.getKeyCode() == KeyEvent.VK_HOME )
				{
					leaf.moveMarkerHome( caret.getMarker() );
				}
				else if ( event.getKeyCode() == KeyEvent.VK_END )
				{
					leaf.moveMarkerEnd( caret.getMarker() );
				}
				
				if ( !caret.getMarker().equals( prevPos ) )
				{
					onCaretMove( prevPos, ( modifiers & Modifier.SHIFT ) == 0 );
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
	


	//
	//
	// DRAG AND DROP PROTOCOL
	//
	// 1. The user presses a mouse button:
	//	onButtonDown is sent.
	//	onDndButtonDown is also sent. If a widget with DnD enabled can be found, it creates and returns a DndDrop structure that will be used to track information
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
	
	protected PresentationAreaComponent.PresAreaTransferHandler getDndTransferHandler()
	{
		return (PresentationAreaComponent.PresAreaTransferHandler)component.getPresentationComponent().getTransferHandler();
	}
	
	
	public void pointerDndInitiateDrag(Pointer pointer, DndDropLocal drop, MouseEvent mouseEvent, int requestedAction)
	{
		PresentationAreaComponent.PresAreaTransferHandler xferHandler = getDndTransferHandler();
		xferHandler.beginExportDnd( drop );
		xferHandler.exportAsDrag( component.getPresentationComponent(), mouseEvent, requestedAction );
		xferHandler.endExportDnd();
	}

	
	private boolean swingDndCanImport(TransferHandler.TransferSupport transfer)
	{
		Point windowPos = transfer.getDropLocation().getDropPoint();
		Point2 rootPos = windowSpaceToRootSpace( new Point2( windowPos.x, windowPos.y ) );
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
		Point2 rootPos = windowSpaceToRootSpace( new Point2( windowPos.x, windowPos.y ) );
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
	
	
	
	protected void setCursorDrag(PointerInterface pointer)
	{
		component.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
	}
	
	protected void setCursorHand(PointerInterface pointer)
	{
		component.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
	}
	
	protected void setCursorArrow(PointerInterface pointer)
	{
		component.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
	}
	
	
	public InputTable getInputTable()
	{
		return inputTable;
	}
	
	public void elementUnrealised(DPWidget element)
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
	
	private void onCaretMove(Marker prevPos, boolean bClearSelection)
	{
		if ( bClearSelection )
		{
			selection.getMarker0().moveTo( caret.getMarker() );
			selection.getMarker1().moveTo( caret.getMarker() );
		}
		else
		{
			selection.getMarker1().moveTo( caret.getMarker() );
		}
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
		DPFrame selectionFrame = getSelectionFrame();
		if ( selectionFrame != null  &&  !selection.isEmpty() )
		{
			EditHandler editHandler = selectionFrame.getEditHandler();
			if ( editHandler != null )
			{
				if ( caret.getMarker().equals( selection.getEndMarker() ) )
				{
					caret.getMarker().moveTo( selection.getStartMarker() );
				}
				editHandler.deleteSelection();
			}
		}
	}

	protected void replaceSelection(String replacement)
	{
		DPFrame selectionFrame = getSelectionFrame();
		if ( selectionFrame != null )
		{
			EditHandler editHandler = selectionFrame.getEditHandler();
			if ( editHandler != null )
			{
				editHandler.replaceSelection( replacement );
			}
		}
	}
	
	
	
	
	//
	//
	// FRAME METHODS
	//
	//
	
	protected DPFrame getCaretFrame()
	{
		if ( caret.isValid() )
		{
			return caret.getMarker().getElement().getFrame();
		}
		else
		{
			return null;
		}
	}
	
	protected DPFrame getSelectionFrame()
	{
		if ( !selection.isEmpty() )
		{
			return selection.getFrame();
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	//
	//
	// META-TREE METHODS
	//
	//

	public DPPresentationArea initialiseMetaTree()
	{
		if ( metaArea == null )
		{
			metaArea = new DPPresentationArea( null );
			metaArea.disableHorizontalClamping();
			metaArea.setChild( initialiseMetaElement() );
		}
		
		return metaArea;
	}
	
	public void shutdownMetaTree()
	{
		if ( metaArea != null )
		{
			shutdownMetaElement();
			metaArea = null;
		}
	}
	
	
	
	
	public ElementTreeExplorer createTreeExplorer()
	{
		if ( explorer == null  ||  !explorer.isVisible() )
		{
			explorer = new ElementTreeExplorer( this );
		}
		return explorer;
	}
}
