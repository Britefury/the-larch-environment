//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.event.KeyEvent;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementInteractor;

public class TextEntry extends Control
{
	public static interface TextEntryListener
	{
		public void onAccept(TextEntry textEntry, String text);
		public void onCancel(TextEntry textEntry, String originalText);
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
	

	private class TextEntryInteractor extends ElementInteractor
	{
		private TextEntryInteractor()
		{
		}
		
		
		public boolean onKeyPress(DPWidget element, KeyEvent event)
		{
			return event.getKeyCode() == KeyEvent.VK_ENTER  ||  event.getKeyCode() == KeyEvent.VK_ESCAPE;
		}

		public boolean onKeyRelease(DPWidget element, KeyEvent event)
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

		public boolean onKeyTyped(DPWidget element, KeyEvent event)
		{
			return event.getKeyChar() == KeyEvent.VK_ENTER  ||  event.getKeyChar() == KeyEvent.VK_ESCAPE;
		}
	}
	
	
	
	private DPBorder outerElement;
	private DPText textElement;
	private TextEntryListener listener;
	private String originalText;


	
	protected TextEntry(DPBorder outerElement, DPText textElement, TextEntryListener listener)
	{
		this.outerElement = outerElement;
		this.textElement = textElement;
		this.listener = listener;
		this.textElement.addInteractor( new TextEntryInteractor() );
		originalText = textElement.getText();
	}
	
	protected TextEntry(DPBorder outerElement, DPText textElement, PyObject acceptListener, PyObject cancelListener)
	{
		this( outerElement, textElement, new PyTextEntryListener( acceptListener, cancelListener ) );
	}
	
	
	public DPWidget getElement()
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
		ungrabCaret();
		listener.onAccept( this, getText() );
	}

	public void cancel()
	{
		ungrabCaret();
		listener.onCancel( this, originalText );
	}
}
