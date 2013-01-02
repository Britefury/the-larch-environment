//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.HoverElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.UI.Section;
import BritefuryJ.Pres.UI.SectionHeading1;
import BritefuryJ.StyleSheet.StyleSheet;

public class PresentationEventErrorLog implements Presentable
{
	private static class ElementHoverHighlighter implements HoverElementInteractor
	{
		private LSElement elementToHighlight;
		
		
		public ElementHoverHighlighter(LSElement element)
		{
			this.elementToHighlight = element;
		}
		
		
		@Override
		public void pointerEnter(LSElement element, PointerMotionEvent event)
		{
			elementHighlight.highlight( elementToHighlight );
		}

		@Override
		public void pointerLeave(LSElement element, PointerMotionEvent event)
		{
			elementHighlight.unhighlight( elementToHighlight );
		}
	}
	
	
	public static abstract class Entry implements Presentable
	{
		protected String event;
		protected Throwable error;
		
		
		public Entry(String event, Throwable error)
		{
			this.event = event;
			this.error = error;
		}
		
		
		protected static final AbstractBorder entryBorder = new SolidBorder( 1.5, 2.0, 5.0, 5.0, new Color( 0.5f, 0.05f, 0.25f ), null );
	}
	
	
	private static class PresentationEventEntry extends Entry
	{
		public PresentationEventEntry(String event, Throwable error)
		{
			super( event, error );
		}

		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Pres header = new NormalText( new Object[] { "Exception during event ", eventPres( event ) } ).alignVRefY();
			return entryBorder.surround( new Column( new Object[] { header, new Spacer( 0.0, 5.0 ), error } ) );
		}
	}
	
	
	private static class ElementInteractorEventEntry extends Entry
	{
		private LSElement element;
		private Object interactor;
		private ElementHoverHighlighter hoverHighlighter;
		
		public ElementInteractorEventEntry(LSElement element, Object interactor, String event, Throwable error)
		{
			super( event, error );
			
			this.element = element;
			this.interactor = interactor;
			hoverHighlighter = new ElementHoverHighlighter( element );
		}

		
		protected Pres interactorPres()
		{
			return interactorNameStyle.applyTo( new Label( interactor.getClass().getName() ) );
		}
		
		protected Pres elementPres()
		{
			String elementName = element.getClass().getName();
			int dot = elementName.lastIndexOf( '.' );
			if ( dot != -1 )
			{
				elementName = elementName.substring( dot + 1 );
			}
			return elementNameBorder.surround( elementNameStyle.applyTo( new Label( elementName ) ) ).withElementInteractor( hoverHighlighter );
		}
		
		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Pres header = new NormalText( new Object[] { "Exception during event ", eventPres( event ),
					" handled by interactor ", interactorPres(), " at element ", elementPres() } ).alignVRefY();
			return entryBorder.surround( new Column( new Object[] { header, new Spacer( 0.0, 5.0 ), error } ) );
		}

	
		protected static final StyleSheet interactorNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.2f, 0.2f, 0.5f ) ) );
		protected static final StyleSheet elementNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.2f, 0.5f, 0.2f ) ) );
		protected static final AbstractBorder elementNameBorder = new SolidBorder( 1.0, 1.0, 3.0, 3.0,
				new Color( 0.2f, 0.5f, 0.2f ), new Color( 0.95f, 1.0f, 0.95f ),
				new Color( 0.05f, 0.25f, 0.05f ), new Color( 0.8f, 1.0f, 0.8f ));
	}
	
	
	
	private IncrementalValueMonitor incr = new IncrementalValueMonitor();
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	
	
	
	public PresentationEventErrorLog()
	{
	}
	
	
	protected void exceptionDuringPresentationEventHandler(String event, Throwable error)
	{
		entries.add( new PresentationEventEntry( event, error ) );
		incr.onChanged();
	}
	
	protected void exceptionDuringElementInteractor(LSElement element, Object interactor, String event, Throwable error)
	{
		entries.add( new ElementInteractorEventEntry( element, interactor, event, error ) );
		incr.onChanged();
	}
	
	

	protected static Pres eventPres(String event)
	{
		return eventNameStyle.applyTo( new Label( event ) );
	}
	


	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		incr.onAccess();
		return new Section( new SectionHeading1( "Presentation event error log" ), entryListStyle.applyTo( new Column( entries.toArray() ) ) );
	}

	
	
	protected static final StyleSheet eventNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.5f, 0.0f, 0.5f ) ) );
	private static final StyleSheet entryListStyle = StyleSheet.style( Primitive.columnSpacing.as( 7.0 ) );


	private static final BasicStroke dashedStroke = new BasicStroke( 1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f, 4.0f }, 0.0f );
	protected static final ElementHighlighter elementHighlight = new ElementHighlighter( 
			new FilledOutlinePainter( new Color( 1.0f, 0.0f, 0.0f, 0.1f ), new Color( 0.5f, 0.0f, 0.0f, 0.5f ), dashedStroke ) );
}
