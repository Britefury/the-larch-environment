//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.ClipboardFilter.ClipboardCopier;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.SequentialRichStringVisitor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringBuilder;

public class SequentialRichStringController extends SequentialController
{
	public SequentialRichStringController(String editorName)
	{
		super( editorName );
	}
	
	
	//
	//
	// OVERRIDE THESE AS NECESSARY
	//
	//
	
	public RichString joinRichStringsForInsertion(FragmentView subtreeRootFragment, RichString before, RichString insertion, RichString after)
	{
		RichStringBuilder builder = new RichStringBuilder();
		builder.extend( before );
		builder.extend( insertion );
		builder.extend( after );
		return builder.richString();
	}
	
	public RichString joinRichStringsForDeletion(FragmentView subtreeRootFragment, RichString before, RichString after)
	{
		RichStringBuilder builder = new RichStringBuilder();
		builder.extend( before );
		builder.extend( after );
		return builder.richString();
	}
	
	
	
	// Override the following methods in SequentialEditor to support RichString objects

	public Object getSequentialContentInSelection(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, TextSelection selection)
	{
		SequentialRichStringVisitor visitor = new SequentialRichStringVisitor();
		RichString richStr = visitor.getRichStringInTextSelection( selection );
		// Copy the selected content - otherwise altering the original data *after* the copy operation will alter the contents of the cut buffer.
		return ClipboardCopier.instance.copy( richStr );
	}

	public Object spliceForInsertion(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, Marker prefixEnd, Marker suffixStart, Object insertedContent)
	{
		// Get the item strings for the root element content, before and after the selected region
		SequentialRichStringVisitor visitor = new SequentialRichStringVisitor();
		RichString before = visitor.getRichStringFromStartToMarker( subtreeRootFragmentElement, prefixEnd );
		RichString after = visitor.getRichStringFromMarkerToEnd( subtreeRootFragmentElement, suffixStart );
		
		// Copy the inserted content - the same content can be pasted multiple times - the copies *must* be distinct, else modifying
		// one pasted copy will alter all the others
		RichString insertedRichStr = (RichString)ClipboardCopier.instance.copy( (RichString)insertedContent );
		
		// Join
		return joinRichStringsForInsertion( subtreeRootFragment, before, insertedRichStr, after );
	}

	public Object spliceForDeletion(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, Marker selectionStart, Marker selectionEnd)
	{
		// Get the item strings for the root element content, before and after the selected region
		SequentialRichStringVisitor visitor = new SequentialRichStringVisitor();
		RichString before = visitor.getRichStringFromStartToMarker( subtreeRootFragmentElement, selectionStart );
		RichString after = visitor.getRichStringFromMarkerToEnd( subtreeRootFragmentElement, selectionEnd );
		
		// Join
		return joinRichStringsForDeletion( subtreeRootFragment, before, after );
	}


	
	protected Object textToSequentialForImport(String text)
	{
		return new RichString( text );
	}
	
	protected boolean canConvertSequentialToTextForExport(Object sequential)
	{
		RichString richStr = (RichString)sequential;
		return richStr.isTextual();
	}

	protected String sequentialToTextForExport(Object sequential)
	{
		RichString richStr = (RichString)sequential;
		if ( richStr.isTextual() )
		{
			return richStr.textualValue();
		}
		else
		{
			return null;
		}
	}
}
