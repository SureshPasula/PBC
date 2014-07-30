/*
Sample program for use with IBM Integration Bus
© Copyright International Business Machines Corporation 2013
Licensed Materials - Property of IBM
*/

package com.ibm.etools.mft.samples.scribble.actions;
import org.eclipse.help.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

import com.ibm.etools.mft.samples.scribble.*;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class ScribblePublisherAction implements IWorkbenchWindowActionDelegate, ILiveHelpAction
{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public ScribblePublisherAction() {
	}

	public void setInitializationString (String data)
	{
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow ();
	}

	public void run ()
	{
		run (null);
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run (IAction action) 
	{
		// Active help does not run on the UI thread, so we must use syncExec

		Display.getDefault ().syncExec (new Runnable () 
		{
			public void run ()
			{
				if (window != null)
				{
					Shell shell = window.getShell ();
					
					new ScribblePublisher (shell).open ();
					
					shell.setActive ();
				}
				else
				{
					new ScribblePublisher (null).open ();
				}
			}
		});	
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
