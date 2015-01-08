package com.fractals;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TextField;

/**
 * Applet implementation of the Koch snowflake.
 * This isn't threaded, so once you start drawing it,
 * you have to wait for it to finish drawing to change the settings.
 * @version created: 2000.06.05.n1; updated: 2000.06.08.n4
 * @author  Nathan Bronecke
 */

/*
<html><head><title>Koch Snowflake</title></head><body>
Koch Snowflake demo applet<br />
<applet code="KochSnowflake.class" width="500" height="600">You need Java 1.1.</applet>
</body></html>
 */

public class KochSnowflake extends Applet implements java.awt.event.ActionListener {

	private static final long serialVersionUID = 8302365665401708271L;
	private final static int DEPTH_DEFAULT = 4;
	private final static int DEPTH_MAX = 12;	//gets pointless over 8

	private final static double cos60 = 0.5;	//value of cos(60 degrees)
	private final static double sin60 = Math.sqrt(3)*0.5;	//value of sin(60 degrees)
	private final static double value2root3over3 = Math.sqrt(3)*2/3;

	private int curDepth = DEPTH_DEFAULT;

	private boolean doLines = true;
	private boolean doDots = false;
	private boolean doBoundingRect = false;

	private final Button bDraw = new Button("Draw");
	private final TextField tfIterations = new TextField("" + DEPTH_DEFAULT,3);
	private final Checkbox cbLines = new Checkbox("Lines",doLines);
	private final Checkbox cbDots = new Checkbox("Dots",doDots);
	private final Checkbox cbBoundingRect = new Checkbox("Bounding Rectangle",doBoundingRect);

	private boolean infoShown = false;
	/** component that displays the snowflake */
	private final Panel pGfx = new Panel() {
		/**
		 * @version created: 2000.06.06.n2; updated: 2000.06.06.n2
		 */
		public void paint(Graphics g) {
			drawPicture(g,new Rectangle(this.getSize()));
		}
	};

	/**
	 * @version created: 2000.06.05.n1; updated: 2000.06.07.n3
	 */
	public void init() {
		Panel pControls = new Panel();
		pControls.setLayout(new FlowLayout());
		pControls.add(bDraw);
		pControls.add(new Panel());	//just a spacer
		pControls.add(tfIterations);
		pControls.add(new Label("Iterations"));
		pControls.add(cbLines);
		pControls.add(cbDots);
		pControls.add(cbBoundingRect);

		setLayout(new BorderLayout());
		add(pGfx,BorderLayout.CENTER);
		add(pControls,BorderLayout.SOUTH);

		bDraw.addActionListener(this);
	}


	/**
	 * Draws one line segment.
	 * @param   depth	current recursion depth; when 0, draws a line and just returns
	 * @version created: 2000.06.06.n2; updated: 2000.06.06.n2
	 */
	public void drawSegment(Graphics g, Point a, Point b, int depth) {

		//done recursion
		if(depth==0) {
			if(doLines)
				g.drawLine(a.x,a.y, b.x,b.y);
			if(doDots)
				g.drawOval(a.x,a.y, 1,1);

			//compute next line parts
		} else {

			//compute points
			final Point distance = new Point( (b.x-a.x)/3, (b.y-a.y)/3 );
			final Point pa = new Point( a.x+distance.x, a.y+distance.y);
			final Point pb = new Point( b.x-distance.x, b.y-distance.y);
			final Point pTip = new Point(
					pa.x + (int)(distance.x*cos60 + distance.y*sin60),
					pa.y + (int)(distance.y*cos60 - distance.x*sin60)
					);

			//draw line segments
			final int newDepth = depth-1;
			drawSegment(g, a,pa ,newDepth);
			drawSegment(g, pa,pTip ,newDepth);
			drawSegment(g, pTip,pb ,newDepth);
			drawSegment(g, pb,b ,newDepth);
		}

	}

	/**
	 * Draws a koch snowflake on the given graphics surface within the given bounds
	 * using settings set by widgets. Note that the settings are only updated when
	 * the "Draw" button is pressed.
	 * @version created: 2000.06.06.n2; updated: 2000.06.08.n4
	 */
	public void drawPicture(Graphics g, Rectangle bounds) {

		//fill in background
		g.setColor(Color.white);
		g.fillRect(bounds.x,bounds.y, bounds.width,bounds.height);

		//draw box around edge of drawing area
		if(false) {
			g.setColor(Color.blue);
			g.drawRect(bounds.x,bounds.y, bounds.width-1,bounds.height-1);
		}

		//way too small to show
		if( (bounds.width<2) || (bounds.height<2) )
			return;
		//nothing to show
		if(!doLines && !doDots)
			return;

		//find working width and height
		//
		//	if we're assuming starting triangle is equilateral (if it isn't
		//	equilateral, things look ugly), if this is the original shape:
		//	   _______
		//	   \     /
		//	    \   /
		//	     \ /
		//	      ^
		//	and this is second shape (without the inside lines removed) :
		//	                _
		//	      v          |
		//	   __/_\__       |
		//	   \     /       | height
		//	  /_\   /_\      |
		//	     \ /         |
		//	      ^         -+
		//	 |_________|
		//	    width
		//
		//	then the width is equal to 2 root 3 over 3, that is,
		//	(2 times the square root of 3) divided by 3
		//	(this ratio can be derived using geometry of 30-60-90 triangles)
		//	Using this, this part determines the maximum height and
		//	width to use in a given viewing rectangle.
		//
		//	(this formula stuff by NPB)
		//
		final double w,h;
		if(bounds.height < (int)(bounds.width * value2root3over3)) {
			//height is limiting dimension
			h = bounds.height;
			w = h / value2root3over3;
		} else {
			//width is limiting dimension
			w = bounds.width;
			h = w * value2root3over3;
		}

		//determine starting points
		//
		//	 1 _______ 2
		//	   \     /
		//	    \   /
		//	     \ /
		//	      ^ 3
		//
		//	horizontal and vertical location is also centered within the given viewing rectangle
		//
		final int top = bounds.y + (int)((bounds.height-h)*0.5 + (h*0.25));
		final Point p1 = new Point(bounds.x + (int)((bounds.width-w)*0.5), top);
		final Point p2 = new Point(bounds.x + (int)((bounds.width+w)*0.5), top);
		final Point p3 = new Point(
				bounds.x + (bounds.width>>1),
				bounds.y + (int)((bounds.height+h)*0.5)
				);

		//draw snowflake's bounding box
		//	the right/bottom edges may be off a little bit, due to rounding and/or
		//	the way different JVM implementations deal with java.awt.Graphics
		if(doBoundingRect) {
			g.setColor(Color.gray);
			g.drawRect(
					bounds.x+(int)((bounds.width-w)*0.5),
					bounds.y+(int)((bounds.height-h)*0.5),
					(int)w-1,(int)h-1
					);
		}

		//draw recursive line segments
		g.setColor(Color.black);
		drawSegment(g, p1,p2, curDepth);
		drawSegment(g, p2,p3, curDepth);
		drawSegment(g, p3,p1, curDepth);

	}

	/**
	 * @version created: 2000.06.06.n2; updated: 2000.06.07.n3
	 */
	public void actionPerformed(java.awt.event.ActionEvent ae) {
		java.lang.Object o = ae.getSource();

		if(o==bDraw) {
			if(!infoShown) {
				infoShown=true;
				System.out.println(
						"\nKochSnowflake source available at\n" +
								"http://www.everything2.com/index.pl?node_id=598735\n"
						);
			}
			try {
				curDepth = Integer.parseInt(tfIterations.getText());
			} catch(NumberFormatException e) {
				curDepth = DEPTH_DEFAULT;
			}
			if(curDepth < 0)
				curDepth = 0;
			else if(curDepth > DEPTH_MAX)
				curDepth = DEPTH_MAX;
			tfIterations.setText("" + curDepth);
			doLines = cbLines.getState();
			doDots = cbDots.getState();
			doBoundingRect = cbBoundingRect.getState();
			drawPicture(pGfx.getGraphics(),new Rectangle(pGfx.getSize()));
		}
	}

}