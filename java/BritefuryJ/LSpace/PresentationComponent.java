//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
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
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
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
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Input.DndDragSwing;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.Util.WindowTransparency;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;

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
		private boolean open, chainStart;
		private Point2 screenPosition;
		private Vector2 screenSize;
		
		private PresentationPopup(PopupChain popupChain, Window ownerWindow, PresentationComponent parentComponent, LSElement popupContents, int screenX, int screenY,
				Anchor popupAnchor, boolean closeOnLoseFocus, boolean requestFocus, boolean chainStart)
		{
			chain = popupChain;
			chain.addPopup( this );
			open = true;
			this.chainStart = chainStart;
			
			// Create the popup window
			popupWindow = new JWindow( ownerWindow );
			if ( requestFocus )
			{
				popupWindow.setAlwaysOnTop( true );
				popupWindow.setFocusable( true );
			}
			popupWindow.getContentPane().setBackground( Color.WHITE );
			
			// Create a presentation component for the popup contents, and add them
			popupComponent = new PresentationComponent( this );
			popupComponent.setPageController( parentComponent.getPageController() );
			popupComponent.getRootElement().setChild( popupContents.layoutWrap( HAlignment.EXPAND, VAlignment.EXPAND ) );
			
			popupWindow.add( popupComponent );
			
			
			popupWindow.pack();
			Dimension sz = popupWindow.getSize();
			Point2 windowAnchor = new Point2( sz.getWidth() * popupAnchor.getPropX(), sz.getHeight() * popupAnchor.getPropY() );
			screenX -= (int)( windowAnchor.x + 0.5 );
			screenY -= (int)( windowAnchor.y + 0.5 );
			
			// Ensure >= 0
			screenX = Math.max( screenX, 0 );
			screenY = Math.max( screenY, 0 );
			
			// Ensure that it is not offscreen due to width/height
			Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize();
			screenX = Math.min( screenX, (int)( screenSz.getWidth() - sz.getWidth() ) );
			screenY = Math.min( screenY, (int)( screenSz.getHeight() - sz.getHeight() ) );

			popupWindow.setLocation( screenX, screenY );
			
			screenPosition = new Point2( screenX, screenY );
			screenSize = new Vector2( sz.getWidth(), sz.getHeight() );

			
			popupWindow.setVisible( true );
			if ( requestFocus )
			{
				popupWindow.requestFocus();
			}
			
			
			WindowTransparency.setWindowOpaque( popupWindow, false );

		
			WindowFocusListener focusListener = new WindowFocusListener()
			{
				public void windowGainedFocus(WindowEvent arg0)
				{
				}

				public void windowLostFocus(WindowEvent arg0)
				{
					// If the popup has no child
					if ( open )
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
			open = false;
			popupWindow.setVisible( false );
		}
		
		
		public PresentationComponent getPresentationComponent()
		{
			return popupComponent;
		}
		
		
		public Point2 getPositionOnScreen()
		{
			return screenPosition;
		}
		
		public Vector2 getSizeOnScreen()
		{
			return screenSize;
		}
		
		
		public boolean isChainStart()
		{
			return chainStart;
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
				LSRootElement rootElement = p.popupComponent.getRootElement();
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
			int index = 0;
			for (PresentationPopup p: popups)
			{
				if ( p.isChainStart()  &&  index != 0 )
				{
					// Request focus for remaining popup window
					p.popupWindow.requestFocus();
					break;
				}
				
				p.closePopup();
				
				index++;
			}

			ArrayList<PresentationPopup> ps = new ArrayList<PresentationPopup>();
			ps.addAll( popups.subList( index, popups.size() ) );
			popups = ps;
		}
	}
	
	
	
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
						return clipboardHandler.getExportActions( rootElement.getSelection() );
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
						return clipboardHandler.createExportTransferable( rootElement.getSelection() );
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
						clipboardHandler.exportDone( rootElement.getSelection(), rootElement.getTarget(), data, action );
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
							return clipboardHandler.canImport( rootElement.getTarget(), rootElement.getSelection(), new DataTransfer( transfer ) );
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
					Target target = rootElement.getTarget();
					LSRegion region = target != null  ?  target.getRegion()  :  null;
					if ( region != null )
					{
						ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
						if ( clipboardHandler != null )
						{
							return clipboardHandler.importData( rootElement.getTarget(), rootElement.getSelection(), new DataTransfer( transfer ) );
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
	private PresentationPopup containingPopup = null;
	
	
	
	
	public PresentationComponent()
	{
		this( null );
	}
	
	private PresentationComponent(PresentationPopup containingPopup)
	{
		super();
		
		this.containingPopup = containingPopup;
		
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
	
	private Dimension clampSize(Dimension contentSize)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = new Dimension();
		d.setSize( Math.min( contentSize.getWidth(), screenSize.getWidth() ), Math.min( contentSize.getHeight(), screenSize.getHeight() ) );
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
	public void hierarchyChanged(HierarchyEvent e)
	{
		sendRealiseEvents();
	}
	
	
	
	void notifyQueueReallocation()
	{
		if ( isPopup() )
		{
			Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					Dimension s = clampSize( rootElement.allocateAndGetPreferredSize() );
					containingPopup.popupWindow.setSize( s );
				}
			};
			
			SwingUtilities.invokeLater( r );
		}
		else if ( !isMinimumSizeSet()  ||  !isPreferredSizeSet()  ||  !isMaximumSizeSet() )
		{
			invalidate();
		}
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
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				width = screenSize.width;
				height = screenSize.height;
			}
			rootElement.configureEvent( new Vector2( (double)width, (double)height ) );
			configured = true;
		}
	}
	
	
	
	
	//
	//
	// SHOW / HIDE
	//
	//
	
	@Override
	public void componentShown(ComponentEvent e)
	{
		sendRealiseEvents();
	}

	@Override
	public void componentHidden(ComponentEvent e)
	{
		sendUnrealiseEvents();
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
	
	
	protected PresentationPopup createPopupPresentation(LSElement popupContents, int targetX, int targetY, Anchor popupAnchor,
			boolean bCloseOnLoseFocus, boolean bRequestFocus, boolean chainStart)
	{
		// Offset the popup position by the location of this presentation component on the screen
		Point locOnScreen = getLocationOnScreen();
		targetX += locOnScreen.x;
		targetY += locOnScreen.y;
		
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

		return new PresentationPopup( chain, ownerWindow, this, popupContents, targetX, targetY, popupAnchor, bCloseOnLoseFocus, bRequestFocus, chainStart );
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
}
