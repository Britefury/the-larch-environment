package tests.DocLayout;

import java.lang.Math;
import java.util.Vector;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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

import javax.swing.JComponent;
import javax.swing.JFrame;

import BritefuryJ.DocLayout.DocLayoutNode;
import BritefuryJ.DocLayout.DocLayoutNodeRoot;
import BritefuryJ.DocLayout.DocLayoutNodeText;
import BritefuryJ.DocLayout.VMetrics;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Text.TextVisual;






public abstract class DocLayoutTestBase implements DocLayoutNodeRoot.RootListener
{
	static private class PresentationAreaComponent extends JComponent implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener
	{
		private static final long serialVersionUID = 1L;
		
		public static class InvalidMouseButtonException extends RuntimeException
		{
			private static final long serialVersionUID = 1L;
		}

		
		
		public DocLayoutTestBase test;
		private boolean bShown, bConfigured;
		
		
		public PresentationAreaComponent(DocLayoutTestBase area)
		{
			super();
			
			this.test = area;
			
			bShown = false;
			bConfigured = false;
			
			addComponentListener( this );
			addMouseListener( this );
			addMouseMotionListener( this );
			addMouseWheelListener( this );
			addHierarchyListener( this );
		}
		
		
		public void paint(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;

			RenderingHints aa = new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			RenderingHints taa = new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.addRenderingHints( aa );
			g2.addRenderingHints( taa );
			test.exposeEvent( g2, new Rectangle2D.Double( 0.0, 0.0, (double)getWidth(), (double)getHeight()) );
		}
	
		

		public void mousePressed(MouseEvent e)
		{
			test.mouseDownEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		public void mouseReleased(MouseEvent e)
		{
			test.mouseUpEvent( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		
		public void mouseClicked(MouseEvent e)
		{
			switch ( e.getClickCount() )
			{
			case 2:
				test.mouseDown2Event( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
				break;
			case 3:
				test.mouseDown3Event( getButton( e ), new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
				break;
			default:
				break;
			}
		}

		
		public void mouseMoved(MouseEvent e)
		{
			test.mouseMotionEvent( new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		public void mouseDragged(MouseEvent e)
		{
			test.mouseMotionEvent( new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		public void mouseEntered(MouseEvent e)
		{
			test.mouseEnterEvent( new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}

		public void mouseExited(MouseEvent e)
		{
			test.mouseLeaveEvent( new Point2( (double)e.getX(), (double)e.getY() ), getModifiers( e ) );
		}


		


		public void mouseWheelMoved(MouseWheelEvent e)
		{
			test.mouseWheelEvent( new Point2( (double)e.getX(), (double)e.getY() ), e.getWheelRotation(), e.getUnitsToScroll(), getModifiers( e ) );
		}
		
		
		

		public void keyPressed(KeyEvent e)
		{
			test.keyPressEvent( e, getModifiers( e ) );
		}


		public void keyReleased(KeyEvent e)
		{
			test.keyReleaseEvent( e, getModifiers( e ) );
		}


		public void keyTyped(KeyEvent e)
		{
		}
		
		

		
		public void componentResized(ComponentEvent e)
		{
			test.configureEvent( new Vector2( (double)getWidth(), (double)getHeight() ) );
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
				test.configureEvent( new Vector2( (double)getWidth(), (double)getHeight() ) );
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
					test.realiseEvent();
				}
				else
				{
					test.unrealiseEvent();
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
	
	
	
	private Point2 windowTopLeftCornerInRootSpace;
	private double rootScaleInWindowSpace;
	private Point2 dragStartPosInWindowSpace;
	private int dragButton;
	
	private Vector2 areaSize;
	
	private PresentationAreaComponent component;
	
	private boolean bAllocationRequired;
	
	private DocLayoutNodeRoot rootNode;
	
	private Vector<TextVisual> textVisuals;
	private Vector<DocLayoutNodeText> textNodes;
	
	
		
	
	public DocLayoutTestBase()
	{
		super();
		
		textVisuals = new Vector<TextVisual>();
		textNodes = new Vector<DocLayoutNodeText>();
		
		windowTopLeftCornerInRootSpace = new Point2();
		rootScaleInWindowSpace = 1.0;
		dragStartPosInWindowSpace = new Point2();
		dragButton = 0;
		
		areaSize = new Vector2();
		
		component = new PresentationAreaComponent( this );

		bAllocationRequired = true;
		
		rootNode = createRootNode();
		rootNode.setListener( this );
	
	
		JFrame frame = new JFrame( getTitle() );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		component.setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( component );
		frame.pack();
		frame.setVisible( true );
	}
	
	
	
	protected abstract String getTitle();
	
	
	
	protected DocLayoutNodeRoot createRootNode()
	{
		DocLayoutNodeRoot root = new DocLayoutNodeRoot();
		root.setChild( createContentNode() );
		root.setListener( this );
		return root;
	}
	
	protected abstract DocLayoutNode createContentNode();

	protected void paint(Graphics2D graphics)
	{
		for (int i = 0; i < textNodes.size(); i++)
		{
			DocLayoutNodeText node = textNodes.get( i );
			TextVisual v = textVisuals.get( i );
			
			AffineTransform x = applyNodeOffset( graphics, node );
			
			v.draw( graphics );
			
			graphics.setTransform( x );
		}
	}
	
	
	protected Vector2 getOffset(DocLayoutNode node)
	{
		Vector2 offset = node.getPositionInParent().toVector2();
		
		DocLayoutNode parent = node.getParent();
		
		if ( parent != null )
		{
			offset = offset.add( getOffset( parent ) ); 
		}
		
		return offset;
	}
	
	
	protected AffineTransform applyNodeOffset(Graphics2D graphics, DocLayoutNode node)
	{
		AffineTransform backup = graphics.getTransform();
		Vector2 offset = getOffset( node );
		graphics.translate( offset.x, offset.y );
		return backup;
	}
	
	protected DocLayoutNodeText buildTextNode(String text, Font font, Color colour)
	{
		TextVisual v = new TextVisual( text, font, colour, null );
		v.realise( component );
		
		DocLayoutNodeText node = new DocLayoutNodeText( v );
		
		textVisuals.add( v );
		textNodes.add( node );
		
		return node;
	}
	
	
	
	public JComponent getComponent()
	{
		return component;
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
	
	public void onLayoutRootRelayoutRequest()
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
			rootNode.refreshMinimumHMetrics();
			rootNode.refreshPreferredHMetrics();
			rootNode.allocateX( 0.0, areaSize.x / rootScaleInWindowSpace );
			rootNode.refreshMinimumVMetrics();
			VMetrics v = rootNode.refreshPreferredVMetrics();
			rootNode.allocateY( 0.0, v.height );
			bAllocationRequired = false;
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
		
		// Save the current transform
		AffineTransform transform = graphics.getTransform();
		
		// Apply the transformation
		graphics.scale( rootScaleInWindowSpace, rootScaleInWindowSpace );
		graphics.translate( -windowTopLeftCornerInRootSpace.x, -windowTopLeftCornerInRootSpace.y );
		
		// Draw
		paint( graphics );
		
		// Restore transform
		graphics.setTransform( transform );
	}
	
	
	
	protected void mouseDownEvent(int button, Point2 windowPos, int modifiers)
	{
		component.grabFocus();
	}
	
	protected void mouseUpEvent(int button, Point2 windowPos, int modifiers)
	{
		//Point2 rootPos = windowSpaceToRootSpace( windowPos );
	}
	
	
	
	

	
	protected void mouseMotionEvent(Point2 windowPos, int modifiers)
	{
		//Point2 rootPos = windowSpaceToRootSpace( windowPos );
		
		if ( dragButton == 0 )
		{
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
	}



	protected void mouseEnterEvent(Point2 windowPos, int modifiers)
	{
		//Point2 rootPos = windowSpaceToRootSpace( windowPos );
		
		if ( dragButton == 0 )
		{
		}
	}

	protected void mouseLeaveEvent(Point2 windowPos, int modifiers)
	{
		//Point2 rootPos = windowSpaceToRootSpace( windowPos );
		
		if ( dragButton == 0 )
		{
		}
	}


	protected void mouseDown2Event(int button, Point2 windowPos, int modifiers)
	{
		//Point2 rootPos = windowSpaceToRootSpace( windowPos );
	}


	protected void mouseDown3Event(int button, Point2 windowPos, int modifiers)
	{
		//Point2 rootPos = windowSpaceToRootSpace( windowPos );
	}
	
	
	protected void mouseWheelEvent(Point2 windowPos, int wheelClicks, int unitsToScroll, int modifiers)
	{
		//Point2 rootPos = windowSpaceToRootSpace( windowPos );
		if ( ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.ALT )
		{
			double delta = (double)-wheelClicks;
			double scaleDelta = Math.pow( 2.0,  ( delta / 1.5 ) );
			
			zoom( scaleDelta, windowPos );
		}
		else if ( ( modifiers & Modifier._KEYS_MASK )  !=  0 )
		{
		}
		else
		{
			double delta = (double)wheelClicks;
			panWindowSpace( new Vector2( 0.0, delta * 75.0 ) );
		}
	}
	
	
	
	
	protected boolean keyPressEvent(KeyEvent event, int modifiers)
	{
		//boolean bCtrl = ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.CTRL;
		//boolean bCtrlShift = ( modifiers & Modifier._KEYS_MASK )  ==  ( Modifier.CTRL | Modifier.SHIFT );
		//char keyChar = event.getKeyChar();
		return true;
	}
	
	protected boolean keyReleaseEvent(KeyEvent event, int modifiers)
	{
		//boolean bCtrl = ( modifiers & Modifier._KEYS_MASK )  ==  Modifier.CTRL;
		//boolean bCtrlShift = ( modifiers & Modifier._KEYS_MASK )  ==  ( Modifier.CTRL | Modifier.SHIFT );
		//char keyChar = event.getKeyChar();
		return true;
	}
	
	
	
	
	protected void realiseEvent()
	{
	}
	
	
	protected void unrealiseEvent()
	{
	}
}
