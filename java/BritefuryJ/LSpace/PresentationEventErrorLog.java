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
import BritefuryJ.Pres.RichText.RichSpan;
import BritefuryJ.Pres.UI.Section;
import BritefuryJ.Pres.UI.SectionHeading1;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.TypeUtils;

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
		protected Throwable error;
		
		
		public Entry(Throwable error)
		{
			this.error = error;
		}
		
		
		protected static final AbstractBorder entryBorder = new SolidBorder( 1.5, 2.0, 5.0, 5.0, new Color( 0.5f, 0.05f, 0.25f ), null );
	}
	
	
	private static class PresentationEventEntry extends Entry
	{
		protected String event;

		public PresentationEventEntry(String event, Throwable error)
		{
			super( error );
			this.event = event;
		}

		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Pres header = new NormalText( new Object[] { "Exception during event ", eventPres( event ) } ).alignVRefY();
			return entryBorder.surround( new Column( new Object[] { header, new Spacer( 0.0, 5.0 ), error } ) );
		}
	}


	private static abstract class ElementEntry extends Entry {
		private LSElement element;
		private ElementHoverHighlighter hoverHighlighter;

		public ElementEntry(LSElement element, Throwable error)
		{
			super( error );

			this.element = element;
			hoverHighlighter = new ElementHoverHighlighter( element );
		}


		protected Pres elementPres()
		{
			String elementName = element.getClass().getName();
			int dot = elementName.lastIndexOf( '.' );
			if ( dot != -1 )
			{
				elementName = elementName.substring( dot + 1 );
			}

			String typeName = TypeUtils.nameOfTypeOf(((FragmentView) element.getFragmentContext()).getModel());
			dot = elementName.lastIndexOf( '.' );
			if ( dot != -1 )
			{
				typeName = typeName.substring( dot + 1 );
			}

			Pres elementP = elementNameBorder.surround( elementNameStyle.applyTo( new Label( elementName ) ) ).withElementInteractor( hoverHighlighter );
			Pres modelP = modelTypeNameBorder.surround(modelTypeNameStyle.applyTo(new Label(typeName)));
			return new RichSpan(new Object[] { elementP, " (within fragment for a ", modelP, ")"});
		}


		protected static final StyleSheet elementNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.2f, 0.5f, 0.2f ) ) );
		protected static final AbstractBorder elementNameBorder = new SolidBorder( 1.0, 1.0, 3.0, 3.0,
				new Color( 0.2f, 0.5f, 0.2f ), new Color( 0.95f, 1.0f, 0.95f ),
				new Color( 0.05f, 0.25f, 0.05f ), new Color( 0.8f, 1.0f, 0.8f ));
		protected static final StyleSheet modelTypeNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.2f, 0.2f, 0.5f ) ) );
		protected static final AbstractBorder modelTypeNameBorder = new SolidBorder( 1.0, 1.0, 3.0, 3.0,
				new Color( 0.2f, 0.2f, 0.5f ), new Color( 0.95f, 0.95f, 1.0f ),
				new Color( 0.05f, 0.05f, 0.25f ), new Color( 0.8f, 0.8f, 1.0f ));
	}


	private static class ElementInteractorEventEntry extends ElementEntry
	{
		protected String event;
		private Object interactor;

		public ElementInteractorEventEntry(LSElement element, Object interactor, String event, Throwable error)
		{
			super( element, error );
			
			this.event = event;
			this.interactor = interactor;
		}

		
		protected Pres interactorPres()
		{
			return interactorNameStyle.applyTo(new Label(interactor.getClass().getName()));
		}
		

		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Pres header = new NormalText( new Object[] { "Exception during event ", eventPres( event ),
					" handled by interactor ", interactorPres(), " at region ", elementPres() } ).alignVRefY();
			return entryBorder.surround( new Column( new Object[] { header, new Spacer( 0.0, 5.0 ), error } ) );
		}

	
		protected static final StyleSheet interactorNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.2f, 0.2f, 0.5f ) ) );
		protected static final StyleSheet elementNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.2f, 0.5f, 0.2f ) ) );
		protected static final AbstractBorder elementNameBorder = new SolidBorder( 1.0, 1.0, 3.0, 3.0,
				new Color( 0.2f, 0.5f, 0.2f ), new Color( 0.95f, 1.0f, 0.95f ),
				new Color( 0.05f, 0.25f, 0.05f ), new Color( 0.8f, 1.0f, 0.8f ));
	}





	private static class ClipboardEventEntry extends ElementEntry
	{
		protected String event;
		private Object handler;

		public ClipboardEventEntry(LSRegion region, Object handler, String event, Throwable error)
		{
			super( region, error );

			this.event = event;
			this.handler = handler;
		}


		protected Pres handlerPres()
		{
			return handlerTypeNameStyle.applyTo(new Label(handler.getClass().getName()));
		}


		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Pres header = new NormalText( new Object[] { "Exception during clipboard event ", eventPres( event ),
					" handled by ", handlerPres(), " at region ", elementPres() } ).alignVRefY();
			return entryBorder.surround( new Column( new Object[] { header, new Spacer( 0.0, 5.0 ), error } ) );
		}


		protected static final StyleSheet handlerTypeNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.2f, 0.2f, 0.5f ) ) );
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

	protected void exceptionDuringClipboardOperation(LSRegion region, Object handler, String event, Throwable error)
	{
		entries.add( new ClipboardEventEntry( region, handler, event, error ) );
		incr.onChanged();
	}

	protected void exceptionDuringInvokeLater(Throwable error)
	{
		entries.add( new PresentationEventEntry( "invoke later", error ) );
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
