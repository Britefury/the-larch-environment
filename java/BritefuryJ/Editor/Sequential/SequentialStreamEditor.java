//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.DocPresent.StreamValue.SequentialStreamValueVisitor;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.IncrementalView.FragmentView;

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
	
	
	
	// Override the following methods in SequentialEditor to support StreamValue objects

	public Object getSequentialContentInSelection(TextSelection selection)
	{
		SequentialStreamValueVisitor visitor = new SequentialStreamValueVisitor();
		StreamValue stream = visitor.getStreamValueInTextSelection( selection );
		
		StreamValueBuilder builder = new StreamValueBuilder();
		for (StreamValue.Item item: stream.getItems())
		{
			if ( item instanceof StreamValue.StructuralItem )
			{
				StreamValue.StructuralItem structuralItem = (StreamValue.StructuralItem)item;
				builder.appendStructuralValue( copyStructuralValue( structuralItem.getStructuralValue() ) );
			}
			else if ( item instanceof StreamValue.TextItem )
			{
				StreamValue.TextItem textItem = (StreamValue.TextItem)item;
				builder.appendTextValue( textItem.getTextValue() );
			}
		}
		
		return builder.stream();
	}

	public Object spliceForInsertion(FragmentView subtreeRootFragment, DPElement subtreeRootFragmentElement, Marker prefixEnd, Marker suffixStart, Object insertedContent)
	{
		// Get the item streams for the root element content, before and after the selected region
		SequentialStreamValueVisitor visitor = new SequentialStreamValueVisitor();
		StreamValue before = visitor.getStreamValueFromStartToMarker( subtreeRootFragmentElement, prefixEnd );
		StreamValue after = visitor.getStreamValueFromMarkerToEnd( subtreeRootFragmentElement, suffixStart );
		
		// Join
		return joinStreamsForInsertion( subtreeRootFragment, before, (StreamValue)insertedContent, after );
	}

	public Object spliceForDeletion(FragmentView subtreeRootFragment, DPElement subtreeRootFragmentElement, Marker selectionStart, Marker selectionEnd)
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
