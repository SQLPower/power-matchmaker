/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.awt.Color;
import java.awt.Point;

import ca.sqlpower.matchmaker.swingui.munge.StepDescription;

/**
 * This is the munge step for a label which can be colour coded. It also has
 * a text area that can be moved. 
 */
public class LabelMungeStep extends AbstractMungeStep {
	
	// parameter for the background colour of the step component
	private static final String COLOUR_PARAM = "stepColour";
	
	// parameter for the position of the step component relatively in the munge pen
	private static final String LAYER_PARAM = "layerPos";
	
	// the default layer of a label is set to the bottom most layer
	private static final int DEFAULT_LAYER = Integer.MIN_VALUE;
	
	// parameters that deal with the text area
	private static final String AREA_X_PARAM = "areaX";
	private static final String AREA_Y_PARAM = "areaY";
	private static final String AREA_COLOUR_PARAM = "areaColour";
	private static final String AREA_TEXT_PARAM = "areaText";
	
	public LabelMungeStep(StepDescription sd) {
		super(sd, true);
		defineIO();
        setColour(new Color(0xee, 0xee, 0xee));
        setupTextArea();
        setDefaultInputClass(String.class);
	}

    /**
     * This constructor is for the persistence layer.
     */
    @SuppressWarnings("unused")
    public LabelMungeStep() {
        super(new StepDescription(), true);
        defineIO();
    }

	private void setupTextArea() {
		setText("Enter Text Here");
		setTextAreaColour(Color.BLACK);
		setTextAreaLocation(new Point(0,0));
	}

	/**
	 * Sets the first input and output for this munge component
	 * as required to have inputs and outputs.
	 */
	private void defineIO() {
		MungeStepOutput<String> out = new MungeStepOutput<String>("output", String.class);
		addChild(out);
		InputDescriptor desc1 = new InputDescriptor("input", String.class);
		super.addInput(desc1);
	}

	public void setColour(Color c) {
		setParameter(COLOUR_PARAM, String.format("0x%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
	}
	
	public Color getColour() {
		if (getParameter(COLOUR_PARAM) == null) {
			return new Color(0xee, 0xee, 0xee);
		} else {
			return Color.decode(getParameter(COLOUR_PARAM));
		}
	}
	
	public void setLayer(int pos) {
		setParameter(LAYER_PARAM, pos);
	}
	
	public int getLayer() {
		if (getParameter(LAYER_PARAM) == null) {
			return DEFAULT_LAYER;
		} else {
			return getIntegerParameter(LAYER_PARAM);
		}
	}
	
	public void setTextAreaLocation(Point p) {
		setParameter(AREA_X_PARAM, p.x);
		setParameter(AREA_Y_PARAM, p.y);
	}
	
	public Point getTextAreaLocation() {
		Point p = new Point(0, 0);
		p.x = getIntegerParameter(AREA_X_PARAM);
		p.y = getIntegerParameter(AREA_Y_PARAM);
		return p;
	}
	
	public void setTextAreaColour(Color c) {
		setParameter(AREA_COLOUR_PARAM, String.format("0x%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
	}
	
	public Color getTextAreaColour() {
		return Color.decode(getParameter(AREA_COLOUR_PARAM));
	}
	
	public void setText(String s) {
		setParameter(AREA_TEXT_PARAM, s);
	}
	
	public String getText() {
		return getParameter(AREA_TEXT_PARAM);
	}
}
