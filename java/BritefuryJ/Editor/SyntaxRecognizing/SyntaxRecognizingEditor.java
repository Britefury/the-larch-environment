//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import java.awt.datatransfer.DataFlavor;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.Editor.Sequential.SequentialEditor;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Utils.HashUtils;

public abstract class SyntaxRecognizingEditor extends SequentialEditor
{
	public static interface CommitFn
	{
		void commit(Object model, Object newValue);
	}
	
	
	private class ParsingNodeEditListener extends ParsingEditListener
	{
		private CommitFn commit;
		private String logName;
		
		
		public ParsingNodeEditListener(ParserExpression parser, CommitFn commit, String logName)
		{
			super( parser );
			
			this.commit = commit;
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
		protected HandleEditResult handleParseSuccess(DPElement element, DPElement sourceElement,
				GSymFragmentView fragment, EditEvent event, Object model, StreamValue value,
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
			if ( x instanceof ParsingNodeEditListener )
			{
				ParsingNodeEditListener px = (ParsingNodeEditListener)x;
				
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
	
	
	
	private class PartialParsingNodeEditListener extends PartialParsingEditListener
	{
		private String logName;
		
		
		public PartialParsingNodeEditListener(ParserExpression parser, String logName)
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
		public boolean testValue(StreamValue value);
	}
	
	public interface UnparseableCommitFn
	{
		public void commit(Object model, StreamValue stream);
	}
	
	
	private class UnparsedNodeEditListener extends UnparsedEditListener
	{
		private String logName;
		private UnparseableContentTest test;
		private UnparseableCommitFn commit;
		
		
		public UnparsedNodeEditListener(UnparseableContentTest test, UnparseableCommitFn commit, String logName)
		{
			this.test = test;
			this.commit = commit;
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


		protected boolean isValueValid(DPElement element, DPElement sourceElement, GSymFragmentView fragment, EditEvent event, Object model, StreamValue value)
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
		protected HandleEditResult handleUnparsed(DPElement element, DPElement sourceElement, GSymFragmentView fragment, EditEvent event, Object model, StreamValue value)
		{
			commit.commit( model, value );
			return HandleEditResult.HANDLED;
		}
	}

	
	
	
	private class TopLevelNodeEditListener extends TopLevelEditListener
	{
		public TopLevelNodeEditListener()
		{
		}
		
		
		@Override
		protected SyntaxRecognizingEditor getSyntaxRecognizingEditor()
		{
			return SyntaxRecognizingEditor.this;
		}
	}

	
	
	
	private static Pattern whitespace = Pattern.compile( "[ ]+" );
	
	private WeakHashMap<ParsingNodeEditListener, WeakReference<ParsingNodeEditListener>> parsingCache =
		new WeakHashMap<ParsingNodeEditListener, WeakReference<ParsingNodeEditListener>>();
	private WeakHashMap<PartialParsingNodeEditListener, WeakReference<PartialParsingNodeEditListener>> partialParsingCache =
		new WeakHashMap<PartialParsingNodeEditListener, WeakReference<PartialParsingNodeEditListener>>();
	private WeakHashMap<UnparsedNodeEditListener, WeakReference<UnparsedNodeEditListener>> unparsedCache =
		new WeakHashMap<UnparsedNodeEditListener, WeakReference<UnparsedNodeEditListener>>();
	private TopLevelNodeEditListener cachedTopLevel = new TopLevelNodeEditListener();
	
	
	public SyntaxRecognizingEditor()
	{
		super();
	}
	
	public SyntaxRecognizingEditor(DataFlavor bufferFlavor)
	{
		super( bufferFlavor );
	}
	
	
	
	public ParsingEditListener parsingNodeEditListener(String logName, ParserExpression parser, CommitFn commit)
	{
		ParsingNodeEditListener listener = new ParsingNodeEditListener( parser, commit, logName );
		WeakReference<ParsingNodeEditListener> x = parsingCache.get( listener );
		if ( x == null  ||  x.get() == null )
		{
			parsingCache.put( listener, new WeakReference<ParsingNodeEditListener>( listener ) );
			return listener;
		}
		else
		{
			return x.get();
		}
	}
	
	
	
	public PartialParsingEditListener partialParsingNodeEditListener(String logName, ParserExpression parser)
	{
		PartialParsingNodeEditListener listener = new PartialParsingNodeEditListener( parser, logName );
		WeakReference<PartialParsingNodeEditListener> x = partialParsingCache.get( listener );
		if ( x == null  ||  x.get() == null )
		{
			partialParsingCache.put( listener, new WeakReference<PartialParsingNodeEditListener>( listener ) );
			return listener;
		}
		else
		{
			return x.get();
		}
	}
	
	
	
	public UnparsedEditListener unparsedNodeEditListener(String logName, UnparseableContentTest test, UnparseableCommitFn commit)
	{
		UnparsedNodeEditListener listener = new UnparsedNodeEditListener( test, commit, logName );
		WeakReference<UnparsedNodeEditListener> x = unparsedCache.get( listener );
		if ( x == null  ||  x.get() == null )
		{
			unparsedCache.put( listener, new WeakReference<UnparsedNodeEditListener>( listener ) );
			return listener;
		}
		else
		{
			return x.get();
		}
	}
	
	
	public TopLevelNodeEditListener topLevelNodeEditListener()
	{
		return cachedTopLevel;
	}
	
	
	
	protected boolean isValueEmpty(StreamValue value)
	{
		return value.isTextual()  &&  whitespace.matcher( value.textualValue() ).matches();
	}
}
