package Britefury.Cell;


public interface CellOwner {
	public void onCellEvaluator(CellInterface cell, CellEvaluator oldEval, CellEvaluator newEval);
}
