//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;

import BritefuryJ.Browser.PaneManager;
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
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Input.DndDragSwing;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;

public abstract class PresentationComponent extends JComponent implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener, FocusListener, WindowListener
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
	
	
	
	
	
	//
	// Transfer handler for drag and drop AND copy and paste
	class PresAreaTransferHandler extends TransferHandler
	{
		private static final long serialVersionUID = 1L;
		private DndDragSwing drop = null;
		
		
		
		
		@Override
		public int getSourceActions(JComponent component)
		{
			if ( drop != null )
			{
				return drop.getSourceDropActions();
			}
			else
			{
				Selection selection = rootElement.getSelection();
				LSRegion region = selection != null  ?  selection.getRegion()  :  null;
				if ( region != null )
				{
					ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
					if ( clipboardHandler != null )
					{
						try {
							return clipboardHandler.getExportActions( rootElement.getSelection() );
						}
						catch (Throwable t) {
							rootElement.notifyExceptionDuringClipboardOperation(region, clipboardHandler, "getExportActions", t);
							return NONE;
						}
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
				Selection selection = rootElement.getSelection();
				LSRegion region = selection != null  ?  selection.getRegion()  :  null;
				if ( region != null )
				{
					ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
					if ( clipboardHandler != null )
					{
						try {
							return clipboardHandler.createExportTransferable( rootElement.getSelection() );
						}
						catch (Throwable t) {
							rootElement.notifyExceptionDuringClipboardOperation(region, clipboardHandler, "createExportTransferable", t);
							return null;
						}
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
				rootElement.setPotentialDrop( null );
				rootElement.detachElementPreview( data );
				drop.getSourceElement().getDndHandler().exportDone( drop.getSourceElement(), data, action );
				drop = null;
				rootElement.dndDragExportDone();
			}
			else
			{
				Selection selection = rootElement.getSelection();
				LSRegion region = selection != null  ?  selection.getRegion()  :  null;
				if ( region != null )
				{
					ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
					if ( clipboardHandler != null )
					{
						try {
							clipboardHandler.exportDone( rootElement.getSelection(), rootElement.getTarget(), data, action );
						}
						catch (Throwable t) {
							rootElement.notifyExceptionDuringClipboardOperation(region, clipboardHandler, "exportDone", t);
						}
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
					Target target = rootElement.getTarget();
					LSRegion region = target != null  ?  target.getRegion()  :  null;
					if ( region != null )
					{
						ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
						if ( clipboardHandler != null )
						{
							try {
								return clipboardHandler.canImport( rootElement.getTarget(), rootElement.getSelection(), new DataTransfer( transfer ) );
							}
							catch (Throwable t) {
								rootElement.notifyExceptionDuringClipboardOperation(region, clipboardHandler, "canImport", t);
							}
						}
					}
					return false;
				}
			}
			catch (Throwable e)
			{
				rootElement.notifyExceptionDuringEventHandler( "canImport", e );
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
					Target target = rootElement.getTarget();
					LSRegion region = target != null  ?  target.getRegion()  :  null;
					if ( region != null )
					{
						ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
						if ( clipboardHandler != null )
						{
							try {
								return clipboardHandler.importData( rootElement.getTarget(), rootElement.getSelection(), new DataTransfer( transfer ) );
							}
							catch (Throwable t) {
								rootElement.notifyExceptionDuringClipboardOperation(region, clipboardHandler, "importData", t);
							}
						}
					}
					return false;
				}
			}
			catch (Throwable e)
			{
				rootElement.notifyExceptionDuringEventHandler( "importData", e );
				return false;
			}
		}

		public void initiateExportDnd(DndDragSwing drop)
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
	
	


	
	
	
	public LSRootElement rootElement;
	private boolean realised, configured;
	private Window window;
	
	
	
	
	public PresentationComponent()
	{
		super();
		
		rootElement = new LSRootElement( this );
		
		realised = false;
		configured = false;

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




	//
	//
	// ROOT ELEMENT METHODS
	//
	//
	
	public LSRootElement getRootElement()
	{
		return rootElement;
	}
	
	
	
	//
	//
	// PAGE CONTROLLER METHODS
	//
	//
	
	
	public void setPageController(PageController pageController)
	{
		rootElement.setPageController( pageController );
	}
	
	public PageController getPageController()
	{
		return rootElement.getPageController();
	}



	//
	//
	// PANE MANAGEMENT
	//
	//

	public PaneManager getPaneManager()
	{
		return null;
	}
	

	
	
	//
	//
	// PAINTING METHODS
	//
	//
	
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
	
	
	//
	//
	// MOUSE EVENT METHODS
	//
	//

	@Override
	public void mousePressed(MouseEvent e)
	{
		onPopupClosingEvent();
		rootElement.mouseDownEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), Modifier.getKeyModifiersFromEvent( e ) );
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		rootElement.mouseUpEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), Modifier.getKeyModifiersFromEvent( e ) );
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		rootElement.mouseClicked( getButton( e ), e.getClickCount(), new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ) );
	}

	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		rootElement.mouseMotionEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), Modifier.getKeyModifiersFromEvent( e ), e );
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		rootElement.mouseDragEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), Modifier.getKeyModifiersFromEvent( e ), e );
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		rootElement.mouseEnterEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), Modifier.getKeyModifiersFromEvent( e ) );
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		rootElement.mouseLeaveEvent( new Point2( (double)e.getX(), (double)e.getY() ), getButtonModifiers( e ), Modifier.getKeyModifiersFromEvent( e ) );
	}


	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		rootElement.mouseWheelEvent( new Point2( (double)e.getX(), (double)e.getY() ), e.getWheelRotation(), e.getUnitsToScroll(), getButtonModifiers( e ) );
	}
	
	
	
	//
	//
	// KEYBOARD EVENT METHODS
	//
	//
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		rootElement.keyPressEvent( e, Modifier.getKeyModifiersFromEvent( e ) );
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		rootElement.keyReleaseEvent( e, Modifier.getKeyModifiersFromEvent( e ) );
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		rootElement.keyTypedEvent( e, Modifier.getKeyModifiersFromEvent( e ) );
	}
	
	

	

	//
	//
	// LAYOUT METHODS
	//
	//
	
	protected Dimension clampSize(Dimension contentSize)
	{
		Rectangle windowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Dimension d = new Dimension();
		d.setSize( Math.min( contentSize.getWidth(), windowBounds.width ), Math.min( contentSize.getHeight(), windowBounds.height ) );
		return d;
	}
	
	@Override
	public Dimension getMinimumSize()
	{
		if ( isMinimumSizeSet() )
		{
			return super.getMinimumSize();
		}
		else
		{
			return clampSize( rootElement.getMinimumSize() );
		}
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		if ( isPreferredSizeSet() )
		{
			return super.getPreferredSize();
		}
		else
		{
			return clampSize( rootElement.getPreferredSize() );
		}
	}
	
	@Override
	public Dimension getMaximumSize()
	{
		if ( isMaximumSizeSet() )
		{
			return super.getMaximumSize();
		}
		else
		{
			return clampSize( rootElement.getMaximumSize() );
		}
	}





	//
	//
	// ComponentListener: resized, moved, shown, hidden
	//
	//

	@Override
	public void componentResized(ComponentEvent e)
	{
		rootElement.configureEvent( new Vector2( (double)getWidth(), (double)getHeight() ) );
		configured = true;
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
	}

	@Override
	public void componentHidden(ComponentEvent e)
	{
	}



	//
	//
	// HierarchyListener: hierarchyChanged
	//
	//

	@Override
	public void hierarchyChanged(HierarchyEvent e)
	{
		Window w = SwingUtilities.getWindowAncestor( this );

		if ( isHierarchyVisible()  &&  w != null )
		{
			sendRealiseEvents();
		}

		if ( w != window )
		{
			// Window changed
			if ( window != null )
			{
				window.removeWindowListener( this );
			}
			window = w;
			if ( window != null )
			{
				window.addWindowListener( this );
			}
		}
	}



	private boolean isHierarchyVisible()
	{
		Container c = getParent();
		while ( c != null )
		{
			if ( !c.isVisible() )
			{
				return false;
			}
			c = c.getParent();
		}

		return true;
	}
	
	
	
	abstract void notifyQueueReallocation();



	//
	//
	// WINDOW EVENTS
	//
	//

	@Override
	public void windowOpened(WindowEvent e)
	{
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
		sendUnrealiseEvents();
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}


	
	
	//
	//
	// DRAG AND DROP METHODS
	//
	//

	private boolean swingDndCanImport(TransferHandler.TransferSupport transfer)
	{
		return rootElement.swingDndCanImport( transfer );
	}
	
	private boolean swingDndImportData(TransferHandler.TransferSupport transfer)
	{
		return rootElement.swingDndImportData( transfer );
	}
	

	
	//
	//
	// REALISE / UNREALISE
	//
	//
	
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
	
	private void initialise()
	{
		if ( !configured )
		{
			int width = getWidth(), height = getHeight();
			if ( width == 0  &&  height == 0 )
			{
				Rectangle windowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
				width = windowBounds.width;
				height = windowBounds.height;
			}
			rootElement.configureEvent( new Vector2( (double)width, (double)height ) );
			configured = true;
		}
	}




	//
	//
	// FOCUS GAIN / LOSS
	//
	//
	
	@Override
	public void focusGained(FocusEvent e)
	{
		rootElement.componentFocusGained();
	}
	
	@Override
	public void focusLost(FocusEvent e)
	{
		rootElement.componentFocusLost();
	}



	

	
	
	
	//
	//
	// POPUP METHODS
	//
	//
	
	public boolean isPopup()
	{
		return false;
	}
	
	protected abstract PresentationPopupWindow createPopupPresentation(LSElement popupContents, int targetX, int targetY, Anchor popupAnchor,
			boolean closeAutomatically, boolean requestFocus, boolean chainStart);

	protected void onPopupClosingEvent()
	{
	}




	//
	//
	// MOUSE UTILITY FUNCTIONS
	//
	//
	
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


	public abstract PresentationEventErrorLog getEventErrorLog();
}
