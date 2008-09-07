package BritefuryJ.Cell;

public abstract class CellEvaluator
{
	public abstract Object evaluate();
	
	public boolean isLiteral()
	{
		return false;
	}
}
