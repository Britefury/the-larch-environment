//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import java.util.IdentityHashMap;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.TextClipboardHandler;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Util.UnaryFn;

public class EditableTextCell
{
	public static Pres textCellWithCachedListener(String text, UnaryFn textToValue)
	{
		TreeEventListener listener = cachedTreeEventListenerFor( textToValue );
		Pres textPres = new Text( text );
		textPres = textPres.withTreeEventListener( listener );
		return new Region( textPres, clipboardHandler );
	}


	public static Pres textCell(String text, UnaryFn textToValue)
	{
		TreeEventListener listener = treeEventListenerFor( textToValue );
		Pres textPres = new Text( text );
		textPres = textPres.withTreeEventListener( listener );
		return new Region( textPres, clipboardHandler );
	}


	private static final TextClipboardHandler clipboardHandler = new TextClipboardHandler()
	{
		@Override
		protected void deleteText(TextSelection selection, Caret caret)
		{
			DPText textElement = (DPText)selection.getStartMarker().getElement();
			textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
		}

		@Override
		protected void insertText(Marker marker, String text)
		{
			DPText textElement = (DPText)marker.getElement();
			textElement.insertText( marker, text );
		}
		
		@Override
		protected void replaceText(TextSelection selection, Caret caret, String replacement)
		{
			DPText textElement = (DPText)selection.getStartMarker().getElement();
			textElement.replaceText( selection.getStartMarker(), selection.getEndMarker(), replacement );
		}
		
		@Override
		protected String getText(TextSelection selection)
		{
			DPText textElement = (DPText)selection.getStartMarker().getElement();
			return textElement.getTextRepresentationBetweenMarkers( selection.getStartMarker(), selection.getEndMarker() );
		}
	};
	
	
	
	private static final IdentityHashMap<UnaryFn, TreeEventListener> textCellTreeEventListeners = new IdentityHashMap<UnaryFn, TreeEventListener>();
	
	private static TreeEventListener cachedTreeEventListenerFor(final UnaryFn textToValue)
	{
		TreeEventListener listener = textCellTreeEventListeners.get( textToValue );
		
		if ( listener == null )
		{
			listener = treeEventListenerFor( textToValue );
			textCellTreeEventListeners.put( textToValue, listener );
		}
		
		return listener;
	}
	
	
	private static TreeEventListener treeEventListenerFor(final UnaryFn textToValue)
	{
		TreeEventListener listener = new TreeEventListener()
		{
			public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEvent )
				{
					String textValue = element.getTextRepresentation();
					Object value = textToValue.invoke( textValue );
					if ( value != null )
					{
						return CellEditPerspective.notifySetCellValue( element, value );
					}
				}
				return false;
			}
		};
		
		return listener;
	}
}
