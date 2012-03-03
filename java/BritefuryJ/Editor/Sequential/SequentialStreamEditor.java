//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.Selection.TextSelection;
import BritefuryJ.LSpace.StreamValue.SequentialStreamValueVisitor;
import BritefuryJ.LSpace.StreamValue.StreamValue;
import BritefuryJ.LSpace.StreamValue.StreamValueBuilder;

public abstract class SequentialStreamEditor extends SequentialEditor
{
	//
	//
	// OVERRIDE THESE AS NECESSARY
	//
	//
	
	public abstract Object copyStructuralValue(Object x);

	
	
	public StreamValue joinStreamsForInsertion(FragmentView subtreeRootFragment, StreamValue before, StreamValue insertion, StreamValue after)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		builder.extend( before );
		builder.extend( insertion );
		builder.extend( after );
		return builder.stream();
	}
	
	public StreamValue joinStreamsForDeletion(FragmentView subtreeRootFragment, StreamValue before, StreamValue after)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		builder.extend( before );
		builder.extend( after );
		return builder.stream();
	}
	
	
	
	private StreamValue copyStream(StreamValue stream)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		for (StreamValue.Item item: stream.getItems())
		{
			if ( item instanceof StreamValue.StructuralItem )
			{
				StreamValue.StructuralItem structuralItem = (StreamValue.StructuralItem)item;
				builder.appendStructuralValue( copyStructuralValue( structuralItem.getValue() ) );
			}
			else if ( item instanceof StreamValue.TextItem )
			{
				StreamValue.TextItem textItem = (StreamValue.TextItem)item;
				builder.appendTextValue( textItem.getValue() );
			}
		}
		return builder.stream();
	}
	
	
	
	// Override the following methods in SequentialEditor to support StreamValue objects

	public Object getSequentialContentInSelection(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, TextSelection selection)
	{
		SequentialStreamValueVisitor visitor = new SequentialStreamValueVisitor();
		StreamValue stream = visitor.getStreamValueInTextSelection( selection );
		// Copy the selected content - otherwise altering the original data *after* the copy operation will alter the contents of the cut buffer.
		return copyStream( stream );
	}

	public Object spliceForInsertion(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, Marker prefixEnd, Marker suffixStart, Object insertedContent)
	{
		// Get the item streams for the root element content, before and after the selected region
		SequentialStreamValueVisitor visitor = new SequentialStreamValueVisitor();
		StreamValue before = visitor.getStreamValueFromStartToMarker( subtreeRootFragmentElement, prefixEnd );
		StreamValue after = visitor.getStreamValueFromMarkerToEnd( subtreeRootFragmentElement, suffixStart );
		
		// Copy the inserted content - the same content can be pasted multiple times - the copies *must* be distinct, else modifying
		// one pasted copy will alter all the others
		StreamValue insertedStream = copyStream( (StreamValue)insertedContent );
		
		// Join
		return joinStreamsForInsertion( subtreeRootFragment, before, insertedStream, after );
	}

	public Object spliceForDeletion(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, Marker selectionStart, Marker selectionEnd)
	{
		// Get the item streams for the root element content, before and after the selected region
		SequentialStreamValueVisitor visitor = new SequentialStreamValueVisitor();
		StreamValue before = visitor.getStreamValueFromStartToMarker( subtreeRootFragmentElement, selectionStart );
		StreamValue after = visitor.getStreamValueFromMarkerToEnd( subtreeRootFragmentElement, selectionEnd );
		
		// Join
		return joinStreamsForDeletion( subtreeRootFragment, before, after );
	}


	
	protected Object textToSequentialForImport(String text)
	{
		return new StreamValue( text );
	}
	
	protected boolean canConvertSequentialToTextForExport(Object sequential)
	{
		StreamValue stream = (StreamValue)sequential;
		return stream.isTextual();
	}

	protected String sequentialToTextForExport(Object sequential)
	{
		StreamValue stream = (StreamValue)sequential;
		if ( stream.isTextual() )
		{
			return stream.textualValue();
		}
		else
		{
			return null;
		}
	}
}
