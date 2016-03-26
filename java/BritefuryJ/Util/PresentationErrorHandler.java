//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util;

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.IncrementalView;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.Pres.Pres;

public abstract class PresentationErrorHandler {
	private static PresentationErrorHandler errorHandler;


	public static PresentationErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public static void setErrorHandler(PresentationErrorHandler handler) {
		errorHandler = handler;
	}

	public static boolean hasErrorHandler() {
		return errorHandler != null;
	}


	public abstract Pres handlePresentationError(IncrementalView incView, FragmentView fragmentView, Throwable exception, Pres exceptionPresentation);
	public abstract Pres handlePresentationRealisationError(IncrementalView incView, FragmentView fragmentView, Throwable exception, Pres exceptionPresentation);
	public abstract void notifyExceptionDuringElementInteractor(LSElement element, Object interactor, String event, Throwable error);
	public abstract void notifyExceptionDuringClipboardOperation(LSRegion region, Object handler, String event, Throwable error);
	public abstract void notifyExceptionDuringInvokeLater(Throwable error);
}
