/*
Sample program for use with IBM Integration Bus
© Copyright International Business Machines Corporation 2013
Licensed Materials - Property of IBM
*/

package com.ibm.etools.mft.samples.scribble;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A widget that supports scribbling. The ScribbleCanvas does its drawing in 
 * an offscreen image, and when asked to repaint, copies this image to the screen. 
 * This means that scribbles are not wiped out if the canvas is obscured by another 
 * window.
 */

public class ScribbleCanvas extends Canvas implements PaintListener
{
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2008 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	Cursor 		drawCursor, 
				normalCursor;
	Image 		backingImage;
	GC 			backingGC;
	
	public ScribbleCanvas (Composite parent, int style)
	{
		super (parent, style | SWT.NO_BACKGROUND);
		
		drawCursor = new Cursor (getDisplay (), SWT.CURSOR_CROSS);
		normalCursor = new Cursor (getDisplay (), SWT.CURSOR_ARROW);

		addPaintListener (this);
	}

	public void draw (int x1, int y1, int x2, int y2)
	{
		backingGC.drawLine (x1, y1, x2, y2);
		redraw ();
	}

	public void paintControl (PaintEvent event)
	{
		event.gc.drawImage (backingImage, 0, 0);
	}
	
	public void setBounds (int x, int y, int width, int height)
	{
		super.setBounds (x, y, width, height);
		
		Image 	image = new Image (getDisplay (), width, height);
		GC 		gc = new GC (image);
		
		gc.setForeground (getForeground ());
		gc.setBackground (getBackground ());
		gc.fillRectangle (0, 0, width, height);

		if (backingImage != null && !backingImage.isDisposed ())
		{
			gc.copyArea (backingImage, 0, 0);
			backingImage.dispose ();
		}

		if (backingGC != null && !backingGC.isDisposed ())
		{
			backingGC.dispose ();
		}

		backingImage = image;
		backingGC = gc;
	}
	
	/**
	 * The scribble canvas is a fixed size.
	 */
	
	public Point computeSize (int wHint, int hHint, boolean changed)
	{
		super.computeSize (wHint, hHint, changed);

		return new Point (300, 200);
	}
	
	public void clear ()
	{
		Rectangle bounds = backingImage.getBounds ();
		
		backingGC.setBackground (getBackground ());
		backingGC.fillRectangle (0, 0, bounds.width, bounds.height);
		redraw ();
		update ();
	}
	
	public void setEnabled (boolean enabled)
	{
		super.setEnabled (enabled);
			
		setCursor (enabled ? drawCursor : normalCursor);
	}
}
