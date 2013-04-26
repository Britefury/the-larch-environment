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
import BritefuryJ.Editor.Sequential.SequentialController;
import BritefuryJ.Editor.Sequential.Item.SoftStructuralItem;
import BritefuryJ.Editor.Sequential.Item.StructuralItem;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.ElementTreeVisitor;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.SequentialRichStringVisitor;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Segment;
import BritefuryJ.Pres.RichText.RichText;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringBuilder;

public abstract class RichTextController extends SequentialController
{
	private static abstract class Visitor extends ElementTreeVisitor
	{
		protected ArrayList<Object> prefix = new ArrayList<Object>();
		protected ArrayList<Object> contents = new ArrayList<Object>();
		protected ArrayList<Object> suffix = new ArrayList<Object>();
		protected RichTextController controller;
		
		
		public Visitor(RichTextController controller)
		{
			this.controller = controller;
		}


		@Override
		protected void preOrderVisitElement(LSElement e, boolean complete)
		{
			if ( e.hasFixedValue() )
			{
				Object model = e.getFixedValue();
				
				Object prefix = controller.modelToPrefixTag( model );
				if ( prefix != null )
				{
					contents.add( prefix );
				}
				
				if ( !complete )
				{
					Object regionStart = controller.modelToContainingPrefixTag( model );
					Object regionEnd = controller.modelToContainingSuffixTag( model );
					
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
					Object start = controller.modelToContainingPrefixTag( model );
					Object end = controller.modelToContainingSuffixTag( model );
					
					if ( start != null )
					{
						prefix.add( 0, start );
					}
					if ( end != null )
					{
						contents.add( end );
					}
				}

				Object suffix = controller.modelToSuffixTag( model );
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
		public TagsVisitor(RichTextController controller)
		{
			super( controller );
		}

		@Override
		protected void completelyVisitStructralValue(Object value)
		{
			controller.modelToTags( contents, value );
		}
	}
	
	
	private static class NodeVisitor extends Visitor
	{
		public NodeVisitor(RichTextController controller)
		{
			super( controller );
		}

		@Override
		protected void completelyVisitStructralValue(Object value)
		{
			if ( controller.isStyleSpan( value ) )
			{
				controller.modelToTags( contents, value );
			}
			else
			{
				contents.add( controller.modelToEditorModel( value ) );
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
	
	
	protected static class RichTextControllerPropertyKey
	{
	}
	
	// Instance variables, so that each rich text editor has its own set of keys, so that two different editors cannot interfere with one another
	protected final RichTextControllerPropertyKey inlineEmbedPropertyKey = new RichTextControllerPropertyKey();
	protected final RichTextControllerPropertyKey spanPropertyKey = new RichTextControllerPropertyKey();
	protected final RichTextControllerPropertyKey paragraphPropertyKey = new RichTextControllerPropertyKey();
	protected final RichTextControllerPropertyKey paragraphEmbedPropertyKey = new RichTextControllerPropertyKey();
	protected final RichTextControllerPropertyKey blockPropertyKey = new RichTextControllerPropertyKey();
	protected final RichTextControllerPropertyKey blockItemPropertyKey = new RichTextControllerPropertyKey();
	
	
	
	
	
	private RichStringEditFilter textEditListener, paraEditListener, blockEditListener;
	
	private TreeEventListener richTextBreakListener = new TreeEventListener()
	{
		@Override
		public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
		{
			if ( event instanceof EditEvent )
			{
				EditEvent editEvent = (EditEvent)event;
				if ( event instanceof TextEditEvent  ||  isSelectionEditEvent( editEvent )  ||  isEditEvent( editEvent )  ||  event instanceof ClearNeighbourEditEvent )
				{
					// If event is a selection edit event, and its source element is @element, then @element has had its fixed value
					// set by a SequentialClipboardHandler - so don't clear it.
					// Otherwise, clear it
					if ( !( isSelectionEditEvent( editEvent )  &&  getEventSourceElement( editEvent ) == element ) )
					{
						SequentialRichStringVisitor visitor = editEvent.getRichStringVisitor();
						Object model = element.getFixedValue();
						if ( model == null )
						{
							throw new RuntimeException( "Could not get model from element" );
						}
						EdNode ed = modelToEditorModel( model );
						
						Tag prefix = ed.prefixTag();
						if ( prefix != null )
						{
							visitor.setElementPrefix( element, prefix );
						}

						Tag regionStart = ed.containingPrefixTag();
						if ( regionStart != null )
						{
							visitor.setElementPrefix( element, regionStart );
						}

						Tag regionEnd = ed.containingSuffixTag();
						if ( regionEnd != null )
						{
							visitor.setElementSuffix( element, regionEnd );
						}

						Tag suffix = ed.suffixTag();
						if ( suffix != null )
						{
							visitor.setElementSuffix( element, suffix );
						}
					}
				}
			}
			return false;
		}
	};
	
	public RichTextController(String controllerName)
	{
		super( controllerName );
		
		RichStringEditFilterFn onTextEdit = new RichStringEditFilterFn()
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
					return handled  ?  EditFilter.HandleEditResult.HANDLED  :  EditFilter.HandleEditResult.NOT_HANDLED;
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

		
		RichStringEditFilterFn onParagraphEdit = new RichStringEditFilterFn()
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
					return handled  ?  EditFilter.HandleEditResult.HANDLED  :  EditFilter.HandleEditResult.NOT_HANDLED;
				}
				else if ( event instanceof SelectionEditTreeEvent )
				{
					boolean handled = setParagraphContentsFromCompleteParagraphRichString( fragment.getView().getLog(), model, value );
					return handled  ?  EditFilter.HandleEditResult.HANDLED  :  EditFilter.HandleEditResult.NOT_HANDLED;
				}
				return HandleEditResult.NOT_HANDLED;
			}
		};

		
		RichStringEditFilterFn onBlockEdit = new RichStringEditFilterFn()
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
					setBlockModelContentsFromEditorModelRichString( fragment.getView().getLog(), model, value );
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
		Pres p = new StructuralItem( this, model, child );
		return p.withProperty( inlineEmbedPropertyKey, model );
	}
	
	public Pres editableSpan(Object model, Object child)
	{
		Pres p = richTextSoftStructuralItem( model, Pres.coerce( child ), textEditListener );
		return p.withProperty( spanPropertyKey, model );
	}
	
	public Pres editableParagraph(Object model, Object child)
	{
		Pres p = editableParaStyle.applyTo( child );
		p = richTextSoftStructuralItem( model, p, paraEditListener );
		return p.withProperty( paragraphPropertyKey, model ).withProperty( blockItemPropertyKey, model );
	}

	public Pres editableParagraphEmbed(Object model, Object child)
	{
		Pres p = new StructuralItem( this, model, child );
		p = new Row( new Pres[] { new Segment( p ) } );
		// The Segment surrounding the content will add caret slot elements either side of the embedded object
		// These elements will generate empty strings that will appear either side of the structural item when the rich
		// string value is acquired. These can cause problems when copying and pasting into a block.
		// To alleviate this issue, attached the value of the embedded object, along with a 'clear structural value' listener to @p.
		p = p.withFixedValue( model );
		p = p.withTreeEventListener( getClearStructuralValueListener() );
		return p.withProperty( paragraphEmbedPropertyKey, model ).withProperty( blockItemPropertyKey, model );
	}
	
	public Pres editableBlock(Object model, Object child)
	{
		Pres p = new SoftStructuralItem( this, blockEditListener, model, child );
		return p.withProperty( blockPropertyKey, model );
	}
	

	private Pres richTextSoftStructuralItem(Object model, Pres child, TreeEventListener editListener)
	{
		// Give the element a fixed value; the model
		Pres p = child.withFixedValue( model );
		// The first event handler suppresses the structural value, so that the next event handler gets the contents of the paragraph, rather than
		// the unmodified data model
		p = p.withTreeEventListener( getClearStructuralValueListener() );
		// The next event handler will apply modifications to the paragraph in response to user edits, provided that the paragraph does not get split with a newline
		p = p.withTreeEventListener( editListener );
		// Finally, the break event handler attaches the paragraph prefix tag to the beginning, so that the outer stages (block-level edits) can re-assemble the paragraph
		// with styling attributes intact
		p = p.withTreeEventListener( richTextBreakListener );
		return p;
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
	
	
	
	public void insertInlineEmbedAtCaret(Caret caret, MakeInlineEmbedValueFn makeInlineEmbedValue)
	{
		if ( caret.isValid()  &&  caret.isEditable() )
		{
			insertInlineEmbedAtMarker( caret.getMarker(), makeInlineEmbedValue );
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
			
			// Find the text span or paragraph that contains the inline embedded object
			LSElement.PropertyValue textPropValue = embedElement.findFirstPropertyInAncestors( Arrays.asList( new Object[] { spanPropertyKey, paragraphPropertyKey } ) );
			if ( textPropValue != null )
			{
				Object textModel = textPropValue.getValue();

				// Find the paragraph that contains the inline embedded object (may be the same the outer text span)
				LSElement.PropertyValue paraPropValue = embedElement.findPropertyInAncestors( paragraphPropertyKey );
				if ( paraPropValue != null )
				{
					LSElement paragraphElement = paraPropValue.getElement();
					FragmentView fragment = (FragmentView)paragraphElement.getFragmentContext();
					Object paragraphModel = paraPropValue.getValue();

					removeInlineEmbedFromText( fragment.getView().getLog(), paragraphElement, embedElement, paragraphModel, textModel, embed );
					//removeInlineEmbed( text, embed );
				}
				//removeInlineEmbed( textModel, embed );
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
						setParagraphContentsFromCompleteParagraphRichString( paragraphFragment.getView().getLog(), paragraphModel, richStr );
					}
					else if ( targetPropValue.getKey() == blockPropertyKey )
					{
						Object blockModel = targetPropValue.getValue();
						LSElement blockElement = targetPropValue.getElement();
						RichString richStr = richStringWithModifiedSelectionStyle( blockElement, (TextSelection)selection, computeSpanStyles );
						FragmentView blockFragment = (FragmentView)blockElement.getFragmentContext();
						setBlockModelContentsFromEditorModelRichString( blockFragment.getView().getLog(), blockModel, richStr );
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


	private boolean setTextContentsFromRichString(Log log, Object model, RichString value)
	{
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextController" ).hItem( "Description", "RichTextEditor.setTextContentsFromRichString" ).vItem( "richStr", value ) );
		}
		setModelContentsFromEditorModelRichString( model, value );
		return !value.contains( "\n" );
	}

	private boolean setParagraphTextContentsFromRichString(Log log, Object model, RichString value)
	{
		if ( value.endsWith( "\n" ) )
		{
			if ( log.isRecording() )
			{
				log.log( new LogEntry( "RichTextController" ).hItem( "Description", "RichTextEditor.setParagraphTextContentsFromRichString - with trailing newline" ).vItem( "richStr", value ) );
			}
			RichString toLast = value.substring( 0, value.length() - 1 );
			setModelContentsFromEditorModelRichString( model, toLast );
			return !toLast.contains( "\n" );
		}
		else
		{
			if ( log.isRecording() )
			{
				log.log( new LogEntry( "RichTextController" ).hItem( "Description", "RichTextEditor.setParagraphTextContentsFromRichString - no trailing newline" ).vItem( "richStr", value ) );
			}
			setModelContentsFromEditorModelRichString( model, value );
			EdNode e = modelToEditorModel( model );
			((EdParagraph)e).suppressNewline();
		}
		return false;
	}


	private boolean setParagraphContentsFromCompleteParagraphRichString(Log log, Object paragraph, RichString richStr)
	{
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextController" ).hItem( "Description", "RichTextEditor.setParagraphContentsFromCompleteParagraphRichString" ).vItem( "richStr", richStr ) );
		}
		List<Object> items = richStr.getItemValues();
		if ( items.size() == 1 )
		{
			EdParagraph inputPara = (EdParagraph)items.get( 0 );
			List<? extends Object> contents = inputPara.getContents();
			RichString contentRichStr = new RichStringBuilder( contents ).richString();
			setModelContentsFromEditorModelRichString( paragraph, contentRichStr );
			// No need to return false if newlines detected.
			// They will have been replaced by paragraphs in an earlier stage of processing.
			return true;
		}
		else
		{
			return false;
		}
	}


	private void setBlockContentsFromRawRichString(Log log, Object model, RichString value)
	{
		ArrayList<Object> tags = new ArrayList<Object>();
		
		// Convert items, leaving tags in place, convering model objects to editor model objects
		for (RichString.Item item: value.getItems())
		{
			if ( item.isStructural() )
			{
				RichString.StructuralItem structuralItem = (RichString.StructuralItem)item;
				Object structuralValue = structuralItem.getValue();
				if ( structuralValue instanceof Tag )
				{
					tags.add( (Tag)structuralValue );
				}
				else
				{
					tags.add( modelToEditorModel( structuralValue ) );
				}
			}
			else
			{
				RichString.TextItem text = (RichString.TextItem)item;
				tags.add( text.getValue() );
			}
		}
		
		List<Object> flattened = Flatten.flattenParagraphs( tags );
		List<Object> flattenedForLog = null;
		if ( log.isRecording() )
		{
			flattenedForLog = new ArrayList<Object>();
			flattenedForLog.addAll( flattened );
		}
		List<Object> paras = Merge.mergeParagraphs( flattened );
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextController" ).hItem( "Description", "RichTextEditor.setBlockContentsFromRawRichString" ).vItem( "value", value ).vItem( "tags", tags ).vItem( "flattened", flattenedForLog ).vItem( "paras", paras ) );
		}
		setModelContentsFromEditorModelRichString( model, new RichStringBuilder( paras ).richString() );
	}



	
	protected abstract void setModelContents(Object model, List<Object> contents);
	protected abstract EdNode modelToEditorModel(Object model);

	protected abstract boolean isDataModelObject(Object x);
	
	protected abstract void insertParagraphIntoBlockAfter(Object block, Object para, Object paragraphBefore);
	protected abstract void deleteParagraphFromBlock(Object block, Object paragraph);
	
	
	protected abstract Object buildInlineEmbed(Object value);
	protected abstract Object buildParagraphEmbed(Object value);
	protected abstract Object buildParagraph(List<Object> contents, Map<Object, Object> styleAttrs);
	protected abstract Object buildSpan(List<Object> contents, Map<Object, Object> styleAttrs);

	
	private Object buildModelForEditorModel(EdNode editorModel)
	{
		return editorModel.buildModel( this );
	}

	
	private Object convertModelToEditorModel(Object model)
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
			throw new RuntimeException( "Could not filter editor model value: value is not a String, EdNode, List, or data model object - it is " + x.toString() + " (" + x.getClass().getName() + ")" );
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

	
	private void setModelContentsFromEditorModelRichString(Object model, RichString richString)
	{
		List<Object> modelValues = editorModelListToModelList( richString.getItemValues() );

		setModelContents( model, modelValues );
	}

	private void setBlockModelContentsFromEditorModelRichString(Log log, Object model, RichString richString)
	{
		List<Object> modelValues = editorModelListToModelList( richString.getItemValues() );

		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextController" ).hItem( "Description", "RichTextEditor.setBlockModelContentsFromEditorModelRichString" ).vItem( "value", richString.getItemValues() ).vItem( "modelValues", modelValues ) );
		}

		setModelContents( model, modelValues );
	}


	
	






	private Tag modelToPrefixTag(Object model)
	{
		return modelToEditorModel( model ).prefixTag();
	}
	
	private Tag modelToSuffixTag(Object model)
	{
		return modelToEditorModel( model ).suffixTag();
	}
	
	private Tag modelToContainingPrefixTag(Object model)
	{
		return modelToEditorModel( model ).containingPrefixTag();
	}
	
	private Tag modelToContainingSuffixTag(Object model)
	{
		return modelToEditorModel( model ).containingSuffixTag();
	}
	
	private void modelToTags(List<Object> tags, Object model)
	{
		modelToEditorModel( model ).buildTagList( tags );
	}
	
	
	
	private boolean isStyleSpan(Object model)
	{
		return modelToEditorModel( model ) instanceof EdStyleSpan;
	}
	
	private boolean isParagraph(Object model)
	{
		return modelToEditorModel( model ) instanceof EdParagraph;
	}
	
	private boolean isBlock(Object model)
	{
		return modelToEditorModel( model ) instanceof EdBlock;
	}
	
	
	
	
	

	@Override
	protected boolean isClipboardEditLevelFragmentView(FragmentView fragment)
	{
		Object model = fragment.getModel();
		return isParagraph( model )  ||  isBlock( model );
	}
	
	@Override
	public boolean isClearNeighbouringStructuresEnabled()
	{
		return true;
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
	public Object textToSequentialForImport(String text)
	{
		if ( text.contains( "\n" ) )
		{
			// If the text contains newlines, convert to flattened paragraphs
			return Flatten.flattenParagraphs( Arrays.asList( new Object[] { text } ) );
		}
		else
		{
			EdStyleSpan span = new EdStyleSpan( Arrays.asList( new Object[] { text } ), new HashMap<Object, Object>() );
			return Arrays.asList( new EdNode[] { span } );
		}
	}

	public Object modelParagraphListToSequentialForImport(List<Object> models)
	{
		ArrayList<Object> editorModels = new ArrayList<Object>();
		editorModels.ensureCapacity(models.size());
		for (Object m: models)
		{
			editorModels.add(modelToEditorModel(m));
		}
		return Flatten.flattenParagraphs(editorModels);
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




	private void insertInlineEmbed(Log log, LSElement element, Object paragraph, Marker marker, EdInlineEmbed embed)
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
		setParagraphContentsFromCompleteParagraphRichString( log, paragraph, new RichStringBuilder( splicedMerged ).richString() );
	}

	private void removeInlineEmbedFromText(Log log, LSElement paragraphElement, LSElement embedElement, Object paragraphModel, Object textModel, Object embedModel)
	{
		Visitor v1 = new TagsVisitor( this );
		v1.visitFromStartOfRootToElement( embedElement, paragraphElement );
		Visitor v2 = new TagsVisitor( this );
		v2.visitFromElementToEndOfRoot( embedElement, paragraphElement );
		ArrayList<Object> splicedFlattened = new ArrayList<Object>();
		splicedFlattened.addAll( v1.flattened() );
		splicedFlattened.addAll( v2.flattened() );
		ArrayList<Object> splicedMerged = Merge.mergeParagraphs( splicedFlattened );
		setParagraphContentsFromCompleteParagraphRichString( log, paragraphModel, new RichStringBuilder( splicedMerged ).richString() );
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
			Object regionStart = modelToContainingPrefixTag( m );
			Object regionEnd = modelToContainingSuffixTag( m );
			if ( regionStart != null )
			{
				v.prefix.add( 0, regionStart );
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
		Log log = subtreeRootFragment.getView().getLog();
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextController" ).hItem( "Description", "RichTextEditor.spliceForInsertion" ).vItem( "splicedFlattened", splicedFlattened ).vItem( "splicedMerged", splicedMerged ) );
		}
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
		Log log = subtreeRootFragment.getView().getLog();
		if ( log.isRecording() )
		{
			log.log( new LogEntry( "RichTextController" ).hItem( "Description", "RichTextEditor.spliceForDeletion" ).vItem( "splicedFlattened", splicedFlattened ).vItem( "splicedMerged", splicedMerged ) );
		}
		return new RichStringBuilder( splicedMerged ).richString();
	}
	

	
	private RichString richStringWithModifiedSelectionStyle(LSElement element, TextSelection selection, ComputeSpanStylesFn computeStylesFn)
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
		Map<Object, Object> values = null;
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
