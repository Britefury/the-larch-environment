//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Help;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

import javax.swing.Timer;

import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.PresentationPopupWindow;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.HoverElementInteractor;
import BritefuryJ.LSpace.Interactor.MotionElementInteractor;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.UI.BubblePopup;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;
import BritefuryJ.Util.WeakIdentityHashMap;

public class AttachTooltip extends CompositePres
{
	private static final BasicStroke dashedStroke = new BasicStroke( 1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f, 4.0f }, 0.0f );
	private static final BasicStroke hoverStroke = new BasicStroke( 1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f, 4.0f }, 0.0f );
	private static final Painter highlightPainter = new FilledOutlinePainter( new Color( 0.5f, 0.5f, 0.5f, 0.1f ), new Color( 0.5f, 0.656f, 0.5f, 0.4f ), dashedStroke );
	private static final Painter hoverPainter = new FilledOutlinePainter( new Color( 0.5f, 0.5f, 0.5f, 0.15f ), new Color( 0.5f, 0.656f, 0.5f, 0.8f ), hoverStroke );
	
	private static final StyleSheet tipStyle = StyleSheet.style( Primitive.fontSize.as( 11 ), Primitive.editable.as( false ) );
	
	
	private static int HIGHLIGHT_TIMEOUT = 500;
	private static int TIMEOUT = 800;
	
	private static WeakIdentityHashMap<PresentationComponent, Stack<LSElement>> tooltipStacks = new WeakIdentityHashMap<PresentationComponent, Stack<LSElement>>(); 

	
	private class TooltipInteractor implements HoverElementInteractor, MotionElementInteractor
	{
		@Override
		public void pointerEnter(LSElement element, PointerMotionEvent event)
		{
			startTimer( element, getTimeout() );
		}

		@Override
		public void pointerLeave(LSElement element, PointerMotionEvent event)
		{
			stopTimer( element );
		}

		@Override
		public void pointerMotion(LSElement element, PointerMotionEvent event)
		{
			startTimer( element, getTimeout() );
		}

		@Override
		public void pointerLeaveIntoChild(LSElement element, PointerMotionEvent event)
		{
		}

		@Override
		public void pointerEnterFromChild(LSElement element, PointerMotionEvent event)
		{
		}
		
		
		protected int getTimeout()
		{
			return TIMEOUT;
		}
	}
	
	
	private class HighlightInteractor extends TooltipInteractor implements ElementPainter
	{
		private Stack<LSElement> getStack(LSElement element)
		{
			PresentationComponent component = element.getRootElement().getComponent();
			
			Stack<LSElement> stack = tooltipStacks.get( component );
			if ( stack == null )
			{
				stack = new Stack<LSElement>();
				tooltipStacks.put( component, stack );
			}
			return stack;
		}
		
		
		@Override
		public void pointerEnter(LSElement element, PointerMotionEvent event)
		{
			super.pointerEnter( element, event );

			Stack<LSElement> stack = getStack( element );
			if ( stack.size() > 0 )
			{
				stack.lastElement().queueFullRedraw();
			}
			stack.add( element );
			element.queueFullRedraw();
		}

		@Override
		public void pointerLeave(LSElement element, PointerMotionEvent event)
		{
			super.pointerLeave( element, event );

			Stack<LSElement> stack = getStack( element );
			element.queueFullRedraw();
			stack.remove( element );
			if ( stack.size() > 0 )
			{
				stack.lastElement().queueFullRedraw();
			}
		}


		
		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
		}

		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
			Stack<LSElement> stack = getStack( element );

			Painter painter;
			if ( stack.size() > 0  &&  element == stack.lastElement())
			{
				painter = hoverPainter;
			}
			else
			{
				painter = highlightPainter;
			}
			
			painter.drawShapes( graphics, element.getShapes() );
		}

	
		protected int getTimeout()
		{
			return HIGHLIGHT_TIMEOUT;
		}
	}
	
	
	private class HelpPopup
	{
		private Timer timer;
		private PresentationPopupWindow popup;
		
		
		private HelpPopup(final LSElement targetElement, int timeout)
		{
			ActionListener listener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					display( targetElement );
				}
			};
			
			timer = new Timer( timeout, listener );
			timer.setRepeats( false );
		}
		
		
		private void restart()
		{
			if ( popup != null )
			{
				popup.closePopup();
			}
			timer.restart();
		}
		
		private void stop()
		{
			if ( popup != null )
			{
				popup.closePopup();
			}
			timer.stop();
		}
		
		private void display(LSElement targetElement)
		{
			popup = BubblePopup.popupInBubbleAdjacentTo( tipStyle.applyTo( help ), targetElement, Anchor.BOTTOM, true, false );
		}
	}
	
	
	
	private Pres contents;
	private Pres help;
	private TooltipInteractor tooltipInteractor;
	private HighlightInteractor highlightInteractor;
	private WeakIdentityHashMap<LSElement, HelpPopup> popups = new WeakIdentityHashMap<LSElement, HelpPopup>();
	private LiveFunction presFn;
	
	
	
	public AttachTooltip(Object contents, Object help, boolean alwaysAvailable)
	{
		this( contents, Pres.coerce( help ), alwaysAvailable );
	}
	
	public AttachTooltip(Object contents, Object help)
	{
		this( contents, Pres.coerce( help ), true );
	}
	
	public AttachTooltip(Object contents, String help, boolean alwaysAvailable)
	{
		this( contents, TipBox.multilineTextTip( help ), alwaysAvailable );
	}
	
	public AttachTooltip(Object contents, String help)
	{
		this( contents, TipBox.multilineTextTip( help ), true );
	}
	

	
	private AttachTooltip(Object contents, Pres help, final boolean alwaysAvailable)
	{
		this.contents = Pres.coerce( contents );
		this.help = Pres.coerce( help );
		tooltipInteractor = new TooltipInteractor();
		highlightInteractor = new HighlightInteractor();
		
		LiveFunction.Function fn = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				if ( areHighlightsEnabled() )
				{
					return new Proxy( AttachTooltip.this.contents ).withElementInteractor( highlightInteractor ).withPainter( highlightInteractor );
				}
				else
				{
					if ( alwaysAvailable )
					{
						return new Proxy( AttachTooltip.this.contents ).withElementInteractor( tooltipInteractor );
					}
					else
					{
						return AttachTooltip.this.contents;
					}
				}
			}
		};
		presFn = new LiveFunction( fn );
	}
	
	
	private void startTimer(LSElement element, int timeout)
	{
		HelpPopup popup = popups.get( element );
		if ( popup == null )
		{
			popup = new HelpPopup( element, timeout );
			popups.put( element, popup );
		}
		else
		{
			popup.restart();
		}
	}
	
	private void stopTimer(LSElement element)
	{
		HelpPopup popup = popups.get( element );
		if ( popup != null )
		{
			popup.stop();
		}
	}
	
	
	
	public Pres pres(PresentationContext ctx, StyleValues style)
	{
		return presFn;
	}
	
	
	
	
	//
	// Help enabling and disabling
	//
	
	private static LiveValue highlightTips = new LiveValue( false );
	
	

	public static boolean areHighlightsEnabled()
	{
		return (Boolean)highlightTips.getValue();
	}
	
	public static void enableHighlights()
	{
		highlightTips.setLiteralValue( true );
	}
	
	public static void disableHighlights()
	{
		highlightTips.setLiteralValue( false );
	}
	
	public static void toggleHighlights()
	{
		boolean value = (Boolean)highlightTips.getStaticValue();
		highlightTips.setLiteralValue( !value );
	}
}
