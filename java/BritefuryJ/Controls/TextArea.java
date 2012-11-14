//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.ViewFragmentFunction;
import BritefuryJ.LSpace.ElementValueFunction;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TextEditEventInsert;
import BritefuryJ.LSpace.TextEditEventRemove;
import BritefuryJ.LSpace.TextEditEventReplace;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Clipboard.TextClipboardHandler;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.Segment;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.Primitive.Whitespace;
import BritefuryJ.Projection.Perspective;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;
import BritefuryJ.Util.RichString.RichStringBuilder;

public class TextArea extends ControlPres
{
	private static interface TextAreaPresentable
	{
		public Pres textAreaPresent(FragmentView fragment, SimpleAttributeTable inheritedState);
	}
	
	
	private static class TextAreaViewFragmentFn implements ViewFragmentFunction
	{
		public Pres createViewFragment(Object x, FragmentView ctx, SimpleAttributeTable inheritedState)
		{
			TextAreaPresentable p = (TextAreaPresentable)x;
			return p.textAreaPresent( ctx, inheritedState );
		}
	}
	
	protected static Perspective textAreaPerspective = new Perspective( new TextAreaViewFragmentFn() );
	
	
	
	public static interface TextToPresFn
	{
		Pres textToPres(String text);
	}
	
	
	
	public static class RegexPresTable implements TextToPresFn
	{
		private ArrayList<Pattern> patterns = new ArrayList<Pattern>();
		private ArrayList<TextToPresFn> functions = new ArrayList<TextToPresFn>();
		private Pattern completePattern = null;
		
		public RegexPresTable()
		{
		}
		
		
		public void addPattern(Pattern pat, TextToPresFn function)
		{
			patterns.add( pat );
			functions.add( function );
		}
		
		
		@Override
		public Pres textToPres(String text)
		{
			if ( completePattern == null )
			{
				StringBuilder r = new StringBuilder();
				boolean first = true;
				for (Pattern pat: patterns)
				{
					if ( !first )
					{
						r.append( "|" );
					}
					r.append( "(" ).append( pat.pattern() ).append( ")" );
					first = false;
				}
				completePattern = Pattern.compile( r.toString() ); 
			}
			
			
			if ( text.length() == 0 )
			{
				return new Text( text );
			}
			else
			{
				ArrayList<Pres> p = new ArrayList<Pres>();
				Matcher m = completePattern.matcher( text );
				
				int pos = 0;
				while ( pos < text.length() )
				{
					if ( m.find( pos ) )
					{
						int start = m.start(), end = m.end();
						if ( start > pos )
						{
							p.add( new Text( text.substring( pos, start ) ) );
						}
						// Determine which pattern was matched
						String seq = text.substring( start, end );
						boolean patternFound = false;
						for (int i = 0; i < patterns.size(); i++)
						{
							if ( patterns.get( i ).matcher( seq ).matches() )
							{
								p.add( functions.get( i ).textToPres( seq ) );
								patternFound = true;
								break;
							}
						}
						if ( !patternFound )
						{
							throw new RuntimeException( "This shouldn't have happened; could not identify which pattern matched '" + seq + "'" );
						}
						pos = end;
					}
					else
					{
						// No match - finish
						p.add( new Text( text.substring( pos ) ) );
						break;
					}
				}
				
				if ( p.size() == 1 )
				{
					return p.get( 0 );
				}
				else
				{
					return new Span( p.toArray( new Pres[] {} ) );
				}
			}
		}
		
	}

	
	
	public static class TextAreaListener
	{
		public void onAccept(TextAreaControl textArea, String text)
		{
		}

		public void onTextInserted(TextAreaControl textArea, int position, String textInserted)
		{
		}

		public void onTextRemoved(TextAreaControl textArea, int position, int length)
		{
		}
		
		public void onTextReplaced(TextAreaControl textArea, int position, int length, String replacementText)
		{
		}
		
		public void onTextChanged(TextAreaControl textArea)
		{
		}
	}
	
	
	public static class TextAreaControl extends Control implements IncrementalMonitorListener
	{
		private class TextAreaInteractor implements KeyElementInteractor
		{
			private TextAreaInteractor()
			{
			}
			
			
			@Override
			public boolean keyPressed(LSElement element, KeyEvent event)
			{
				return event.isControlDown()  &&  event.getKeyCode() == KeyEvent.VK_ENTER;
			}
	
			@Override
			public boolean keyReleased(LSElement element, KeyEvent event)
			{
				if ( event.isControlDown()  &&  event.getKeyCode() == KeyEvent.VK_ENTER )
				{
					accept();
					return true;
				}
				return false;
			}
	
			@Override
			public boolean keyTyped(LSElement element, KeyEvent event)
			{
				return event.isControlDown()  &&  event.getKeyChar() == KeyEvent.VK_ENTER;
			}
		}
		
		
				
		private class ValueFn implements ElementValueFunction
		{
			public Object computeElementValue(LSElement element)
			{
				return getDisplayedText();
			}
	
			public void addPrefixToRichString(RichStringBuilder builder, LSElement element)
			{
			}
	
			public void addSuffixToRichString(RichStringBuilder builder, LSElement element)
			{
			}
		}
		
		
		private class TextAreaBox implements TextAreaPresentable
		{
			private class TextAreaClipboardHandler extends TextClipboardHandler
			{
				@Override
				protected void deleteText(TextSelection selection, Caret caret)
				{
					int startPosition = selection.getStartMarker().getClampedIndexInSubtree( element );
					int endPosition = selection.getEndMarker().getClampedIndexInSubtree( element );
		
					String newText = element.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  element.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
					setText( newText );
					
					if ( listener != null )
					{
						listener.onTextRemoved( TextAreaControl.this, startPosition, endPosition - startPosition );
						listener.onTextChanged( TextAreaControl.this );
					}
				}
				
				@Override
				protected void insertText(Marker marker, String text)
				{
					marker.getElement().insertText( marker, text );
					
					// Don't inform the listener - the text edit event will take care of that
				}
				
				@Override
				protected void replaceText(TextSelection selection, Caret caret, String replacement)
				{
					int startPosition = selection.getStartMarker().getClampedIndexInSubtree( element );
					int endPosition = selection.getEndMarker().getClampedIndexInSubtree( element );
		
					String newText = element.getTextRepresentationFromStartToMarker( selection.getStartMarker() )  +  replacement  +  element.getTextRepresentationFromMarkerToEnd( selection.getEndMarker() );
					setText( newText );
					
					if ( listener != null )
					{
						listener.onTextReplaced( TextAreaControl.this, startPosition, endPosition - startPosition, replacement );
						listener.onTextChanged( TextAreaControl.this );
					}
				}
				
				@Override
				protected String getText(TextSelection selection)
				{
					return element.getRootElement().getTextRepresentationInSelection( selection );
				}
			}
			
			

			private class TextLine implements TextAreaPresentable
			{
				private class TextAreaTextLineTreeEventListener implements TreeEventListener
				{
					@Override
					public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
					{
						if ( event instanceof TextEditEvent )
						{
							String lineText = element.getTextRepresentation();
							int lineOffset = sourceElement.getTextRepresentationOffsetInSubtree( TextAreaControl.this.element );
							
							int indexOfNewline = lineText.indexOf( "\n" );
							if ( indexOfNewline == -1 )
							{
								// No newline characters at all - join with the next line
								lineJoin( TextLine.this, lineText );
							}
							else if ( lineText.length() > 0  &&  indexOfNewline  ==  lineText.length() - 1 )
							{
								// The line text has characters and the first newline is at the end - no need to merge or split text lines
								text = lineText.substring( 0, lineText.length() - 1 );
								PresentationStateListenerList.onPresentationStateChanged( listeners, TextLine.this );
							}
							else
							{
								// There are newline characters in the text, just not only at the end - split the line up
								lineModified( TextLine.this, lineText );
							}
							
							if ( listener != null )
							{
								if ( event instanceof TextEditEventInsert )
								{
									TextEditEventInsert insert = (TextEditEventInsert)event;
									listener.onTextInserted( TextAreaControl.this, lineOffset + insert.getPosition(), insert.getTextInserted() );
									listener.onTextChanged( TextAreaControl.this );
								}
								else if ( event instanceof TextEditEventRemove )
								{
									TextEditEventRemove remove = (TextEditEventRemove)event;
									listener.onTextRemoved( TextAreaControl.this, lineOffset + remove.getPosition(), remove.getLength() );
									listener.onTextChanged( TextAreaControl.this );
								}
								else if ( event instanceof TextEditEventReplace )
								{
									TextEditEventReplace replace = (TextEditEventReplace)event;
									listener.onTextReplaced( TextAreaControl.this, lineOffset + replace.getPosition(), replace.getLength(), replace.getReplacement() );
									listener.onTextChanged( TextAreaControl.this );
								}
							}

							return true;
						}
						return false;
					}
				}
			

				
				private String text;
				private PresentationStateListenerList listeners = null;
				private TextAreaTextLineTreeEventListener textLineTreeEventListener = new TextAreaTextLineTreeEventListener();
				
				
				public TextLine(String t)
				{
					this.text = t;
				}
				
				
				
				@Override
				public Pres textAreaPresent(FragmentView fragment, SimpleAttributeTable inheritedState)
				{
					fragment.disableInspector();
					
					listeners = PresentationStateListenerList.addListener( listeners, fragment );
					
					Pres textPres;
					if ( textToPres != null )
					{
						textPres = textToPres.textToPres( text );
					}
					else
					{
						textPres = new Text( text );
					}
					Pres seg = new Segment( false, false, textPres );
					Pres newline = new Whitespace( "\n" );
					
					return new Paragraph( new Object[] { seg, newline } ).withTreeEventListener( textLineTreeEventListener );
				}
			}
			
			
			

			
			private PresentationStateListenerList listeners = null;
			private ArrayList<TextLine> lines = new ArrayList<TextLine>();
			private TextAreaClipboardHandler clipboardHandler = new TextAreaClipboardHandler();

			
			
			public TextAreaBox()
			{
				lines.add( new TextLine( "" ) );
			}
			
			
			@Override
			public Pres textAreaPresent(FragmentView fragment, SimpleAttributeTable inheritedState)
			{
				fragment.disableInspector();
				
				listeners = PresentationStateListenerList.addListener( listeners, fragment );
				
				Pres textBox = new Column( lines.toArray() ).alignHPack();
				return new Region( textBox, clipboardHandler ); 
			}
			
			
			public String getText()
			{
				StringBuilder builder = new StringBuilder();
				boolean first = true;
				for (TextLine line: lines)
				{
					if ( !first )
					{
						builder.append( "\n" );
					}
					builder.append( line.text );
					first = false;
				}
				return builder.toString();
			}
			
			public void setText(String text)
			{
				if ( text.length() == 0 )
				{
					lines.clear();
					lines.add( new TextLine( "" ) );
				}
				else
				{
					String textLines[] = text.split( "\\r?\\n", -1 );
					
					lines.clear();
					for (String line: textLines)
					{
						lines.add( new TextLine( line ) );
					}
				}
				PresentationStateListenerList.onPresentationStateChanged( listeners, this );
			}
			
			private void lineModified(TextLine line, String lineText)
			{
				int index = lines.indexOf( line );
				
				String withoutLastNewline = lineText.substring( 0, lineText.length() - 1 );

				String lineTextSplitIntoLines[] = withoutLastNewline.split( "\\r?\\n", -1 );
				
				
				ArrayList<TextLine> replacementLines = new ArrayList<TextLine>();
				for (String t: lineTextSplitIntoLines)
				{
					replacementLines.add( new TextLine( t ) );
				}
				
				lines.remove( index );
				lines.addAll( index, replacementLines );
				
				PresentationStateListenerList.onPresentationStateChanged( listeners, this );
			}
			
			private void lineJoin(TextLine line, String lineText)
			{
				int index = lines.indexOf( line );
				if ( index < lines.size() - 1 )
				{
					// Join with next line
					TextLine next = lines.get( index + 1 );
					String joinedText = lineText + next.text;
					lines.remove( index );
					lines.set( index, new TextLine( joinedText ) );
				}
				else
				{
					// Deleted the newline at the end of the last line, just re-create
					lines.set( index, new TextLine( lineText ) );
				}
				PresentationStateListenerList.onPresentationStateChanged( listeners, this );
			}
		}
	
		
		
		private LSBorder element;
		private TextAreaBox box;
		private LiveInterface value;
		private TextToPresFn textToPres;
		private TextAreaListener listener;
		
		
		
		protected TextAreaControl(PresentationContext ctx, StyleValues style, LSBorder element, TextToPresFn textToPres, TextAreaListener listener, LiveInterface value)
		{
			super( ctx, style );
			
			this.value = value;
			this.value.addListener( this );
			
			this.element = element;
			this.textToPres = textToPres;
			this.listener = listener;
		
			element.setValueFunction( new ValueFn() );
			
			box = new TextAreaBox();
			
			element.setChild( Pres.coerce( box ).present( ctx, style ).layoutWrap( style.get( Primitive.hAlign, HAlignment.class ), style.get( Primitive.vAlign, VAlignment.class ) ) );
			
			this.element.addElementInteractor( new TextAreaInteractor() );
			
			queueRebuild();
		}
		
	
		@Override
		public LSElement getElement()
		{
			return element;
		}
		
		
		public String getDisplayedText()
		{
			return box.getText();
		}
		
		public void setDisplayedText(String text)
		{
			box.setText( text );
		}
		
		
		
		public void grabCaret()
		{
			element.grabCaret();
		}
		
		public void ungrabCaret()
		{
			element.ungrabCaret();
		}
	
		
		public void accept()
		{
			ungrabCaret();
			if ( listener != null )
			{
				listener.onAccept( this, getDisplayedText() );
			}
		}

		
		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			queueRebuild();
		}
		
		
		private void queueRebuild()
		{
			Runnable event = new Runnable()
			{
				@Override
				public void run()
				{
					String text = (String)value.getValue();
					// Only alter the contents if they differ from the desired value
					// Prevents an infinite loop
					if ( !text.equals( getDisplayedText() ) )
					{
						box.setText( text );
					}
				}
			};
			element.queueImmediateEvent( event );
		}
	}
	
	
	private static class CommitListener extends TextAreaListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onAccept(TextAreaControl textArea, String text)
		{
			value.setLiteralValue( text );
		}
	}
	
	private static class ChangeListener extends TextAreaListener
	{
		private LiveValue value;
		
		public ChangeListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onTextChanged(TextAreaControl textArea)
		{
			String text = textArea.getDisplayedText();
			if ( !text.equals( value.getStaticValue() ) )
			{
				value.setLiteralValue( text );
			}
		}
	}
	
	
	private LiveSource valueSource;
	private TextAreaListener listener;
	private TextToPresFn textToPres;
	
	
	private TextArea(LiveSource valueSource, TextAreaListener listener, TextToPresFn textToPres)
	{
		this.valueSource = valueSource;
		this.listener = listener;
		this.textToPres = textToPres;
	}

	public TextArea(String initialText, TextAreaListener listener)
	{
		this( new LiveSourceValue( initialText ), listener, null );
	}
	
	public TextArea(LiveInterface value, TextAreaListener listener)
	{
		this( new LiveSourceRef( value ), listener, null );
	}
	
	public TextArea(LiveValue value)
	{
		this( new LiveSourceRef( value ), new CommitListener( value ), null );
	}
	
	
	public TextArea withTextToPresFunction(TextToPresFn textToPres)
	{
		return new TextArea( valueSource, listener, textToPres );
	}
	


	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		ctx = new PresentationContext( ctx.getFragment(), textAreaPerspective, ctx.getInheritedState() );
		style = style.withAttr( Primitive.editable, true );
		StyleSheet textAreaStyleSheet = style.get( Controls.textAreaAttrs, StyleSheet.class );
		
		StyleValues textAreaStyle = style.withAttrs( textAreaStyleSheet );
		
		LiveInterface value = valueSource.getLive();

		Pres elementPres = new Border( new Blank() );
		LSBorder element = (LSBorder)elementPres.present( ctx, textAreaStyle );
		
		return new TextAreaControl( ctx, style.withAttr( Primitive.hAlign, HAlignment.PACK ), element, textToPres, listener, value );
	}
	
	
	public static TextArea textAreaCommitOnChange(LiveValue value)
	{
		return new TextArea( new LiveSourceRef( value ), new ChangeListener( value ), null );
	}
	
	public static TextArea textAreaCommitOnChange(LiveValue value, TextToPresFn textToPres)
	{
		return new TextArea( new LiveSourceRef( value ), new ChangeListener( value ), textToPres );
	}
}
