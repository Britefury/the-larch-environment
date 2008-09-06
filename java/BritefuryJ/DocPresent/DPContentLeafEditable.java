package BritefuryJ.DocPresent;

import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;

public abstract class DPContentLeafEditable extends DPContentLeaf
{
	DPContentLeafEditable()
	{
		super();
	}
	
	DPContentLeafEditable(ContentLeafStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	
	DPContentLeafEditable(ContentLeafStyleSheet styleSheet, String content)
	{
		super( styleSheet, content );
	}
	
	
	
	public void insertContent(Marker marker, String x)
	{
		int index = marker.getIndex();
		content = content.substring( 0, index ) + x + content.substring( index );
		markerInsert( index, x.length() );
		contentChanged();
		if ( listener != null )
		{
			listener.contentInserted( marker, x );
		}
	}

	public void removeContent(Marker m, int length)
	{
		int index = m.getIndex();
		content = content.substring( 0, index ) + content.substring( index + length );
		markerRemove( index, length );
		contentChanged();
		if ( listener != null )
		{
			listener.contentRemoved( m, length );
		}
	}
	
	public void replaceContent(Marker m, int length, String x)
	{
		int index = m.getIndex();
		content = content.substring( 0, index )  +  x  +  content.substring( index + length );
		
		if ( x.length() > length )
		{
			markerInsert( index + length, x.length() - length );
		}
		else if ( x.length() < length )
		{
			markerRemove( index + x.length(), length - x.length() );
		}
		contentChanged();
		if ( listener != null )
		{
			listener.contentReplaced( m, length, x );
		}
	}
	
	
	

	public boolean isEditable()
	{
		return true;
	}


	
	
	//
	//
	// INPUT EVENT HANDLING
	//
	//
	
	protected boolean handleBackspace(Caret caret)
	{
		if ( isMarkerAtStart( caret.getMarker() ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			if ( left == null )
			{
				return false;
			}
			else
			{
				left.moveMarkerToEnd( caret.getMarker() );
				return true;
			}
		}
		else
		{
			removeContent( markerToLeft( caret.getMarker(), false, true ), 1 );
			return true;
		}
	}
	
	protected boolean handleDelete(Caret caret)
	{
		if ( isMarkerAtEnd( caret.getMarker() ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			if ( right == null )
			{
				return false;
			}
			else
			{
				right.moveMarkerToStart( caret.getMarker() );
				return true;
			}
		}
		else
		{
			removeContent( caret.getMarker(), 1 );
			return true;
		}
	}
	
	protected boolean onKeyPress(Caret caret, KeyEvent event)
	{
		if ( event.getKeyCode() == KeyEvent.VK_BACK_SPACE )
		{
			return handleBackspace( caret );
		}
		else if ( event.getKeyCode() == KeyEvent.VK_DELETE )
		{
			return handleDelete( caret );
		}
		return false;
	}

	protected boolean onKeyRelease(Caret caret, KeyEvent event)
	{
		return false;
	}

	protected boolean onKeyTyped(Caret caret, KeyEvent event)
	{
		if ( event.getKeyChar() != KeyEvent.VK_BACK_SPACE  &&  event.getKeyChar() != KeyEvent.VK_DELETE )
		{
			insertContent( caret.getMarker(), String.valueOf( event.getKeyChar() ) );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	
	protected boolean onButtonDown(PointerButtonEvent event)
	{
		if ( event.getButton() == 1 )
		{
			Caret caret = presentationArea.getCaret();
			int contentPos = getContentPositonForPoint( event.getPointer().getLocalPos() );
			moveMarker( caret.getMarker(), contentPos, Marker.Bias.START );
			return true;
		}
		else
		{
			return false;
		}
	}
}
