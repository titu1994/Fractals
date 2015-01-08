package com.fractals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.attribute.standard.DateTimeAtCompleted;

import org.apache.commons.math3.complex.Complex;

public class MendelbrottFractal {
	// Setup
	static MendelbrottFractal fractal;
	private static BufferedImage outputImg;
	private static Graphics g;
	
	//Initialize Variables
	private static final int MAP_AREA = 2; // Square this value to calculate the map area to which
										   // the color will be mapped. Larger than 2 reduces sharpness. Lesser than 2 causes too many computations

	// Colors
	private static final Color white = Color.WHITE;
	private static final int MAX_COLORS = 256;

	public static void main(String args[]) {
		fractal = new MendelbrottFractal();
		outputImg = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
		g = outputImg.getGraphics();
		
		fractal.clearBackground();
		fractal.MendelBrot();
		fractal.saveFile();
	}

	private void clearBackground() {
		System.out.println("Cleared Background");
		g.setColor(white);
		g.fillRect(0, 0, 400, 400);
		
	}

	private void mapCoords(float x, float y, int color) {
		double ratio = outputImg.getWidth() / 2;

		int left = (int) (x * ratio + ratio);
		int top = (int) (y * ratio + ratio);

		g.setColor(new Color(color * 65535));
		g.fillRect(left, top, MAP_AREA, MAP_AREA);
	}

	public void MendelBrot() {
		System.out.println("Calculating Mendlebrott");
		float epsilon = 0.001f; // The step size across the X and Y axis
		float x = 0;
		float y = 0;
		int maxIterations = 10000; // increasing this will give you a more detailed fractal but increase computation time

		Complex Z;
		Complex C;
		int iterations;
		for (x = -2; x <= 2; x += epsilon) {
			System.out.println("x = " + x);
			for (y = -2; y <= 2; y += epsilon) {
				iterations = 0;
				C = new Complex(x, y);
				Z = new Complex(0, 0);
				while (Z.abs() < 2 && iterations < maxIterations) {
					Z = Z.multiply(Z);
					Z = Z.add(C);
					iterations++;
				}
				mapCoords(x, y, MAX_COLORS % iterations);
			}
		}
	}
	
	private void saveFile() {
		System.out.println("Saving File");
		String fileName = "Mendelbrott";
		File f = new File(fileName + System.currentTimeMillis()/1000 + ".png");
		try {
			ImageIO.write(outputImg, "png", f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("File Saved");
	}

}
