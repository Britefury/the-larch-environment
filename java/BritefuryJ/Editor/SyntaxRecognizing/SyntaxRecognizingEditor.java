//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import BritefuryJ.Editor.Sequential.SequentialRichStringEditor;
import BritefuryJ.Editor.SyntaxRecognizing.Precedence.PrecedenceHandler;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Util.HashUtils;
import BritefuryJ.Util.RichString.RichString;

public abstract class SyntaxRecognizingEditor extends SequentialRichStringEditor
{
	public static enum EditMode
	{
		DISPLAY,
		EDIT
	}
	

	public static interface CommitFn
	{
		void commit(Object model, Object newValue);
	}
	
	
	private class ParsingNodeEditFilter extends ParsingEditFilter
	{
		private CommitFn commit, emptyCommit;
		private String logName;
		
		
		public ParsingNodeEditFilter(ParserExpression parser, CommitFn commit, CommitFn emptyCommit, String logName)
		{
			super( parser );
			
			this.commit = commit;
			this.emptyCommit = emptyCommit;
			this.logName = logName;
		}
		
		
		@Override
		protected SyntaxRecognizingEditor getSyntaxRecognizingEditor()
		{
			return SyntaxRecognizingEditor.this;
		}
		
		@Override
		public String getLogName()
		{
			return logName;
		}

		
		@Override
		protected HandleEditResult handleEmptyValue(LSElement element, FragmentView fragment, EditEvent event, Object model)
		{
			if ( emptyCommit == null )
			{
				return HandleEditResult.NOT_HANDLED;
			}
			else
			{
				emptyCommit.commit( model, null );
				return HandleEditResult.HANDLED;
			}
		}

		@Override
		protected HandleEditResult handleParseSuccess(LSElement element, LSElement sourceElement,
				FragmentView fragment, EditEvent event, Object model, RichString value,
				Object parsed)
		{
			if ( parsed.equals( model ) )
			{
				return HandleEditResult.NO_CHANGE;
			}
			else
			{
				commit.commit( model, parsed );
				return HandleEditResult.HANDLED;
			}
		}

		public boolean equals(Object x)
		{
			if ( x instanceof ParsingNodeEditFilter )
			{
				ParsingNodeEditFilter px = (ParsingNodeEditFilter)x;
				
				return parser.equals( px.parser )  &&  commit.equals( px.commit )  && logName.equals( px.logName );  
			}
			else
			{
				return false;
			}
		}
		
		public int hashCode()
		{
			return HashUtils.tripleHash( parser.hashCode(), commit.hashCode(), logName != null  ?  logName.hashCode()  :  0 );
		}
	}
	
	
	
	private class PartialParsingNodeEditFilter extends PartialParsingEditFilter
	{
		private String logName;
		
		
		public PartialParsingNodeEditFilter(ParserExpression parser, String logName)
		{
			super( parser );
			
			this.logName = logName;
		}
		
		
		@Override
		protected SyntaxRecognizingEditor getSyntaxRecognizingEditor()
		{
			return SyntaxRecognizingEditor.this;
		}
		
		@Override
		public String getLogName()
		{
			return logName;
		}
	}
	
	
	
	public interface UnparseableContentTest
	{
		public boolean testValue(RichString value);
	}
	
	public interface UnparseableCommitFn
	{
		public void commit(Object model, RichString richStr);
	}
	
	
	private class UnparsedNodeEditFilter extends UnparsedEditFilter
	{
		private String logName;
		private UnparseableContentTest test;
		private UnparseableCommitFn commit, innerCommit;
		
		
		public UnparsedNodeEditFilter(UnparseableContentTest test, UnparseableCommitFn commit, UnparseableCommitFn innerCommit, String logName)
		{
			this.test = test;
			this.commit = commit;
			this.innerCommit = innerCommit;
			this.logName = logName;
		}
		
		
		@Override
		protected SyntaxRecognizingEditor getSyntaxRecognizingEditor()
		{
			return SyntaxRecognizingEditor.this;
		}
		
		@Override
		public String getLogName()
		{
			return logName;
		}


		protected boolean isValueValid(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value)
		{
			if ( test != null )
			{
				return test.testValue( value );
			}
			else
			{
				return true;
			}
		}

		@Override
		protected boolean shouldApplyToInnerFragment(LSElement element, LSElement sourceElement, FragmentView fragment,
				EditEvent event, Object model, RichString value)
		{
			return innerCommit != null;
		}
		
		@Override
		protected HandleEditResult handleUnparsed(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value)
		{
			commit.commit( model, value );
			return HandleEditResult.HANDLED;
		}

		@Override
		protected HandleEditResult handleInnerUnparsed(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value)
		{
			innerCommit.commit( model, value );
			return HandleEditResult.HANDLED;
		}
	}

	
	
	
	private class TopLevelNodeEditFilter extends TopLevelEditFilter
	{
		public TopLevelNodeEditFilter()
		{
		}
		
		
		@Override
		protected SyntaxRecognizingEditor getSyntaxRecognizingEditor()
		{
			return SyntaxRecognizingEditor.this;
		}
	}

	
	
	
	private static Pattern whitespace = Pattern.compile( "[ ]+" );
	
	private WeakHashMap<ParsingNodeEditFilter, WeakReference<ParsingNodeEditFilter>> parsingCache =
		new WeakHashMap<ParsingNodeEditFilter, WeakReference<ParsingNodeEditFilter>>();
	private WeakHashMap<PartialParsingNodeEditFilter, WeakReference<PartialParsingNodeEditFilter>> partialParsingCache =
		new WeakHashMap<PartialParsingNodeEditFilter, WeakReference<PartialParsingNodeEditFilter>>();
	private WeakHashMap<UnparsedNodeEditFilter, WeakReference<UnparsedNodeEditFilter>> unparsedCache =
		new WeakHashMap<UnparsedNodeEditFilter, WeakReference<UnparsedNodeEditFilter>>();
	private TopLevelNodeEditFilter cachedTopLevel = new TopLevelNodeEditFilter();
	
	
	public SyntaxRecognizingEditor()
	{
		super();
	}
	
	
	
	public ParsingEditFilter parsingEditFilter(String logName, ParserExpression parser, CommitFn commit, CommitFn emptyCommit)
	{
		ParsingNodeEditFilter listener = new ParsingNodeEditFilter( parser, commit, emptyCommit, logName );
		WeakReference<ParsingNodeEditFilter> x = parsingCache.get( listener );
		if ( x == null  ||  x.get() == null )
		{
			parsingCache.put( listener, new WeakReference<ParsingNodeEditFilter>( listener ) );
			return listener;
		}
		else
		{
			return x.get();
		}
	}
	
	public ParsingEditFilter parsingEditFilter(String logName, ParserExpression parser, CommitFn commit)
	{
		return parsingEditFilter( logName, parser, commit, null );
	}
	
	
	
	public PartialParsingEditFilter partialParsingEditFilter(String logName, ParserExpression parser)
	{
		PartialParsingNodeEditFilter listener = new PartialParsingNodeEditFilter( parser, logName );
		WeakReference<PartialParsingNodeEditFilter> x = partialParsingCache.get( listener );
		if ( x == null  ||  x.get() == null )
		{
			partialParsingCache.put( listener, new WeakReference<PartialParsingNodeEditFilter>( listener ) );
			return listener;
		}
		else
		{
			return x.get();
		}
	}
	
	
	
	public UnparsedEditFilter unparsedEditFilter(String logName, UnparseableContentTest test, UnparseableCommitFn commit)
	{
		return unparsedEditFilter( logName, test, commit, null );
	}
	
	public UnparsedEditFilter unparsedEditFilter(String logName, UnparseableContentTest test, UnparseableCommitFn commit, UnparseableCommitFn innerCommit)
	{
		UnparsedNodeEditFilter listener = new UnparsedNodeEditFilter( test, commit, innerCommit, logName );
		WeakReference<UnparsedNodeEditFilter> x = unparsedCache.get( listener );
		if ( x == null  ||  x.get() == null )
		{
			unparsedCache.put( listener, new WeakReference<UnparsedNodeEditFilter>( listener ) );
			return listener;
		}
		else
		{
			return x.get();
		}
	}
	
	
	public TopLevelNodeEditFilter topLevelEditFilter()
	{
		return cachedTopLevel;
	}
	
	
	
	public SREditRule editRule(PrecedenceHandler precedenceHandler, List<TreeEventListener> editListeners)
	{
		return new SREditRule( this, precedenceHandler, editListeners );
	}
	
	public SREditRule editRule( List<TreeEventListener> editListeners)
	{
		return new SREditRule( this, null, editListeners );
	}
	
	
	
	public SRSoftStructuralEditRule softStructuralEditRule(PrecedenceHandler precedenceHandler, List<TreeEventListener> editListeners)
	{
		return new SRSoftStructuralEditRule( this, precedenceHandler, editListeners );
	}
	
	public SRSoftStructuralEditRule softStructuralEditRule(List<TreeEventListener> editListeners)
	{
		return new SRSoftStructuralEditRule( this, null, editListeners );
	}
	
	
	protected boolean isValueEmpty(RichString value)
	{
		return value.isTextual()  &&  whitespace.matcher( value.textualValue() ).matches();
	}
}
