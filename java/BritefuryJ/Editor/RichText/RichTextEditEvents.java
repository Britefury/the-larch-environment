//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.Editor.RichText.EditorModel.EdInlineEmbed;
import BritefuryJ.Editor.Sequential.EditListener;
import BritefuryJ.IncrementalView.FragmentView;

class RichTextEditEvents
{
	protected static abstract class RichTextRequest extends EditEvent
	{
		protected EditListener.HandleEditResult invokeOnInlineEmbed(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object embed, StreamValue value)
		{
			return EditListener.HandleEditResult.NOT_HANDLED;
		}

		protected EditListener.HandleEditResult invokeOnTextSpan(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object textSpan, StreamValue value)
		{
			return EditListener.HandleEditResult.NOT_HANDLED;
		}

		protected EditListener.HandleEditResult invokeOnParagraph(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object paragraph, StreamValue value)
		{
			return EditListener.HandleEditResult.NOT_HANDLED;
		}

		protected EditListener.HandleEditResult invokeOnParagraphListItem(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object paragraph, StreamValue value)
		{
			return EditListener.HandleEditResult.NOT_HANDLED;
		}

		protected EditListener.HandleEditResult invokeOnBlock(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object block, StreamValue value)
		{
			return EditListener.HandleEditResult.NOT_HANDLED;
		}
	}
	
	
	
	protected static class StyleRequest extends RichTextRequest
	{
		private RichTextEditor.ComputeSpanStylesFn computeStylesFn;
		
		public StyleRequest(RichTextEditor.ComputeSpanStylesFn computeStylesFn)
		{
			this.computeStylesFn = computeStylesFn;
		}

		@Override
		protected EditListener.HandleEditResult invokeOnParagraph(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object paragraph, StreamValue value)
		{
			Selection selection = element.getRootElement().getSelection();
			if ( selection instanceof TextSelection )
			{
				StreamValue stream = editor.streamWithModifiedSelectionStyle( element, (TextSelection)selection, computeStylesFn );
				editor.setParagraphContentsFromBlockStream( paragraph, stream );
				return EditListener.HandleEditResult.HANDLED;
			}
			return EditListener.HandleEditResult.NOT_HANDLED;
		}

		@Override
		protected EditListener.HandleEditResult invokeOnBlock(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object block, StreamValue value)
		{
			Selection selection = element.getRootElement().getSelection();
			if ( selection instanceof TextSelection )
			{
				StreamValue stream = editor.streamWithModifiedSelectionStyle( element, (TextSelection)selection, computeStylesFn );
				editor.setModelContentsFromStream( block, stream );
			}
			return EditListener.HandleEditResult.HANDLED;
		}
	}
	
	
	
	protected static class ParagraphStyleRequest extends RichTextRequest
	{
		private RichTextEditor.ModifyParagraphFn modifyParagraphFn;
		
		public ParagraphStyleRequest(RichTextEditor.ModifyParagraphFn modifyParagraphFn)
		{
			this.modifyParagraphFn = modifyParagraphFn;
		}

		@Override
		protected EditListener.HandleEditResult invokeOnParagraph(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object paragraph, StreamValue value)
		{
			modifyParagraphFn.invoke( paragraph );
			return EditListener.HandleEditResult.HANDLED;
		}
	}


	
	protected static class InsertParagraphRequest extends RichTextRequest
	{
		private RichTextEditor.MakeParagraphFn makeParagraphFn;
		private Object paragraphBefore = null;
		
		public InsertParagraphRequest(RichTextEditor.MakeParagraphFn makeParagraphFn)
		{
			this.makeParagraphFn = makeParagraphFn;
		}

		@Override
		protected EditListener.HandleEditResult invokeOnParagraphListItem(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object paragraph, StreamValue value)
		{
			paragraphBefore = paragraph;
			return EditListener.HandleEditResult.PASS_TO_PARENT;
		}
	
		@Override
		protected EditListener.HandleEditResult invokeOnBlock(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object block, StreamValue value)
		{
			if ( paragraphBefore != null )
			{
				Object para = makeParagraphFn.invoke();
				editor.insertParagraphIntoBlockAfter( block, para, paragraphBefore );
			}
			return EditListener.HandleEditResult.HANDLED;
		}
	}


	
	protected static class DeleteParagraphRequest extends RichTextRequest
	{
		private Object paragraph = null;
		
		public DeleteParagraphRequest()
		{
		}

		@Override
		protected EditListener.HandleEditResult invokeOnParagraphListItem(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object paragraph, StreamValue value)
		{
			this.paragraph = paragraph;
			return EditListener.HandleEditResult.PASS_TO_PARENT;
		}
	
		@Override
		protected EditListener.HandleEditResult invokeOnBlock(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object block, StreamValue value)
		{
			if ( paragraph != null )
			{
				editor.deleteParagraphFromBlock( block, paragraph );
			}
			return EditListener.HandleEditResult.HANDLED;
		}
	}



	protected static class InsertInlineEmbedRequest extends RichTextRequest
	{
		private Marker marker;
		private RichTextEditor.MakeInlineEmbedValueFn makeInlineEmbedFn;
		
		public InsertInlineEmbedRequest(Marker marker, RichTextEditor.MakeInlineEmbedValueFn makeInlineEmbedFn)
		{
			this.marker = marker;
			this.makeInlineEmbedFn = makeInlineEmbedFn;
		}

		@Override
		protected EditListener.HandleEditResult invokeOnParagraph(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object paragraph, StreamValue value)
		{
			Object embedValue = makeInlineEmbedFn.invoke();
			EdInlineEmbed embed = new EdInlineEmbed( embedValue );
			editor.insertInlineEmbed( element, paragraph, marker, embed );
			return EditListener.HandleEditResult.HANDLED;
		}
	}


	
	protected static class DeleteInlineEmbedRequest extends RichTextRequest
	{
		private Object embed = null;
		
		public DeleteInlineEmbedRequest()
		{
		}

		@Override
		protected EditListener.HandleEditResult invokeOnInlineEmbed(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object embed, StreamValue value)
		{
			this.embed = embed;
			return EditListener.HandleEditResult.PASS_TO_PARENT;
		}
	
		@Override
		protected EditListener.HandleEditResult invokeOnTextSpan(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object textSpan, StreamValue value)
		{
			editor.removeInlineEmbed( textSpan, embed );
			return EditListener.HandleEditResult.HANDLED;
		}
		
		@Override
		protected EditListener.HandleEditResult invokeOnParagraph(RichTextEditor editor, DPElement element,
				DPElement sourceElement, FragmentView fragment, Object paragraph, StreamValue value)
		{
			editor.removeInlineEmbed( paragraph	, embed );
			return EditListener.HandleEditResult.HANDLED;
		}
	}
}
