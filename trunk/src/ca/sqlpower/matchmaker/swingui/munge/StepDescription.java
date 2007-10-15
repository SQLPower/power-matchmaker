/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.munge;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

/**
 * This is a class that holds all the information of a munge step. This includes
 * the name, the object class, the gui class, and the icon.
 */
public class 
StepDescription extends JComponent implements Comparable<StepDescription> {

	private static final Logger logger = Logger.getLogger(StepDescription.class);

	
	private String name;
	private Class logicClass;
	private Class guiClass;
	private Icon icon;

	public StepDescription() {
	}
	
	public void setProperty(String property, String value) throws ClassNotFoundException {
		if (property.equals("name")) {
			setName(value);
		} else if (property.equals("logic")) {
			setLogicClass(Class.forName(value));
		} else if (property.equals("gui")) {
			setGuiClass(Class.forName(value));
		} else if (property.equals("icon")) {
			if(value.equals("")){
				setIcon(null);
			} else if (ClassLoader.getSystemResource(value) != null) {
				setIcon(new ImageIcon(ClassLoader.getSystemResource(value)));
			} else if (new File(value).exists()) {
				setIcon(new ImageIcon(value));
			} else {
				setIcon(null);
			}
		}
			
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Class getLogicClass() {
		return logicClass;
	}
	
	public void setLogicClass(Class logicClass) {
		this.logicClass = logicClass;
	}
	
	public Class getGuiClass() {
		return guiClass;
	}
	
	public void setGuiClass(Class guiClass) {
		this.guiClass = guiClass;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public void setIcon(Icon icon) {
		this.icon = icon;
	}
	
	public int compareTo(StepDescription o) {
		return getName().compareTo(o.getName());
	}
}
