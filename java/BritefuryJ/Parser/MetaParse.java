package BritefuryJ.Parser;

import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringAccessor;

import java.util.Arrays;
import java.util.List;

/**
 * Created by geoff on 23/10/15.
 */
public class MetaParse extends ParserExpression {
    protected ParserExpression subexp, metaExpr;


    public MetaParse(ParserExpression subexp, ParserExpression metaExpr) {
        this.subexp = subexp;
        this.metaExpr = metaExpr;
    }

    public MetaParse(String subexp, ParserExpression metaExpr)
    {
        this.subexp = coerce( subexp );
        this.metaExpr = metaExpr;
    }

    public MetaParse(Object subexp, Object metaExpr) throws ParserCoerceException
    {
        this.subexp = coerce( subexp );
        this.metaExpr = coerce(metaExpr);
    }



    public ParserExpression getSubExpression()
    {
        return subexp;
    }
    public ParserExpression getMetaExpression()
    {
        return metaExpr;
    }


    public List<ParserExpression> getChildren()
    {
        ParserExpression[] children = { subexp, metaExpr };
        return Arrays.asList(children);
    }

    public boolean isEquivalentTo(ParserExpression x)
    {
        if ( x instanceof MetaParse )
        {
            MetaParse mx = (MetaParse)x;
            return subexp.isEquivalentTo( mx.subexp ) && metaExpr.isEquivalentTo(mx.metaExpr);
        }
        else
        {
            return false;
        }
    }


    protected ParseResult evaluateNode(ParserState state, Object input)
    {
        ParseResult res = subexp.handleNode( state, input );

        if ( res.isValid() )
        {
            return metaEvaluate(res);
        }
        else
        {
            return res;
        }
    }

    protected ParseResult evaluateStringChars(ParserState state, String input, int start)
    {
        ParseResult res = subexp.handleStringChars( state, input, start );

        if ( res.isValid() )
        {
            return metaEvaluate(res);
        } else
        {
            return res;
        }
    }

    protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
    {
        ParseResult res = subexp.handleRichStringItems( state, input, start );

        if ( res.isValid() )
        {
            return metaEvaluate(res);
        }
        else
        {
            return res;
        }
    }

    protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
    {
        ParseResult res = subexp.handleListItems( state, input, start );

        if ( res.isValid() )
        {
            return metaEvaluate(res);
        }
        else
        {
            return res;
        }
    }


    @SuppressWarnings("unchecked")
    protected ParseResult metaEvaluate(ParseResult res) {
        ParseResult metaRes = metaEvaluateValue(res.value);
        return res.withValueFrom(metaRes);
    }


    @SuppressWarnings("unchecked")
    protected ParseResult metaEvaluateValue(Object val) {
        if (val instanceof List) {
            List<Object> xs = (List<Object>)val;
            ParseResult res = metaExpr.parseListItems(xs);
            if (res.isValid() && res.getEnd() == xs.size()) {
                return res;
            }
            else {
                return ParseResult.failure(res.getEnd());
            }
        }
        else if (val instanceof RichString) {
            RichString str = (RichString)val;
            ParseResult res = metaExpr.parseRichStringItems((RichString) val);
            if (res.isValid() && res.getEnd() == str.length()) {
                return res;
            }
            else {
                return ParseResult.failure(res.getEnd());
            }
        }
        else if (val instanceof String) {
            String str = (String)val;
            ParseResult res = metaExpr.parseStringChars(str);
            if (res.isValid() && res.getEnd() == str.length()) {
                return res;
            }
            else {
                return ParseResult.failure(res.getEnd());
            }
        }
        else {
            ParseResult res = metaExpr.parseNode(val);
            if (res.isValid() && res.getEnd() == 1) {
                return res;
            }
            else {
                return ParseResult.failure(res.getEnd());
            }
        }
    }





    public String toString()
    {
        return "MetaParse( " + subexp.toString() + ", " + metaExpr.toString() + " )";
    }
}
