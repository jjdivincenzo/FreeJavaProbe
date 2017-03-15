package edu.regis.jprobe.ui;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JPanel;

import edu.regis.jprobe.model.Logger;

public abstract class PerformancePanel extends JPanel implements Printable {

	
	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;
	private int maxPageNumber = 1;
			
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if (pageIndex >= maxPageNumber)
			return NO_SUCH_PAGE;

		/*graphics.translate((int)pageFormat.getImageableX(),
			(int)pageFormat.getImageableY());
		int wPage = (int)pageFormat.getImageableWidth();
		int hPage = (int)pageFormat.getImageableHeight();

		int w = bImage.getWidth(this);
		int h = bImage.getHeight(this);
		if (w == 0 || h == 0)
			return NO_SUCH_PAGE;
		int nCol = Math.max((int)Math.ceil((double)w/wPage), 1);
			int nRow = Math.max((int)Math.ceil((double)h/hPage), 1);
			maxPageNumber = nCol*nRow;

		int iCol = pageIndex % nCol;
		int iRow = pageIndex / nCol;
		int x = iCol*wPage;
		int y = iRow*hPage;
		int wImage = Math.min(wPage, w-x);
		int hImage = Math.min(hPage, h-y);

		graphics.drawImage(bImage, 0, 0, wImage, hImage,
			x, y, x+wImage, y+hImage, this);
		System.gc();*/
		Logger.getLogger().debug("Calling Print for " + this.getClass().getName());
		return PAGE_EXISTS;
	}
	/**
	 * 
	 * @param size
	 */
	public void setSampleSize(int size) {
		//Dummy Method, override to implement
		
	}
}
