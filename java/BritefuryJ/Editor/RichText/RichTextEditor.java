//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.ElementTreeVisitor;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.Editor.RichText.EditorModel.EdBlock;
import BritefuryJ.Editor.RichText.EditorModel.EdInlineEmbed;
import BritefuryJ.Editor.RichText.EditorModel.EdNode;
import BritefuryJ.Editor.RichText.EditorModel.EdParagraph;
import BritefuryJ.Editor.RichText.EditorModel.EdStyleSpan;
import BritefuryJ.Editor.Sequential.EditListener;
import BritefuryJ.Editor.Sequential.EditListener.HandleEditResult;
import BritefuryJ.Editor.Sequential.SelectionEditTreeEvent;
import BritefuryJ.Editor.Sequential.SequentialEditor;
import BritefuryJ.Editor.Sequential.StreamEditListener;
import BritefuryJ.Editor.Sequential.Item.EditableStructuralItem;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.RichText.RichText;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class RichTextEditor extends SequentialEditor
{
	private static abstract class Visitor extends ElementTreeVisitor
	{
		protected ArrayList<Object> prefix = new ArrayList<Object>();
		protected ArrayList<Object> contents = new ArrayList<Object>();
		protected ArrayList<Object> suffix = new ArrayList<Object>();
		protected RichTextEditor editor;
		
		
		public Visitor(RichTextEditor editor)
		{
			this.editor = editor;
		}


		@Override
		protected void preOrderVisitElement(DPElement e, boolean complete)
		{
			if ( e.hasFixedValue() )
			{
				Object model = e.getFixedValue();
				
				Object prefix = editor.modelToPrefixTag( model );
				if ( prefix != null )
				{
					contents.add( prefix );
				}
				
				if ( !complete )
				{
					Object regionStart = editor.modelToRegionStartTag( model );
					Object regionEnd = editor.modelToRegionEndTag( model );
					
					if ( regionStart != null )
					{
						contents.add( regionStart );
					}
					if ( regionEnd != null )
					{
						suffix.add( 0, regionEnd );
					}
				}
			}
		}


		@Override
		protected void inOrderCompletelyVisitElement(DPElement e)
		{
			StreamValue stream = e.getStreamValue();
			for (StreamValue.Item item: stream.getItems())
			{
				if ( item.isStructural() )
				{
					completelyVisitStructralValue( ((StreamValue.StructuralItem)item).getValue() );
				}
				else
				{
					contents.add( ((StreamValue.TextItem)item).getValue() );
				}
			}
		}


		@Override
		protected void postOrderVisitElement(DPElement e, boolean complete)
		{
			if ( e.hasFixedValue() )
			{
				Object model = e.getFixedValue();
				
				if ( !complete )
				{
					Object start = editor.modelToRegionStartTag( model );
					Object end = editor.modelToRegionEndTag( model );
					
					if ( start != null )
					{
						prefix.add( 0, start );
					}
					if ( end != null )
					{
						contents.add( end );
					}
				}

				Object suffix = editor.modelToSuffixTag( model );
				if ( suffix != null )
				{
					contents.add( suffix );
				}
			}
		}


		@Override
		protected void inOrderVisitPartialContentLeafEditable(DPContentLeafEditable e, int startIndex, int endIndex)
		{
			contents.add( e.getTextRepresentation().substring( startIndex, endIndex ) );
		}


		@Override
		protected boolean shouldVisitChildrenOfElement(DPElement e, boolean completeVisit)
		{
			return completeVisit  ?  e.hasFixedValue()  :  true;
		}
		
		
		public List<Object> flattened()
		{
			ArrayList<Object> joined = new ArrayList<Object>( prefix );
			joined.addAll( contents );
			joined.addAll( suffix );
			return Flatten.flattenParagraphs( joined );
		}
		
		
		
		protected abstract void completelyVisitStructralValue(Object value);
	}
	
	
	private static class TagsVisitor extends Visitor
	{
		public TagsVisitor(RichTextEditor editor)
		{
			super( editor );
		}

		@Override
		protected void completelyVisitStructralValue(Object value)
		{
			editor.modelToTags( contents, value );
		}
	}
	
	
	private static class NodeVisitor extends Visitor
	{
		public NodeVisitor(RichTextEditor editor)
		{
			super( editor );
		}

		@Override
		protected void completelyVisitStructralValue(Object value)
		{
			if ( editor.isStyleSpan( value ) )
			{
				editor.modelToTags( contents, value );
			}
			else
			{
				contents.add( editor.modelToEditorModel( value ) );
			}
		}
	}
	
	
	
	

	
	public interface ComputeSpanStylesFn
	{
		public Map<Object, Object> invoke(List<Map<Object, Object>> stylesOfSpans);
	}
	
	public interface ModifyParagraphFn
	{
		public void invoke(Object paragraph);
	}
	
	public interface MakeParagraphFn
	{
		public Object invoke();
	}
	
	public interface MakeInlineEmbedValueFn
	{
		public Object invoke();
	}
	
	
	
	
	private StreamEditListener inlineEmbedEditListener, textEditListener, paraEditListener, paraListItemEditListener, blockEditListener;
	
	
	public RichTextEditor()
	{
		HandleStreamValueFn onInlineEmbedEdit = new HandleStreamValueFn()
		{
			@Override
			public EditListener.HandleEditResult handleValue(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value)
			{
				if ( event instanceof RichTextEditEvents.RichTextRequest )
				{
					RichTextEditEvents.RichTextRequest richTextReq = (RichTextEditEvents.RichTextRequest)event;
					return richTextReq.invokeOnInlineEmbed( RichTextEditor.this, element, sourceElement, fragment, model, value );
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

	
		HandleStreamValueFn onTextEdit = new HandleStreamValueFn()
		{
			@Override
			public EditListener.HandleEditResult handleValue(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value)
			{
				if ( event instanceof TextEditEvent )
				{
					boolean handled = false;
					if ( sourceElement.getFragmentContext() == fragment )
					{
						handled = setTextContentsFromStream( fragment.getView().getLog(), model, value );
					}
					return handled  ?  EditListener.HandleEditResult.HANDLED  :  EditListener.HandleEditResult.PASS_TO_PARENT;
				}
				else if ( event instanceof RichTextEditEvents.RichTextRequest )
				{
					RichTextEditEvents.RichTextRequest richTextReq = (RichTextEditEvents.RichTextRequest)event;
					return richTextReq.invokeOnTextSpan( RichTextEditor.this, element, sourceElement, fragment, model, value );
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

		
		HandleStreamValueFn onParagraphEdit = new HandleStreamValueFn()
		{
			@Override
			public EditListener.HandleEditResult handleValue(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value)
			{
				if ( event instanceof TextEditEvent )
				{
					boolean handled = false;
					if ( sourceElement.getFragmentContext() == fragment )
					{
						handled = setParagraphTextContentsFromStream( fragment.getView().getLog(), model, value );
					}
					return handled  ?  EditListener.HandleEditResult.HANDLED  :  EditListener.HandleEditResult.PASS_TO_PARENT;
				}
				else if ( event instanceof SelectionEditTreeEvent )
				{
					boolean handled = setParagraphContentsFromBlockStream( fragment.getView().getLog(), model, value );
					return handled  ?  EditListener.HandleEditResult.HANDLED  :  EditListener.HandleEditResult.PASS_TO_PARENT;
				}
				else if ( event instanceof RichTextEditEvents.RichTextRequest )
				{
					RichTextEditEvents.RichTextRequest richTextReq = (RichTextEditEvents.RichTextRequest)event;
					return richTextReq.invokeOnParagraph( RichTextEditor.this, element, sourceElement, fragment, model, value );
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

		
		HandleStreamValueFn onParagraphListItemEdit = new HandleStreamValueFn()
		{
			@Override
			public EditListener.HandleEditResult handleValue(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value)
			{
				if ( event instanceof RichTextEditEvents.RichTextRequest )
				{
					RichTextEditEvents.RichTextRequest richTextReq = (RichTextEditEvents.RichTextRequest)event;
					return richTextReq.invokeOnParagraphListItem( RichTextEditor.this, element, sourceElement, fragment, model, value );
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

		
//		HandleStreamValueFn onParagraphEmbedEdit = new HandleStreamValueFn()
//		{
//			@Override
//			public EditListener.HandleEditResult handleValue(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value)
//			{
//				if ( event instanceof RichTextEditEvents.RichTextRequest )
//				{
//					RichTextEditEvents.RichTextRequest richTextReq = (RichTextEditEvents.RichTextRequest)event;
//					return richTextReq.invokeOnParagraph( RichTextEditor.this, element, sourceElement, fragment, model, value );
//				}
//				return HandleEditResult.NOT_HANDLED;
//			}
//		};

		
		HandleStreamValueFn onBlockEdit = new HandleStreamValueFn()
		{
			@Override
			public EditListener.HandleEditResult handleValue(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value)
			{
				if ( event instanceof TextEditEvent )
				{
					setBlockContentsFromRawStream( fragment.getView().getLog(), model, value );
					return EditListener.HandleEditResult.HANDLED;
				}
				else if ( event instanceof SelectionEditTreeEvent )
				{
					setModelContentsFromStream( model, value );
					return EditListener.HandleEditResult.HANDLED;
				}
				else if ( event instanceof RichTextEditEvents.RichTextRequest )
				{
					RichTextEditEvents.RichTextRequest richTextReq = (RichTextEditEvents.RichTextRequest)event;
					return richTextReq.invokeOnBlock( RichTextEditor.this, element, sourceElement, fragment, model, value );
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

	
		inlineEmbedEditListener = streamEditListener( onInlineEmbedEdit );
		textEditListener = streamEditListener( onTextEdit );
		paraEditListener = streamEditListener( onParagraphEdit );
		paraListItemEditListener = streamEditListener( onParagraphListItemEdit );
		blockEditListener = streamEditListener( onBlockEdit );
	}
	
	
	
	
	
	
	
	
	@Override
	protected boolean isEditEvent(EditEvent event)
	{
		return event instanceof RichTextEditEvents.RichTextRequest;
	}


	
	public Pres editableInlineEmbed(Object model, Object child)
	{
		child = new Proxy( child );
		return new EditableStructuralItem( this, inlineEmbedEditListener, model, child );
	}
	
	public Pres editableText(Object model, Object child)
	{
		return new EditableStructuralItem( this, textEditListener, model, child );
	}
	
	public Pres editableParagraph(Object model, Object child)
	{
		child = editableParaStyle.applyTo( child );
		return new EditableStructuralItem( this, Arrays.asList( new TreeEventListener[] { paraEditListener, paraListItemEditListener } ), model, child );
	}
	
	public Pres editableParagraphEmbed(Object model, Object child)
	{
		return new EditableStructuralItem( this, paraListItemEditListener, model, child );
	}
	
	public Pres editableBlock(Object model, Object child)
	{
		return new EditableStructuralItem( this, blockEditListener, model, child );
	}
	
	
	public void insertParagraphAtCaret(Caret caret, MakeParagraphFn makeParagraph)
	{
		if ( caret.isValid()  &&  caret.isEditable() )
		{
			caret.getElement().postTreeEvent( new RichTextEditEvents.InsertParagraphRequest( makeParagraph ) );
		}
	}
	
	public void deleteParagraphContainingElement(DPElement element)
	{
		element.postTreeEvent( new RichTextEditEvents.DeleteParagraphRequest() );
	}
	
	
	
	public void insertInlineEmbedAtMarker(Marker marker, MakeInlineEmbedValueFn makeInlineEmbedValue)
	{
		if ( marker.isValid() )
		{
			marker.getElement().postTreeEvent( new RichTextEditEvents.InsertInlineEmbedRequest( marker, makeInlineEmbedValue ) );
		}
	}
	
	public void deleteInlineEmbedContainingElement(DPElement element)
	{
		element.postTreeEvent( new RichTextEditEvents.DeleteInlineEmbedRequest() );
	}
	
	
	public void applyStyleToSelection(TextSelection selection, ComputeSpanStylesFn computeSpanStyles)
	{
		selection.getCommonRoot().postTreeEvent( new RichTextEditEvents.StyleRequest( computeSpanStyles ) );
	}
	
	public void modifyParagraphAtMarker(Marker marker, ModifyParagraphFn modifyParagraph)
	{
		marker.getElement().postTreeEvent( new RichTextEditEvents.ParagraphStyleRequest( modifyParagraph ) );
	}
	
	






	protected boolean setTextContentsFromStream(Log log, Object model, StreamValue value)
	{
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setTextContentsFromStream" ).vItem( "stream", value ) );
		}
		setModelContentsFromStream( model, value );
		return !value.contains( "\n" );
	}

	protected boolean setParagraphTextContentsFromStream(Log log, Object model, StreamValue value)
	{
		if ( value.endsWith( "\n" ) )
		{
			StreamValue toLast = value.subStream( 0, value.length() - 1 );
			setModelContentsFromStream( model, toLast );
			if ( log.isRecording() )
			{
				log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setParagraphTextContentsFromStream - with trailing newline" ).vItem( "stream", value ) );
			}
			return !toLast.contains( "\n" );
		}
		else
		{
			setModelContentsFromStream( model, value );
			EdNode e = modelToEditorModel( model );
			((EdParagraph)e).suppressNewline();
			if ( log.isRecording() )
			{
				log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setParagraphTextContentsFromStream - no trailing newline" ).vItem( "stream", value ) );
			}
		}
		return false;
	}


	protected boolean setParagraphContentsFromBlockStream(Log log, Object paragraph, StreamValue stream)
	{
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setParagraphContentsFromBlockStream" ).vItem( "stream", stream ) );
		}
		List<Object> items = stream.getItemValues();
		if ( items.size() == 1 )
		{
			EdParagraph block = (EdParagraph)items.get( 0 );
			List<? extends Object> contents = block.getContents();
			setModelContentsFromStream( paragraph, new StreamValueBuilder( contents ).stream() );
			return true;
		}
		else
		{
			return false;
		}
	}


	protected void setBlockContentsFromRawStream(Log log, Object model, StreamValue value)
	{
		ArrayList<Object> tags = new ArrayList<Object>();
		modelToTags( tags, model );
		
		List<Object> flattened = Flatten.flattenParagraphs( tags );
		List<Object> paras = Merge.mergeParagraphs( flattened );
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setBlockContentsFromRawStream" ).vItem( "tags", tags ).vItem( "paras", paras ) );
		}
		setModelContentsFromStream( model, new StreamValueBuilder( paras ).stream() );
	}



	
	protected abstract void setModelContentsFromStream(Object model, StreamValue value);
	protected abstract EdNode modelToEditorModel(Object model);
	
	protected abstract void insertParagraphIntoBlockAfter(Object block, Object para, Object paragraphBefore);
	protected abstract void deleteParagraphFromBlock(Object block, Object paragraph);
	protected abstract void removeInlineEmbed(Object textSpan, Object embed);

	
	public abstract Object deepCopyInlineEmbedValue(Object value);


	public Object deepCopyParagraphEmbedValue(Object value)
	{
		return deepCopyInlineEmbedValue( value );
	}


	
	
	protected Object modelToPrefixTag(Object model)
	{
		return modelToEditorModel( model ).prefixTag();
	}
	
	protected Object modelToSuffixTag(Object model)
	{
		return modelToEditorModel( model ).suffixTag();
	}
	
	protected Object modelToRegionStartTag(Object model)
	{
		return modelToEditorModel( model ).regionStartTag();
	}
	
	protected Object modelToRegionEndTag(Object model)
	{
		return modelToEditorModel( model ).regionEndTag();
	}
	
	protected void modelToTags(List<Object> tags, Object model)
	{
		modelToEditorModel( model ).buildTagList( tags );
	}
	
	
	
	protected boolean isStyleSpan(Object model)
	{
		return modelToEditorModel( model ) instanceof EdStyleSpan;
	}
	
	protected boolean isParagraph(Object model)
	{
		return modelToEditorModel( model ) instanceof EdParagraph;
	}
	
	protected boolean isBlock(Object model)
	{
		return modelToEditorModel( model ) instanceof EdBlock;
	}
	
	
	
	
	

	@Override
	protected boolean isClipboardEditLevelFragmentView(FragmentView fragment)
	{
		Object model = fragment.getModel();
		return isParagraph( model )  ||  isBlock( model );
	}
	
	
	
	

	private static boolean isEditorModelListTextual(Object x)
	{
		if ( x instanceof List )
		{
			@SuppressWarnings("unchecked")
			List<? extends Object> ls = (List<? extends Object>)x;
			for (Object a: ls)
			{
				if ( !isEditorModelListTextual( a ) )
				{
					return false;
				}
			}
			return true;
		}
		else if ( x instanceof EdNode )
		{
			return ( (EdNode)x ).isTextual();
		}
		else
		{
			return false;
		}
	}
	
	private static void buildEditorModelTextualValue(StringBuilder builder, Object x)
	{
		if ( x instanceof List )
		{
			@SuppressWarnings("unchecked")
			List<? extends Object> ls = (List<? extends Object>)x;
			for (Object a: ls)
			{
				buildEditorModelTextualValue( builder, a );
			}
		}
		else if ( x instanceof EdNode )
		{
			( (EdNode)x ).buildTextualValue( builder );
		}
		else
		{
			throw new RuntimeException( "Not a list or an #EdNode" );
		}
	}
	
	private static String editorModelTextualValue(Object x)
	{
		StringBuilder builder = new StringBuilder();
		buildEditorModelTextualValue( builder, x );
		return builder.toString();
	}
	
	

	
	@Override
	protected Object textToSequentialForImport(String text)
	{
		EdStyleSpan span = new EdStyleSpan( Arrays.asList( new Object[] { text } ), new HashMap<Object, Object>() );
		return Arrays.asList( new EdNode[] { span } );
	}

	@Override
	protected boolean canConvertSequentialToTextForExport(Object sequential)
	{
		return isEditorModelListTextual( sequential );
	}

	@Override
	protected String sequentialToTextForExport(Object sequential)
	{
		return editorModelTextualValue( sequential );
	}




	protected void insertInlineEmbed(Log log, DPElement element, Object paragraph, Marker marker, EdInlineEmbed embed)
	{
		Visitor v1 = new TagsVisitor( this );
		v1.visitFromStartOfRootToMarker( marker, element );
		Visitor v2 = new TagsVisitor( this );
		v2.visitFromMarkerToEndOfRoot( marker, element );
		ArrayList<Object> splicedFlattened = new ArrayList<Object>();
		splicedFlattened.addAll( v1.flattened() );
		splicedFlattened.add( embed );
		splicedFlattened.addAll( v2.flattened() );
		ArrayList<Object> splicedMerged = Merge.mergeParagraphs( splicedFlattened );
		setParagraphContentsFromBlockStream( log, paragraph, new StreamValueBuilder( splicedMerged ).stream() );
	}




	@Override
	public Object getSequentialContentInSelection(FragmentView editFragment, DPElement editFragmentElement, TextSelection selection)
	{
		Visitor v = new NodeVisitor( this );
		return getFlattenedContentInSelection( v, editFragment, selection );
	}


	private Object getFlattenedContentInSelection(Visitor v, FragmentView editFragment, TextSelection selection)
	{
		v.visitTextSelection( selection );
		
		// Walk the tree from the common root of the selection, up to the 'edit fragment',
		// wrapping the content in start/end tags as we go
		DPElement commonRoot = selection.getCommonRoot();
		FragmentView rootFragment = (FragmentView)commonRoot.getFragmentContext();
		
		while ( rootFragment != editFragment )
		{
			Object m = rootFragment.getModel();
			Object regionStart = modelToRegionStartTag( m );
			Object regionEnd = modelToRegionEndTag( m );
			if ( regionStart != null )
			{
				v.prefix.add( regionStart );
			}
			if ( regionEnd != null )
			{
				v.suffix.add( regionEnd );
			}
			
			rootFragment = (FragmentView)rootFragment.getParent();
		}
		
		return v.flattened();
	}


	
	public List<? extends Object> deepCopyFlattened(List<? extends Object> flattened)
	{
		ArrayList<Object> copy = new ArrayList<Object>();
		copy.ensureCapacity( flattened.size() );
		for (Object x: flattened)
		{
			if ( x instanceof EdNode )
			{
				copy.add( ( (EdNode)x ).deepCopy( this ) );
			}
			else
			{
				copy.add( x );
			}
		}
		return copy;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public Object spliceForInsertion(FragmentView subtreeRootFragment, DPElement subtreeRootFragmentElement, Marker prefixEnd, Marker suffixStart, Object insertedContent)
	{
		Visitor v1 = new NodeVisitor( this );
		v1.visitFromStartOfRootToMarker( prefixEnd, subtreeRootFragmentElement );
		Visitor v2 = new NodeVisitor( this );
		v2.visitFromMarkerToEndOfRoot( suffixStart, subtreeRootFragmentElement );
		ArrayList<Object> splicedFlattened = new ArrayList<Object>();
		splicedFlattened.addAll( v1.flattened() );
		splicedFlattened.addAll( deepCopyFlattened( (List<? extends Object>)insertedContent ) );
		splicedFlattened.addAll( v2.flattened() );
		ArrayList<Object> splicedMerged = Merge.mergeParagraphs( splicedFlattened );
		return new StreamValueBuilder( splicedMerged ).stream();
	}

	@Override
	public Object spliceForDeletion(FragmentView subtreeRootFragment, DPElement subtreeRootFragmentElement, Marker selectionStart, Marker selectionEnd)
	{
		Visitor v1 = new NodeVisitor( this );
		v1.visitFromStartOfRootToMarker( selectionStart, subtreeRootFragmentElement );
		Visitor v2 = new NodeVisitor( this );
		v2.visitFromMarkerToEndOfRoot( selectionEnd, subtreeRootFragmentElement );
		ArrayList<Object> splicedFlattened = new ArrayList<Object>();
		splicedFlattened.addAll( v1.flattened() );
		splicedFlattened.addAll( v2.flattened() );
		ArrayList<Object> splicedMerged = Merge.mergeParagraphs( splicedFlattened );
		return new StreamValueBuilder( splicedMerged ).stream();
	}
	

	
	public StreamValue streamWithModifiedSelectionStyle(DPElement element, TextSelection selection, ComputeSpanStylesFn computeStylesFn)
	{
		FragmentView editFragment = (FragmentView)element.getFragmentContext();
		DPElement editFragmentElement = editFragment.getFragmentElement();
		
		// Get the content within the selection
		Visitor selectionVisitor = new TagsVisitor( this );
		@SuppressWarnings("unchecked")
		List<? extends Object> selected = (List<? extends Object>)getFlattenedContentInSelection( selectionVisitor, editFragment, selection );
		
		// Extract style dictionaries
		ArrayList< Map<Object, Object> > styles = new ArrayList< Map<Object, Object> >();
		for (Object x: selected)
		{
			if ( x instanceof EdStyleSpan )
			{
				styles.add( ( (EdStyleSpan)x ).getStyleAttrs() );
			}
		}
		
		// Compute the style values to apply
		Map<Object, Object> values;
		if ( styles.size() > 0 )
		{
			values = computeStylesFn.invoke( styles );
		}
		else
		{
			values = new HashMap<Object, Object>();
		}
		
		// Apply them
		for (Object x: selected)
		{
			if ( x instanceof EdStyleSpan )
			{
				( (EdStyleSpan)x ).getStyleAttrs().putAll( values );
			}
		}
		
		// Get surrounding content
		Visitor v1 = new TagsVisitor( this );
		v1.visitFromStartOfRootToMarker( selection.getStartMarker(), editFragmentElement );
		Visitor v2 = new TagsVisitor( this );
		v2.visitFromMarkerToEndOfRoot( selection.getEndMarker(), editFragmentElement );
		
		// Splice and merge
		ArrayList<Object> splicedFlattened = new ArrayList<Object>();
		splicedFlattened.addAll( v1.flattened() );
		splicedFlattened.addAll( selected );
		splicedFlattened.addAll( v2.flattened() );
		ArrayList<Object> splicedMerged = Merge.mergeParagraphs( splicedFlattened );
		
		// Convert to stream
		return new StreamValueBuilder( splicedMerged ).stream();
	}
	
	
	
	private static final StyleSheet editableParaStyle = StyleSheet.instance.withAttr( RichText.appendNewlineToParagraphs, true );
}
