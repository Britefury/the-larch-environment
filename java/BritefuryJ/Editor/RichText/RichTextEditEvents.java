//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import BritefuryJ.Editor.Sequential.EditFilter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Util.RichString.RichString;

class RichTextEditEvents
{
	protected static abstract class RichTextRequest extends EditEvent
	{
		protected EditFilter.HandleEditResult invokeOnInlineEmbed(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object embed, RichString value)
		{
			return EditFilter.HandleEditResult.NOT_HANDLED;
		}

		protected EditFilter.HandleEditResult invokeOnTextSpan(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object textSpan, RichString value)
		{
			return EditFilter.HandleEditResult.NOT_HANDLED;
		}

		protected EditFilter.HandleEditResult invokeOnParagraph(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object paragraph, RichString value)
		{
			return EditFilter.HandleEditResult.NOT_HANDLED;
		}

		protected EditFilter.HandleEditResult invokeOnParagraphListItem(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object paragraph, RichString value)
		{
			return EditFilter.HandleEditResult.NOT_HANDLED;
		}

		protected EditFilter.HandleEditResult invokeOnBlock(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object block, RichString value)
		{
			return EditFilter.HandleEditResult.NOT_HANDLED;
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
		protected EditFilter.HandleEditResult invokeOnParagraph(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object paragraph, RichString value)
		{
			Selection selection = element.getRootElement().getSelection();
			if ( selection instanceof TextSelection )
			{
				RichString richStr = editor.richStringWithModifiedSelectionStyle( element, (TextSelection)selection, computeStylesFn );
				editor.setParagraphContentsFromBlockRichString( fragment.getView().getLog(), paragraph, richStr );
				return EditFilter.HandleEditResult.HANDLED;
			}
			return EditFilter.HandleEditResult.NOT_HANDLED;
		}

		@Override
		protected EditFilter.HandleEditResult invokeOnBlock(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object block, RichString value)
		{
			Selection selection = element.getRootElement().getSelection();
			if ( selection instanceof TextSelection )
			{
				RichString richStr = editor.richStringWithModifiedSelectionStyle( element, (TextSelection)selection, computeStylesFn );
				editor.setModelContentsFromEditorModelRichString( block, richStr );
			}
			return EditFilter.HandleEditResult.HANDLED;
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
		protected EditFilter.HandleEditResult invokeOnParagraph(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object paragraph, RichString value)
		{
			modifyParagraphFn.invoke( paragraph );
			return EditFilter.HandleEditResult.HANDLED;
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
		protected EditFilter.HandleEditResult invokeOnParagraphListItem(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object paragraph, RichString value)
		{
			paragraphBefore = paragraph;
			return EditFilter.HandleEditResult.PASS_TO_PARENT;
		}
	
		@Override
		protected EditFilter.HandleEditResult invokeOnBlock(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object block, RichString value)
		{
			if ( paragraphBefore != null )
			{
				Object para = makeParagraphFn.invoke();
				editor.insertParagraphIntoBlockAfter( block, para, paragraphBefore );
			}
			return EditFilter.HandleEditResult.HANDLED;
		}
	}


	
	protected static class DeleteParagraphRequest extends RichTextRequest
	{
		private Object paragraph = null;
		
		public DeleteParagraphRequest()
		{
		}

		@Override
		protected EditFilter.HandleEditResult invokeOnParagraphListItem(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object paragraph, RichString value)
		{
			this.paragraph = paragraph;
			return EditFilter.HandleEditResult.PASS_TO_PARENT;
		}
	
		@Override
		protected EditFilter.HandleEditResult invokeOnBlock(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object block, RichString value)
		{
			if ( paragraph != null )
			{
				editor.deleteParagraphFromBlock( block, paragraph );
			}
			return EditFilter.HandleEditResult.HANDLED;
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
		protected EditFilter.HandleEditResult invokeOnParagraph(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object paragraph, RichString value)
		{
			Object embedValue = makeInlineEmbedFn.invoke();
			EdInlineEmbed embed = new EdInlineEmbed( embedValue );
			editor.insertInlineEmbed( fragment.getView().getLog(), element, paragraph, marker, embed );
			return EditFilter.HandleEditResult.HANDLED;
		}
	}


	
	protected static class DeleteInlineEmbedRequest extends RichTextRequest
	{
		private Object embed = null;
		
		public DeleteInlineEmbedRequest()
		{
		}

		@Override
		protected EditFilter.HandleEditResult invokeOnInlineEmbed(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object embed, RichString value)
		{
			this.embed = embed;
			return EditFilter.HandleEditResult.PASS_TO_PARENT;
		}
	
		@Override
		protected EditFilter.HandleEditResult invokeOnTextSpan(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object textSpan, RichString value)
		{
			editor.removeInlineEmbed( textSpan, embed );
			return EditFilter.HandleEditResult.HANDLED;
		}
		
		@Override
		protected EditFilter.HandleEditResult invokeOnParagraph(RichTextEditor editor, LSElement element,
				LSElement sourceElement, FragmentView fragment, Object paragraph, RichString value)
		{
			editor.removeInlineEmbed( paragraph	, embed );
			return EditFilter.HandleEditResult.HANDLED;
		}
	}
}
