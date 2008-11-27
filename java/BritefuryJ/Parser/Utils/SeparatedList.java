//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Parser.OneOrMore;
import BritefuryJ.Parser.Optional;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Suppress;
import BritefuryJ.Parser.ZeroOrMore;
import BritefuryJ.Parser.ParserExpression.ParserCoerceException;

public class SeparatedList
{
	public static class EmptyListAction implements ParseAction
	{
		public Object invoke(String input, int begin, Object x)
		{
			return new ArrayList<Object>();
		}
	}

	public static class SeparatedListAction implements ParseAction
	{
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			if ( x == null )
			{
				return new ArrayList<Object>();
			}
			else
			{
				ArrayList<Object> result = new ArrayList<Object>();
				List<Object> xs = (List<Object>)x;
				result.add( xs.get( 0 ) );
				
				for (Object a: (List<Object>)xs.get( 1 ))
				{
					result.add( ((List<Object>)a).get( 1 ) );
				}
					
				return result;
			}
		}
	}

	private static class SeparatedListActionOneOrMore implements ParseAction
	{
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			ArrayList<Object> result = new ArrayList<Object>();
			List<Object> xs = (List<Object>)x;
			result.add( xs.get( 0 ) );
			
			for (Object a: (List<Object>)xs.get( 1 ))
			{
				result.add( ((List<Object>)a).get( 1 ) );
			}
				
			return result;
		}
	}
	
	private static class DelimitedListAction implements ParseAction
	{
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			return xs.get( 1 );
		}
	}
	
	
	/**
	 * Creates a parser expression that will match a separated list.
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, new Literal( "," ), false, false, false )<br>
	 * Will construct a parser that will parse the string "a, b, c, d" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	separator							a parser expression that parses a separator that separates elements in the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 */
	public static ParserExpression separatedList(ParserExpression subexp, ParserExpression separator, boolean bNeedAtLeastOne,
			boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		ParserExpression p;
		
		if ( bRequireTrailingSeparatorForLengthOne )
		{
			ParserExpression afterOne = new OneOrMore( separator.__add__( subexp ) );
			if ( bAllowTrailingSeparator )
			{
				afterOne = afterOne.__sub__( new Suppress( new Optional( separator ) ) );
			}
			p = subexp.__add__( afterOne.__or__( separator.action( new EmptyListAction() ) ) );
		}
		else
		{
			p = subexp.__add__( new ZeroOrMore( separator.__add__( subexp ) ) );
			if ( bAllowTrailingSeparator )
			{
				p = p.__add__( new Suppress( new Optional( separator ) ) );
			}
		}
			
			
		if ( bNeedAtLeastOne )
		{
			return p.action( new SeparatedListActionOneOrMore() );
		}
		else
		{
			return new Optional( p ).action( new SeparatedListAction() );
		}
	}



	/**
	 * Creates a parser expression that will match a separated list.
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, ",", false, false, false )<br>
	 * Will construct a parser that will parse the string "a, b, c, d" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	separator							a string that is the separator that separates the elements in the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 */
	public static ParserExpression separatedList(ParserExpression subexp, String separator, boolean bNeedAtLeastOne,
			boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		return separatedList( subexp, ParserExpression.coerce( separator ), bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne ); 
	}




	/**
	 * Creates a parser expression that will match a separated list, with Literal( "," ) used as the separator.
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, ",", false, false, false )<br>
	 * Will construct a parser that will parse the string "a, b, c, d" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 */
	public static ParserExpression separatedList(ParserExpression subexp, boolean bNeedAtLeastOne, boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		return separatedList( subexp, ",", bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne ); 
	}

	
	
	


	/**
	 * Creates a parser expression that will match a delimited separated list.
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, new Literal( "{" ), new Literal( "}" ), new Literal( "," ), false, false, false )<br>
	 * Will construct a parser that will parse the string "{ a, b, c, d }" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	beginDelim						a parser expression that parses the delimiter that marks the beginning of the list
	 *  @param	endDelim							a parser expression that parses the delimiter that marks the end of the list
	 *  @param	separator							a parser expression that parses a separator that separates elements in the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 */
	public static ParserExpression delimitedSeparatedList(ParserExpression subexp, ParserExpression beginDelim, ParserExpression endDelim, ParserExpression separator,
			boolean bNeedAtLeastOne, boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		return beginDelim.__add__( separatedList( subexp, separator, bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne ) ).__add__( endDelim ).
				action( new DelimitedListAction() );
	}



	/**
	 * Creates a parser expression that will match a delimited separated list.
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, new Literal( "{" ), new Literal( "}" ), new Literal( "," ), false, false, false )<br>
	 * Will construct a parser that will parse the string "{ a, b, c, d }" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	beginDelim						a parser expression that parses the delimiter that marks the beginning of the list
	 *  @param	endDelim							a parser expression that parses the delimiter that marks the end of the list
	 *  @param	separator							a string that is the separator that separates the elements in the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 * @throws ParserCoerceException 
	 */
	public static ParserExpression delimitedSeparatedList(ParserExpression subexp, ParserExpression beginDelim, ParserExpression endDelim, String separator,
			boolean bNeedAtLeastOne, boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		return beginDelim.__add__( separatedList( subexp, separator, bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne ) ).__add__( endDelim ).
				action( new DelimitedListAction() );
	}




	/**
	 * Creates a parser expression that will match a delimited separated list, with Literal( "," ) used as the separator
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, new Literal( "{" ), new Literal( "}" ), new Literal( "," ), false, false, false )<br>
	 * Will construct a parser that will parse the string "{ a, b, c, d }" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	beginDelim						a parser expression that parses the delimiter that marks the beginning of the list
	 *  @param	endDelim							a parser expression that parses the delimiter that marks the end of the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 * @throws ParserCoerceException 
	 */
	public static ParserExpression delimitedSeparatedList(ParserExpression subexp, ParserExpression beginDelim, ParserExpression endDelim,
			boolean bNeedAtLeastOne, boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		return beginDelim.__add__( separatedList( subexp, ",", bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne ) ).__add__( endDelim ).
				action( new DelimitedListAction() );
	}




	/**
	 * Creates a parser expression that will match a delimited separated list.
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, new Literal( "{" ), new Literal( "}" ), new Literal( "," ), false, false, false )<br>
	 * Will construct a parser that will parse the string "{ a, b, c, d }" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	beginDelim						a string that is the delimiter that marks the beginning of the list
	 *  @param	endDelim							a string that is the delimiter that marks the end of the list
	 *  @param	separator							a parser expression that parses a separator that separates elements in the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 */
	public static ParserExpression delimitedSeparatedList(ParserExpression subexp, String beginDelim, String endDelim, ParserExpression separator,
			boolean bNeedAtLeastOne, boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		return ParserExpression.coerce( beginDelim ).__add__( separatedList( subexp, separator, bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne ) ).
				__add__( ParserExpression.coerce( endDelim ) ).action( new DelimitedListAction() );
	}

	
	
	
	/**
	 * Creates a parser expression that will match a delimited separated list.
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, new Literal( "{" ), new Literal( "}" ), new Literal( "," ), false, false, false )<br>
	 * Will construct a parser that will parse the string "{ a, b, c, d }" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	beginDelim						a string that is the delimiter that marks the beginning of the list
	 *  @param	endDelim							a string that is the delimiter that marks the end of the list
	 *  @param	separator							a string that is the separator that separates the elements in the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 */
	public static ParserExpression delimitedSeparatedList(ParserExpression subexp, String beginDelim, String endDelim, String separator,
			boolean bNeedAtLeastOne, boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		return ParserExpression.coerce( beginDelim ).__add__( separatedList( subexp, separator, bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne ) ).
				__add__( ParserExpression.coerce( endDelim ) ).action( new DelimitedListAction() );
	}




	/**
	 * Creates a parser expression that will match a delimited separated list, with Literal( "," ) used as the separator.
	 * 
	 * <p>Example:<br>
	 * SeparatedList.separatedList( Tokens.identifier, new Literal( "{" ), new Literal( "}" ), new Literal( "," ), false, false, false )<br>
	 * Will construct a parser that will parse the string "{ a, b, c, d }" to the list [ "a", "b", "c", "d" ]
	 * 
	 *  @param	subexp							a parser expression that parses a single element from the list
	 *  @param	beginDelim						a string that is the delimiter that marks the beginning of the list
	 *  @param	endDelim							a string that is the delimiter that marks the end of the list
	 *  @param	bNeedAtLeastOne					if true, only matches a list with one or more elements
	 *  @param	bAllowTrailingSeparator				if true, will match a list which contains a trailing separator  (e.g. 1,2,3,)
	 *  @param	bRequireTrailingSeparatorForLengthOne	if true, will require lists with a single element to contain a trailing separator
	 *  
	 *  @return									a parser expression that will match the specified separated list
	 */
	public static ParserExpression delimitedSeparatedList(ParserExpression subexp, String beginDelim, String endDelim,
			boolean bNeedAtLeastOne, boolean bAllowTrailingSeparator, boolean bRequireTrailingSeparatorForLengthOne)
	{
		return ParserExpression.coerce( beginDelim ).__add__( separatedList( subexp, ",", bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne ) ).
				__add__( ParserExpression.coerce( endDelim ) ).action( new DelimitedListAction() );
	}
}
