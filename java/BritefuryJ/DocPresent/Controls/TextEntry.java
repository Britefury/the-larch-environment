//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Clipboard.TextEditHandler;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;

public class TextEntry extends Control
{
	public static interface TextEntryListener
	{
		public void onAccept(TextEntry textEntry, String text);
		public void onCancel(TextEntry textEntry, String originalText);
	}
	
	
	public static interface TextEntryValidator
	{
		public boolean validateText(TextEntry textEntry, String text);
		public String validationMessage(TextEntry textEntry, String text);
	}
	
	
	private static class PyTextEntryListener implements TextEntryListener
	{
		private PyObject acceptCallable, cancelCallable;
		
		
		public PyTextEntryListener(PyObject acceptCallable, PyObject cancelCallable)
		{
			this.acceptCallable = acceptCallable;
			this.cancelCallable = cancelCallable;
		}
		
		public void onAccept(TextEntry textEntry, String text)
		{
			if ( acceptCallable != null  &&  acceptCallable != Py.None )
			{
				acceptCallable.__call__( Py.java2py( textEntry ), Py.java2py( text ) );
			}
		}
		
		public void onCancel(TextEntry textEntry, String originalText)
		{
			if ( cancelCallable != null  &&  cancelCallable != Py.None )
			{
				cancelCallable.__call__( Py.java2py( textEntry ), Py.java2py( originalText ) );
			}
		}
	}
	
	private static class PyTextEntryValidator implements TextEntryValidator
	{
		private PyObject validationFn, validationMessageFn;
		
		
		public PyTextEntryValidator(PyObject validationFn, PyObject validationMessageFn)
		{
			this.validationFn = validationFn;
			this.validationMessageFn = validationMessageFn;
		}
		
		public boolean validateText(TextEntry textEntry, String text)
		{
			if ( validationFn != null  &&  validationFn != Py.None )
			{
				return Py.py2boolean( validationFn.__call__( Py.java2py( textEntry ), Py.java2py( text ) ) );
			}
			else
			{
				return true;
			}
		}

		public String validationMessage(TextEntry textEntry, String text)
		{
			if ( validationMessageFn != null  &&  validationMessageFn != Py.None )
			{
				return Py.tojava( validationMessageFn.__call__( Py.java2py( textEntry ), Py.java2py( text ) ), String.class );
			}
			else
			{
				return null;
			}
		}
	}
	
	
	private static class RegexTextEntryValidator implements TextEntryValidator
	{
		private Pattern pattern;
		private String failMessage;
		
		private RegexTextEntryValidator(Pattern pattern, String failMessage)
		{
			this.pattern = pattern;
			this.failMessage = failMessage;
		}
		
		
		@Override
		public boolean validateText(TextEntry textEntry, String text)
		{
			return pattern.matcher( text ).matches();
		}

		@Override
		public String validationMessage(TextEntry textEntry, String text)
		{
			return failMessage;
		}
	}
	

	private class TextEntryInteractor extends ElementInteractor
	{
		private TextEntryInteractor()
		{
		}
		
		
		public boolean onKeyPress(DPElement element, KeyEvent event)
		{
			return event.getKeyCode() == KeyEvent.VK_ENTER  ||  event.getKeyCode() == KeyEvent.VK_ESCAPE;
		}

		public boolean onKeyRelease(DPElement element, KeyEvent event)
		{
			if ( event.getKeyCode() == KeyEvent.VK_ENTER )
			{
				accept();
				return true;
			}
			else if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
			{
				cancel();
				return true;
			}
			return false;
		}

		public boolean onKeyTyped(DPElement element, KeyEvent event)
		{
			return event.getKeyChar() == KeyEvent.VK_ENTER  ||  event.getKeyChar() == KeyEvent.VK_ESCAPE;
		}
	}
	
	
	private class TextEntryEditHandler extends TextEditHandler
	{
		protected void deleteText(Selection selection)
		{
			textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
		}

		protected void insertText(Marker marker, String text)
		{
			textElement.insertText( marker, text );
		}
		
		protected void replaceText(Selection selection, String replacement)
		{
			textElement.replaceText(selection.getStartMarker(), selection.getEndMarker(), replacement );
		}
		
		protected String getText(Selection selection)
		{
			return textElement.getTextRepresentationBetweenMarkers( selection.getStartMarker(), selection.getEndMarker() );
		}
	}
	
	
	
	private DPBorder outerElement;
	private DPText textElement;
	private TextEntryListener listener;
	private TextEntryValidator validator;
	private ControlsStyleSheet styleSheet;
	private String originalText;


	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, TextEntryListener listener, ControlsStyleSheet styleSheet)
	{
		this( outerElement, frame, textElement, listener, (TextEntryValidator)null, styleSheet );
	}
	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, PyObject acceptListener, PyObject cancelListener, ControlsStyleSheet styleSheet)
	{
		this( outerElement, frame, textElement, new PyTextEntryListener( acceptListener, cancelListener ), styleSheet );
	}
	
	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, TextEntryListener listener, TextEntryValidator validator, ControlsStyleSheet styleSheet)
	{
		this.outerElement = outerElement;
		this.textElement = textElement;
		this.listener = listener;
		this.validator = validator;
		this.styleSheet = styleSheet;

		this.textElement.addInteractor( new TextEntryInteractor() );
		originalText = textElement.getText();
		frame.setEditHandler( new TextEntryEditHandler() );
	}
	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, PyObject acceptListener, PyObject cancelListener, PyObject validationFn, PyObject validationMessageFn,
			ControlsStyleSheet styleSheet)
	{
		this( outerElement, frame, textElement, new PyTextEntryListener( acceptListener, cancelListener ), new PyTextEntryValidator( validationFn, validationMessageFn ), styleSheet );
	}
	
	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, TextEntryListener listener, Pattern validationRegex, String validationFailMessage,
			ControlsStyleSheet styleSheet)
	{
		this( outerElement, frame, textElement, listener, new RegexTextEntryValidator( validationRegex, validationFailMessage ), styleSheet );
	}
	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, PyObject acceptListener, PyObject cancelListener, Pattern validationRegex, String validationFailMessage,
			ControlsStyleSheet styleSheet)
	{
		this( outerElement, frame, textElement, new PyTextEntryListener( acceptListener, cancelListener ), validationRegex, validationFailMessage, styleSheet );
	}
	
	
	public DPElement getElement()
	{
		return outerElement;
	}
	

	public String getText()
	{
		return textElement.getText();
	}
	
	public String getOriginalText()
	{
		return originalText;
	}
	
	public void setText(String text)
	{
		textElement.setText( text );
	}
	
	
	public void selectAll()
	{
		PresentationComponent.RootElement root = textElement.getRootElement();
		if ( root != null )
		{
			Selection selection = root.getSelection();
			selection.setSelection( textElement.markerAtStart(), textElement.markerAtEnd() );
		}
		else
		{
			throw new RuntimeException( "Could not get root element - text element is not realised" );
		}
	}
	
	
	public void grabCaret()
	{
		textElement.grabCaret();
	}
	
	public void ungrabCaret()
	{
		textElement.ungrabCaret();
	}

	
	public void accept()
	{
		if ( validator != null  &&  !validator.validateText( this, getText() ) )
		{
			String failMessage = validator.validationMessage( this, getText() );
			if ( failMessage != null )
			{
				TimedPopup tooltip = styleSheet.tooltip( failMessage, 5.0 );
				tooltip.popupBelow( outerElement );
			}
			return;
		}
		ungrabCaret();
		listener.onAccept( this, getText() );
	}

	public void cancel()
	{
		ungrabCaret();
		listener.onCancel( this, originalText );
	}
}
