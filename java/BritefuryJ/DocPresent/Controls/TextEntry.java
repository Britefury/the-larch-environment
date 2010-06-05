//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.DataTransfer;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Selection.Selection;

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
	
	
	private class TextEntryEditHandler implements EditHandler
	{
		public void deleteSelection(Selection selection)
		{
			if ( !selection.isEmpty() )
			{
				textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
			}
		}

		public void replaceSelectionWithText(Selection selection, String replacement)
		{
			if ( !selection.isEmpty() )
			{
				textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
			}
			Caret caret = textElement.getRootElement().getCaret();
			textElement.insertText( caret.getMarker(), replacement );
		}



		public int getExportActions(Selection selection)
		{
			return COPY_OR_MOVE;
		}

		public Transferable createExportTransferable(Selection selection)
		{
			if ( !selection.isEmpty() )
			{
				String selectedText = textElement.getTextRepresentationBetweenMarkers( selection.getStartMarker(), selection.getEndMarker() );
				return new StringSelection( selectedText );
			}
			else
			{
				return null;
			}
		}

		public void exportDone(Selection selection, Transferable transferable, int action)
		{
			if ( action == MOVE )
			{
				if ( !selection.isEmpty() )
				{
					textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
				}
			}
		}

		
		public boolean canImport(Caret caret, Selection selection, DataTransfer dataTransfer)
		{
			return dataTransfer.isDataFlavorSupported( DataFlavor.stringFlavor );
		}

		public boolean importData(Caret caret, Selection selection, DataTransfer dataTransfer)
		{
			if ( canImport( caret, selection, dataTransfer ) )
			{
				try
				{
					String data = (String)dataTransfer.getTransferData( DataFlavor.stringFlavor );
					
					if ( !selection.isEmpty() )
					{
						textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
					}
					
					textElement.insertText( caret.getMarker(), data );
				}
				catch (UnsupportedFlavorException e)
				{
					return false;
				}
				catch (IOException e)
				{
					return false;
				}
			}
			
			return false;
		}
	}
	
	
	
	private DPBorder outerElement;
	private DPText textElement;
	private TextEntryListener listener;
	private String originalText;


	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, TextEntryListener listener)
	{
		this.outerElement = outerElement;
		this.textElement = textElement;
		this.listener = listener;
		this.textElement.addInteractor( new TextEntryInteractor() );
		originalText = textElement.getText();
		frame.setEditHandler( new TextEntryEditHandler() );
	}
	
	protected TextEntry(DPBorder outerElement, DPRegion frame, DPText textElement, PyObject acceptListener, PyObject cancelListener)
	{
		this( outerElement, frame, textElement, new PyTextEntryListener( acceptListener, cancelListener ) );
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
		ungrabCaret();
		listener.onAccept( this, getText() );
	}

	public void cancel()
	{
		ungrabCaret();
		listener.onCancel( this, originalText );
	}
}
