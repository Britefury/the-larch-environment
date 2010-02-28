//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.Modifier;

public class ControlsStyleSheet extends StyleSheet
{
	private static final Font defaultLinkFont = new Font( "Sans serif", Font.PLAIN, 14 );
	private static final Cursor defaultLinkCursor = new Cursor( Cursor.HAND_CURSOR );
	
	
	public static final ControlsStyleSheet instance = new ControlsStyleSheet();

	
	
	
	public static abstract class LinkListener extends ElementInteractor
	{
		public boolean onButtonDown(DPWidget element, PointerButtonEvent event)
		{
			return true;
		}

		public boolean onButtonUp(DPWidget element, PointerButtonEvent event)
		{
			if ( element.isRealised() )
			{
				return onLinkClicked( element, event );
			}
			
			return false;
		}
		
		
		
		protected abstract boolean onLinkClicked(DPWidget element, PointerButtonEvent event);
	}
	
	
	private static class LinkTargetListener extends LinkListener
	{
		private String targetLocation;
		
		
		public LinkTargetListener(String targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public boolean onLinkClicked(DPWidget link, PointerButtonEvent buttonEvent)
		{
			PageController pageController = link.getPresentationArea().getPageController();
			if ( ( buttonEvent.getPointer().getModifiers() & Modifier.CTRL ) != 0 )
			{
				if ( buttonEvent.getButton() == 1  ||  buttonEvent.getButton() == 2 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
					return true;
				}
			}
			else
			{
				if ( buttonEvent.getButton() == 1 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_CURRENT_TAB );
					return true;
				}
				else if ( buttonEvent.getButton() == 2 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_NEW_TAB );
					return true;
				}
			}

			return false;
		}
	}
	
	
	private static class PyLinkListener extends LinkListener
	{
		private PyObject callable;
		
		
		public PyLinkListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public boolean onLinkClicked(DPWidget element, PointerButtonEvent buttonEvent)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( element ), Py.java2py( buttonEvent ) ) );
		}
	}
	

	
	
	
	public static interface ButtonListener
	{
		public boolean onButtonClicked(DPWidget element, PointerButtonEvent event);
	}
	
	
	private static class PyButtonListener implements ButtonListener
	{
		private PyObject callable;
		
		
		public PyButtonListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public boolean onButtonClicked(DPWidget element, PointerButtonEvent buttonEvent)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( element ), Py.java2py( buttonEvent ) ) );
		}
	}
	

	private static class ButtonInteractor extends ElementInteractor
	{
		private Border buttonBorder, highlightBorder;
		private DPBorder element;
		private ButtonListener listener;
		
		private ButtonInteractor(DPBorder element, Border buttonBorder, Border highlightBorder, ButtonListener listener)
		{
			this.element = element;
			this.buttonBorder = buttonBorder;
			this.highlightBorder = highlightBorder;
			this.listener = listener;
		}
		
		
		public boolean onButtonDown(DPWidget element, PointerButtonEvent event)
		{
			return true;
		}

		public boolean onButtonUp(DPWidget element, PointerButtonEvent event)
		{
			if ( element.isRealised() )
			{
				return listener.onButtonClicked( element, event );
			}
			
			return false;
		}
		
		
		public void onEnter(DPWidget element, PointerMotionEvent event)
		{
			this.element.setBorder( highlightBorder );
		}

		public void onLeave(DPWidget element, PointerMotionEvent event)
		{
			this.element.setBorder( buttonBorder );
		}
	}

	
	
	
	public ControlsStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyleSheet", PrimitiveStyleSheet.instance );

		initAttr( "linkAttrs", new AttributeSet( new String[] { "font", "foreground", "cursor" }, new Object[] { defaultLinkFont, Color.blue, defaultLinkCursor } ) );
		
		initAttr( "buttonBorderThickness", 1.0 );
		initAttr( "buttonMargin", 3.0 );
		initAttr( "buttonRounding", 10.0 );
		initAttr( "buttonBorderPaint", new Color( 0.55f, 0.525f, 0.5f ) );
		initAttr( "buttonBorderHighlightPaint", new Color( 0.0f, 0.5f, 0.5f ) );
		initAttr( "buttonBackgPaint", new Color( 0.85f, 0.85f, 0.85f ) );
		initAttr( "buttonBackgHighlightPaint", new Color( 0.925f, 0.925f, 0.925f ) );
	}

	
	protected StyleSheet newInstance()
	{
		return new ControlsStyleSheet();
	}
	

	
	public ControlsStyleSheet withPrimitiveStyleSheet(PrimitiveStyleSheet styleSheet)
	{
		return (ControlsStyleSheet)withAttr( "primitiveStyleSheet", styleSheet );
	}
	
	public ControlsStyleSheet withLinkAttrs(AttributeSet attrs)
	{
		return (ControlsStyleSheet)withAttr( "linkAttrs", attrs );
	}
	
	public ControlsStyleSheet withButtonAttrs(AttributeSet attrs)
	{
		return (ControlsStyleSheet)withAttr( "buttonAttrs", attrs );
	}
	
	
	
	
	private PrimitiveStyleSheet linkStyleSheet = null;

	private PrimitiveStyleSheet getLinkStyleSheet()
	{
		if ( linkStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeSet linkAttrs = getNonNull( "linkAttrs", AttributeSet.class, AttributeSet.identity );
			linkStyleSheet = (PrimitiveStyleSheet)primitive.withAttrSet( linkAttrs );
		}
		return linkStyleSheet;
	}
	
	
	
	private Border buttonBorder = null;

	private Border getButtonBorder()
	{
		if ( buttonBorder == null )
		{
			double thickness = getNonNull( "buttonBorderThickness", Double.class, 1.0 );
			double margin = getNonNull( "buttonMargin", Double.class, 5.0 );
			double rounding = getNonNull( "buttonRounding", Double.class, 10.0 );
			Paint borderPaint = getNonNull( "buttonBorderPaint", Paint.class, Color.black );
			Paint backgPaint = getNonNull( "buttonBackgPaint", Paint.class, new Color( 1.0f, 1.0f, 0.7f ) );

			buttonBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return buttonBorder;
	}
	
	
	
	private Border buttonHighlightBorder = null;

	private Border getButtonHighlightBorder()
	{
		if ( buttonHighlightBorder == null )
		{
			double thickness = getNonNull( "buttonBorderThickness", Double.class, 1.0 );
			double margin = getNonNull( "buttonMargin", Double.class, 5.0 );
			double rounding = getNonNull( "buttonRounding", Double.class, 10.0 );
			Paint borderPaint = getNonNull( "buttonBorderHighlightPaint", Paint.class, Color.black );
			Paint backgPaint = getNonNull( "buttonBackgHighlightPaint", Paint.class, new Color( 1.0f, 1.0f, 0.7f ) );

			buttonHighlightBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return buttonHighlightBorder;
	}
	

	
	private PrimitiveStyleSheet buttonStyleSheet = null;

	private PrimitiveStyleSheet getButtonStyleSheet()
	{
		if ( buttonStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			buttonStyleSheet = primitive.withBorder( getButtonBorder() );
		}
		return buttonStyleSheet;
	}
	
	
	
	
	public DPWidget link(String txt, String targetLocation)
	{
		return link( txt, new LinkTargetListener( targetLocation ) );
	}
	
	public DPWidget link(String txt, LinkListener listener)
	{
		DPWidget element = getLinkStyleSheet().staticText( txt );
		element.addInteractor( listener );
		return element;
	}
	
	public DPWidget link(String txt, PyObject listener)
	{
		return link( txt, new PyLinkListener( listener ) );
	}
	
	
	
	public DPWidget button(DPWidget child, ButtonListener listener)
	{
		DPBorder element = getButtonStyleSheet().border( child );
		ButtonInteractor interactor = new ButtonInteractor( element, getButtonBorder(), getButtonHighlightBorder(), listener );
		element.addInteractor( interactor );
		return element;
	}

	public DPWidget button(DPWidget child, PyObject listener)
	{
		return button( child, new PyButtonListener( listener ) );
	}
}
