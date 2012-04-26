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

import BritefuryJ.ClipboardFilter.ClipboardCopier;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.Editor.Sequential.EditFilter;
import BritefuryJ.Editor.Sequential.EditFilter.HandleEditResult;
import BritefuryJ.Editor.Sequential.RichStringEditFilter;
import BritefuryJ.Editor.Sequential.SelectionEditTreeEvent;
import BritefuryJ.Editor.Sequential.SequentialEditor;
import BritefuryJ.Editor.Sequential.Item.SoftStructuralItem;
import BritefuryJ.Editor.Sequential.Item.StructuralItem;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.ElementTreeVisitor;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.RichText.RichText;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringBuilder;

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
		protected void preOrderVisitElement(LSElement e, boolean complete)
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
		protected void inOrderCompletelyVisitElement(LSElement e)
		{
			RichString richStr = e.getRichString();
			for (RichString.Item item: richStr.getItems())
			{
				if ( item.isStructural() )
				{
					completelyVisitStructralValue( ((RichString.StructuralItem)item).getValue() );
				}
				else
				{
					contents.add( ((RichString.TextItem)item).getValue() );
				}
			}
		}


		@Override
		protected void postOrderVisitElement(LSElement e, boolean complete)
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
		protected void inOrderVisitPartialContentLeafEditable(LSContentLeafEditable e, int startIndex, int endIndex)
		{
			contents.add( e.getTextRepresentation().substring( startIndex, endIndex ) );
		}


		@Override
		protected boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit)
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
	
	

	//
	// PROPERTY KEYS - for finding elements that represent nodes of specific types
	//
	
	
	protected static class RichTextEditorPropertyKey
	{
	}
	
	// Instance variables, so that each rich text editor has its own set of keys, so that two different editors cannot interfere with one another
	protected final RichTextEditorPropertyKey inlineEmbedPropertyKey = new RichTextEditorPropertyKey();
	protected final RichTextEditorPropertyKey spanPropertyKey = new RichTextEditorPropertyKey();
	protected final RichTextEditorPropertyKey paragraphPropertyKey = new RichTextEditorPropertyKey();
	protected final RichTextEditorPropertyKey paragraphEmbedPropertyKey = new RichTextEditorPropertyKey();
	protected final RichTextEditorPropertyKey blockPropertyKey = new RichTextEditorPropertyKey();
	protected final RichTextEditorPropertyKey blockItemPropertyKey = new RichTextEditorPropertyKey();
	
	
	
	
	
	private RichStringEditFilter textEditListener, paraEditListener, blockEditListener;
	
	
	public RichTextEditor()
	{
		HandleRichStringFn onTextEdit = new HandleRichStringFn()
		{
			@Override
			public EditFilter.HandleEditResult handleValue(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value)
			{
				if ( event instanceof TextEditEvent )
				{
					boolean handled = false;
					if ( sourceElement.getFragmentContext() == fragment )
					{
						handled = setTextContentsFromRichString( fragment.getView().getLog(), model, value );
					}
					return handled  ?  EditFilter.HandleEditResult.HANDLED  :  EditFilter.HandleEditResult.PASS_TO_PARENT;
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

		
		HandleRichStringFn onParagraphEdit = new HandleRichStringFn()
		{
			@Override
			public EditFilter.HandleEditResult handleValue(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value)
			{
				if ( event instanceof TextEditEvent )
				{
					boolean handled = false;
					if ( sourceElement.getFragmentContext() == fragment )
					{
						handled = setParagraphTextContentsFromRichString( fragment.getView().getLog(), model, value );
					}
					return handled  ?  EditFilter.HandleEditResult.HANDLED  :  EditFilter.HandleEditResult.PASS_TO_PARENT;
				}
				else if ( event instanceof SelectionEditTreeEvent )
				{
					boolean handled = setParagraphContentsFromBlockRichString( fragment.getView().getLog(), model, value );
					return handled  ?  EditFilter.HandleEditResult.HANDLED  :  EditFilter.HandleEditResult.PASS_TO_PARENT;
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

		
		HandleRichStringFn onBlockEdit = new HandleRichStringFn()
		{
			@Override
			public EditFilter.HandleEditResult handleValue(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value)
			{
				if ( event instanceof TextEditEvent )
				{
					setBlockContentsFromRawRichString( fragment.getView().getLog(), model, value );
					return EditFilter.HandleEditResult.HANDLED;
				}
				else if ( event instanceof SelectionEditTreeEvent )
				{
					setModelContentsFromEditorModelRichString( model, value );
					return EditFilter.HandleEditResult.HANDLED;
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

	
		textEditListener = richStringEditFilter( onTextEdit );
		paraEditListener = richStringEditFilter( onParagraphEdit );
		blockEditListener = richStringEditFilter( onBlockEdit );
	}
	
	
	
	
	
	
	
	
	//
	//
	// PUBLIC INTERFACE
	//
	//
	
	
	//
	// Presentation methods - wrap Pres objects in event handlers to take care of editing events
	//
	
	public Pres editableInlineEmbed(Object model, Object child)
	{
		child = new Proxy( child );
		Pres p = new StructuralItem( model, child );
		return p.withProperty( inlineEmbedPropertyKey, model );
	}
	
	public Pres editableSpan(Object model, Object child)
	{
		Pres p =  new SoftStructuralItem( this, textEditListener, model, child );
		return p.withProperty( spanPropertyKey, model );
	}
	
	public Pres editableParagraph(Object model, Object child)
	{
		child = editableParaStyle.applyTo( child );
		Pres p = new SoftStructuralItem( this, Arrays.asList( new TreeEventListener[] { paraEditListener } ), model, child );
		return p.withProperty( paragraphPropertyKey, model ).withProperty( blockItemPropertyKey, model );
	}
	
	public Pres editableParagraphEmbed(Object model, Object child)
	{
		Pres p = new StructuralItem( model, child );
		return p.withProperty( paragraphEmbedPropertyKey, model ).withProperty( blockItemPropertyKey, model );
	}
	
	public Pres editableBlock(Object model, Object child)
	{
		Pres p = new SoftStructuralItem( this, blockEditListener, model, child );
		return p.withProperty( blockPropertyKey, model );
	}
	
	
	
	//
	// Editor model node construction methods
	//
	
	public EdParagraph editorModelParagraph(List<Object> modelContents, Map<Object, Object> styleAttrs)
	{
		return new EdParagraph( convertModelListToEditorModelList( modelContents ), styleAttrs );
	}
	
	public EdStyleSpan editorModelSpan(List<Object> modelContents, Map<Object, Object> styleAttrs)
	{
		return new EdStyleSpan( convertModelListToEditorModelList( modelContents ), styleAttrs );
	}
	
	public EdInlineEmbed editorModelInlineEmbed(Object value)
	{
		return new EdInlineEmbed( value );
	}
	
	public EdParagraphEmbed editorModelParagraphEmbed(Object value)
	{
		return new EdParagraphEmbed( value );
	}
	
	public EdBlock editorModelBlock(List<Object> modelContents)
	{
		ArrayList<EdNode> ed = new ArrayList<EdNode>();
		ed.ensureCapacity( modelContents.size() );
		for (Object x: modelContents)
		{
			ed.add( modelToEditorModel( x ) );
		}
		return new EdBlock( ed );
	}
	
	
	
	
	//
	// Content modification methods
	//

	public void insertParagraphAtCaret(Caret caret, MakeParagraphFn makeParagraph)
	{
		if ( caret.isValid()  &&  caret.isEditable() )
		{
			insertParagraphAtMarker( caret.getMarker(), makeParagraph );
		}
	}
	
	public void insertParagraphAtMarker(Marker marker, MakeParagraphFn makeParagraph)
	{
		if ( marker.isValid()  &&  marker.getElement().isEditable() )
		{
			LSElement element = marker.getElement();
			LSElement.PropertyValue paraPropValue = element.findPropertyInAncestors( blockItemPropertyKey );
			if ( paraPropValue != null )
			{
				LSElement paragraphElement = paraPropValue.getElement();
				Object paragraphBeforeModel = paraPropValue.getValue();
				
				LSElement.PropertyValue blockValue = paragraphElement.findPropertyInAncestors( blockPropertyKey );
				if ( blockValue != null )
				{
					Object blockModel = blockValue.getValue();
					Object para = makeParagraph.invoke();
					insertParagraphIntoBlockAfter( blockModel, para, paragraphBeforeModel );
				}
			}
		}
	}
	
	public void deleteParagraphContainingElement(LSElement element)
	{
		LSElement.PropertyValue paraPropValue = element.findPropertyInAncestors( blockItemPropertyKey );
		if ( paraPropValue != null )
		{
			Object paragraphModel = paraPropValue.getValue();
			LSElement paragraphElement = paraPropValue.getElement();
			
			LSElement.PropertyValue blockValue = paragraphElement.findPropertyInAncestors( blockPropertyKey );
			if ( blockValue != null )
			{
				Object blockModel = blockValue.getValue();
				deleteParagraphFromBlock( blockModel, paragraphModel );
			}
		}
	}
	
	
	
	public void insertInlineEmbedAtMarker(Marker marker, MakeInlineEmbedValueFn makeInlineEmbedValue)
	{
		if ( marker.isValid()  &&  marker.getElement().isEditable() )
		{
			LSElement element = marker.getElement();
			LSElement.PropertyValue paraPropValue = element.findPropertyInAncestors( paragraphPropertyKey );
			if ( paraPropValue != null )
			{
				LSElement paragraphElement = paraPropValue.getElement();
				FragmentView fragment = (FragmentView)paragraphElement.getFragmentContext();
				Object paragraphModel = paraPropValue.getValue();
				
				Object embedValue = makeInlineEmbedValue.invoke();
				EdInlineEmbed embed = new EdInlineEmbed( embedValue );
				insertInlineEmbed( fragment.getView().getLog(), paragraphElement, paragraphModel, marker, embed );
			}
		}
	}
	
	public void deleteInlineEmbedContainingElement(LSElement element)
	{
		LSElement.PropertyValue embedPropValue = element.findPropertyInAncestors( inlineEmbedPropertyKey );
		if ( embedPropValue != null )
		{
			Object embed = embedPropValue.getValue();
			LSElement embedElement = embedPropValue.getElement();
			
			LSElement.PropertyValue textPropValue = embedElement.findFirstPropertyInAncestors( Arrays.asList( new Object[] { spanPropertyKey, paragraphPropertyKey } ) );
			if ( textPropValue != null )
			{
				Object text = textPropValue.getValue();
				removeInlineEmbed( text, embed );
			}
		}
	}
	
	
	public void applyStyleToSelection(TextSelection selection, ComputeSpanStylesFn computeSpanStyles)
	{
		if ( selection.isValid()  &&  selection.isEditable() )
		{
			LSElement rootElement = selection.getCommonRoot();
			if ( rootElement != null )
			{
				LSElement.PropertyValue targetPropValue = rootElement.findFirstPropertyInAncestors( Arrays.asList( new Object[] { paragraphPropertyKey, blockPropertyKey } ) );
				if ( targetPropValue != null )
				{
					if ( targetPropValue.getKey() == paragraphPropertyKey )
					{
						// Apply modification to closest paragraph
						Object paragraphModel = targetPropValue.getValue();
						LSElement paragraphElement = targetPropValue.getElement();
						FragmentView paragraphFragment = (FragmentView)paragraphElement.getFragmentContext();
						RichString richStr = richStringWithModifiedSelectionStyle( paragraphElement, selection, computeSpanStyles );
						setParagraphContentsFromBlockRichString( paragraphFragment.getView().getLog(), paragraphModel, richStr );
					}
					else if ( targetPropValue.getKey() == blockPropertyKey )
					{
						Object blockModel = targetPropValue.getValue();
						LSElement blockElement = targetPropValue.getElement();
						RichString richStr = richStringWithModifiedSelectionStyle( blockElement, (TextSelection)selection, computeSpanStyles );
						setModelContentsFromEditorModelRichString( blockModel, richStr );
					}
				}

				
				//rootElement.postTreeEvent( new RichTextEditEvents.StyleRequest( computeSpanStyles ) );
			}
		}
	}
	
	public void modifyParagraphAtMarker(Marker marker, ModifyParagraphFn modifyParagraph)
	{
		if ( marker.isValid()  &&  marker.getElement().isEditable() )
		{
			LSElement element = marker.getElement();
			LSElement.PropertyValue paraPropValue = element.findPropertyInAncestors( paragraphPropertyKey );
			if ( paraPropValue != null )
			{
				Object paragraphModel = paraPropValue.getValue();
				
				modifyParagraph.invoke( paragraphModel );
			}
		}
	}
	
	




	
	//
	// INTERNAL: content modification methods
	//


	protected boolean setTextContentsFromRichString(Log log, Object model, RichString value)
	{
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setTextContentsFromRichString" ).vItem( "richStr", value ) );
		}
		setModelContentsFromEditorModelRichString( model, value );
		return !value.contains( "\n" );
	}

	protected boolean setParagraphTextContentsFromRichString(Log log, Object model, RichString value)
	{
		if ( value.endsWith( "\n" ) )
		{
			if ( log.isRecording() )
			{
				log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setParagraphTextContentsFromRichString - with trailing newline" ).vItem( "richStr", value ) );
			}
			RichString toLast = value.substring( 0, value.length() - 1 );
			setModelContentsFromEditorModelRichString( model, toLast );
			return !toLast.contains( "\n" );
		}
		else
		{
			if ( log.isRecording() )
			{
				log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setParagraphTextContentsFromRichString - no trailing newline" ).vItem( "richStr", value ) );
			}
			setModelContentsFromEditorModelRichString( model, value );
			EdNode e = modelToEditorModel( model );
			((EdParagraph)e).suppressNewline();
		}
		return false;
	}


	protected boolean setParagraphContentsFromBlockRichString(Log log, Object paragraph, RichString richStr)
	{
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setParagraphContentsFromBlockRichString" ).vItem( "richStr", richStr ) );
		}
		List<Object> items = richStr.getItemValues();
		if ( items.size() == 1 )
		{
			EdParagraph block = (EdParagraph)items.get( 0 );
			List<? extends Object> contents = block.getContents();
			setModelContentsFromEditorModelRichString( paragraph, new RichStringBuilder( contents ).richString() );
			return true;
		}
		else
		{
			return false;
		}
	}


	protected void setBlockContentsFromRawRichString(Log log, Object model, RichString value)
	{
		ArrayList<Object> tags = new ArrayList<Object>();
		modelToTags( tags, model );
		
		List<Object> flattened = Flatten.flattenParagraphs( tags );
		List<Object> paras = Merge.mergeParagraphs( flattened );
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextEditor" ).hItem( "Description", "RichTextEditor.setBlockContentsFromRawRichString" ).vItem( "tags", tags ).vItem( "paras", paras ) );
		}
		setModelContentsFromEditorModelRichString( model, new RichStringBuilder( paras ).richString() );
	}



	
	protected abstract void setModelContents(Object model, List<Object> contents);
	protected abstract EdNode modelToEditorModel(Object model);

	protected abstract boolean isDataModelObject(Object x);
	
	protected abstract void insertParagraphIntoBlockAfter(Object block, Object para, Object paragraphBefore);
	protected abstract void deleteParagraphFromBlock(Object block, Object paragraph);
	protected abstract void removeInlineEmbed(Object spanOrParagraph, Object embed);
	
	
	protected abstract Object buildInlineEmbed(Object value);
	protected abstract Object buildParagraphEmbed(Object value);
	protected abstract Object buildParagraph(List<Object> contents, Map<Object, Object> styleAttrs);
	protected abstract Object buildSpan(List<Object> contents, Map<Object, Object> styleAttrs);

	
	private Object buildModelForEditorModel(EdNode editorModel)
	{
		return editorModel.buildModel( this );
	}

	
	protected Object convertModelToEditorModel(Object model)
	{
		if ( model instanceof String )
		{
			return model;
		}
		else if ( model instanceof List )
		{
			List<?> xs = (List<?>)model;
			ArrayList<Object> fxs = new ArrayList<Object>( xs.size() );
			for (Object a: xs)
			{
				fxs.add( convertModelToEditorModel( a ) );
			}
			return fxs;
		}
		else
		{
			return modelToEditorModel( model );
		}
	}
	
	protected List<Object> convertModelListToEditorModelList(List<Object> x)
	{
		ArrayList<Object> ed = new ArrayList<Object>();
		ed.ensureCapacity( x.size() );
		for (Object a: x)
		{
			ed.add( convertModelToEditorModel( a ) );
		}
		return ed;
	}

	
	public Object filterValueFromEditorModel(Object x)
	{
		if ( x instanceof String )
		{
			return x;
		}
		else if ( x instanceof EdNode )
		{
			return buildModelForEditorModel( (EdNode)x );
		}
		else if ( x instanceof List )
		{
			List<?> xs = (List<?>)x;
			ArrayList<Object> fxs = new ArrayList<Object>( xs.size() );
			for (Object a: xs)
			{
				fxs.add( filterValueFromEditorModel( a ) );
			}
			return fxs;
		}
		else if ( isDataModelObject( x ) )
		{
			return x;
		}
		else
		{
			throw new RuntimeException( "Could not filter editor model value: value is not a String, EdNode, List, or data model object - it is a " + x.getClass().getName() );
		}
	}


	protected List<Object> editorModelListToModelList(List<Object> x)
	{
		ArrayList<Object> modelList = new ArrayList<Object>();
		modelList.ensureCapacity( x.size() );
		for (Object a: x)
		{
			modelList.add( filterValueFromEditorModel( a ) );
		}
		return modelList;
	}

	
	protected void setModelContentsFromEditorModelRichString(Object model, RichString richString)
	{
		List<Object> modelValues = editorModelListToModelList( richString.getItemValues() );
		
		setModelContents( model, modelValues );
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




	protected void insertInlineEmbed(Log log, LSElement element, Object paragraph, Marker marker, EdInlineEmbed embed)
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
		setParagraphContentsFromBlockRichString( log, paragraph, new RichStringBuilder( splicedMerged ).richString() );
	}




	@Override
	public Object getSequentialContentInSelection(FragmentView editFragment, LSElement editFragmentElement, TextSelection selection)
	{
		Visitor v = new NodeVisitor( this );
		// Copy the acquired content, so that if it is modified after copying, the modifications to not affect pasted copies 
		List<Object> f = getFlattenedContentInSelection( v, editFragment, selection );
		return clipboardCopyFlattenedList( f );
	}


	private List<Object> getFlattenedContentInSelection(Visitor v, FragmentView editFragment, TextSelection selection)
	{
		v.visitTextSelection( selection );
		
		// Walk the tree from the common root of the selection, up to the 'edit fragment',
		// wrapping the content in start/end tags as we go
		LSElement commonRoot = selection.getCommonRoot();
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


	
	private List<? extends Object> clipboardCopyFlattenedList(List<? extends Object> flattened)
	{
		ArrayList<Object> copy = new ArrayList<Object>();
		copy.ensureCapacity( flattened.size() );
		ClipboardCopierMemo memo = ClipboardCopier.instance.memo();
		for (Object x: flattened)
		{
			copy.add( memo.copy( x ) );
		}
		return copy;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public Object spliceForInsertion(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, Marker prefixEnd, Marker suffixStart, Object insertedContent)
	{
		Visitor v1 = new NodeVisitor( this );
		v1.visitFromStartOfRootToMarker( prefixEnd, subtreeRootFragmentElement );
		Visitor v2 = new NodeVisitor( this );
		v2.visitFromMarkerToEndOfRoot( suffixStart, subtreeRootFragmentElement );
		ArrayList<Object> splicedFlattened = new ArrayList<Object>();
		splicedFlattened.addAll( v1.flattened() );
		splicedFlattened.addAll( clipboardCopyFlattenedList( (List<? extends Object>)insertedContent ) );
		splicedFlattened.addAll( v2.flattened() );
		ArrayList<Object> splicedMerged = Merge.mergeParagraphs( splicedFlattened );
		return new RichStringBuilder( splicedMerged ).richString();
	}

	@Override
	public Object spliceForDeletion(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, Marker selectionStart, Marker selectionEnd)
	{
		Visitor v1 = new NodeVisitor( this );
		v1.visitFromStartOfRootToMarker( selectionStart, subtreeRootFragmentElement );
		Visitor v2 = new NodeVisitor( this );
		v2.visitFromMarkerToEndOfRoot( selectionEnd, subtreeRootFragmentElement );
		ArrayList<Object> splicedFlattened = new ArrayList<Object>();
		splicedFlattened.addAll( v1.flattened() );
		splicedFlattened.addAll( v2.flattened() );
		ArrayList<Object> splicedMerged = Merge.mergeParagraphs( splicedFlattened );
		return new RichStringBuilder( splicedMerged ).richString();
	}
	

	
	protected RichString richStringWithModifiedSelectionStyle(LSElement element, TextSelection selection, ComputeSpanStylesFn computeStylesFn)
	{
		FragmentView editFragment = (FragmentView)element.getFragmentContext();
		LSElement editFragmentElement = editFragment.getFragmentElement();
		
		// Get the content within the selection
		Visitor selectionVisitor = new TagsVisitor( this );
		List<Object> selected = getFlattenedContentInSelection( selectionVisitor, editFragment, selection );
		
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
		
		// Convert to rich string
		return new RichStringBuilder( splicedMerged ).richString();
	}


	private static final StyleSheet editableParaStyle = StyleSheet.style( RichText.appendNewlineToParagraphs.as( true ) );
}
