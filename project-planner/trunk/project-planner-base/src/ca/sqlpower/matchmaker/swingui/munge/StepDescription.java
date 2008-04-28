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

import ca.sqlpower.matchmaker.munge.MungeStep;

/**
 * This is a class that holds all the information of a munge step. This includes
 * the name, the object class, the gui class, and the icon.
 */
public class 
StepDescription extends JComponent implements Comparable<StepDescription> {

	private static final Logger logger = Logger.getLogger(StepDescription.class);

	private static final String DEFAULT_ICON = "icons/famfamfam/color_wheel.png";

	private static final String DEFAULT_MAIN_ICON = "icons/wt_sqlp_logo.png";
	
	/**
     * The name of the type of step this description represents.
	 */
	private String name;
    
    /**
     * The class that implements the actual munging part of this step type.
     */
	private Class<? extends MungeStep> logicClass;
    
    /**
     * The class that implements the user interface for this type of step.
     */
	private Class<? extends AbstractMungeComponent> guiClass;
    
    /**
     * The icon that visually represents this type of step.
     */
	private Icon icon;
	
	/**
	 * The icon that will appear in the munge component itself when an image
	 * on the munge component is desired.
	 */
	private Icon mainIcon;
	
	/**
	 * The category that this step belongs to. The categories will allow grouping
	 * multiple steps into one folder in the step library. All steps with the
	 * same category name will be in the same group.
	 */
	private String category;
	
	/**
	 * A description to be displayed to the user for this step.
	 */
	private String description;

	/**
	 * a URL of a web page that gives more information about a step to 
	 * the user.
	 */
	private String infoURL;

    /**
     * Creates a new step description that is invalid (its properties all start
     * off null).
     */
	public StepDescription() {
	}
	
    /**
     * Interprets the given (name, value) pair according to special meanings that
     * are attached to the name. Recognized names are:
     * 
     * <dl>
     *  <dt>name  <dd>The name for this type of step
     *  <dt>logic <dd>The fully-qualified Java class name implementing the step's logic.
     *                This class must be available in the System Classloader, and must
     *                implement the {@link MungeStep} interface.
     *  <dt>gui   <dd>The class that implements this step type's graphical user interface.
     *                This class must be available in the System Classloader, and must
     *                be a subclass of {@link AbstractMungeComponent}.
     *  <dt>icon  <dd>The name of a resource available on the system classpath, or the
     *                path name of a file that resolves correctly when given to the {@link File}
     *                constructor.  If this value does not exist as a system resource or a file,
     *                a default icon will be used instead. This icon is the smaller icon that 
     *                appears in the right hand list of munge steps.
     *  <dt>mainIcon<dd>The name of a resource available on the system classpath, or the
     *                path name of a file that resolves correctly when given to the {@link File}
     *                constructor.  If this value does not exist as a system resource or a file,
     *                a default icon will be used instead. This is a larger icon that will be displayed
     *                on a munge step if it is needed.
     *  <dt>category<dd>The name of the category this step belongs to. Categories with the same
     *  			  name will be grouped together in the library of steps in the right hand
     *  			  side of the munge editor.
     *  <dt>description<dd>The description of this step to be displayed to users.
     *  <dt>infoURL<dd>a URL of a web page that gives more information about a step to 
	 * 				  the user.
     * </dl>
     * 
     * @param property The property name. Recognized property names are enumerated above;
     * unrecognized property names are skipped.
     * @param value The property value.  The meaning of the value depends on the property
     * name.  See above for details.
     * @throws ClassNotFoundException If the given property represents a Java class, and that
     * class cannot be loaded.
     */
	public void setProperty(String property, String value) throws ClassNotFoundException {
		if (property.equals("name")) {
			setName(value);
		} else if (property.equals("logic")) {
            Class<?> logicClass = Class.forName(value);
			setLogicClass(logicClass.asSubclass(MungeStep.class));
		} else if (property.equals("gui")) {
            Class<?> guiClass = Class.forName(value);
			setGuiClass(guiClass.asSubclass(AbstractMungeComponent.class));
		} else if (property.equals("icon")) {
		    if (!value.equals("") && getClass().getClassLoader().getResource(value) != null) {
				setIcon(new ImageIcon(getClass().getClassLoader().getResource(value)));
			} else if (!value.equals("") && new File(value).exists()) {
				setIcon(new ImageIcon(value));
			} else {
				logger.debug("Could not find icon resource " + value);
				setIcon(new ImageIcon(getClass().getClassLoader().getResource(DEFAULT_ICON)));
			}
		} else if (property.equals("mainIcon")) {
			logger.debug("An icon was set for " + getName() + " with icon value " + value);
			if (!value.equals("") && getClass().getClassLoader().getResource(value) != null) {
				setMainIcon(new ImageIcon(getClass().getClassLoader().getResource(value)));
			} else if (!value.equals("") && new File(value).exists()) {
				setMainIcon(new ImageIcon(value));
			} else {
				logger.debug("Could not find icon resource " + value);
				setMainIcon(new ImageIcon(getClass().getClassLoader().getResource(DEFAULT_MAIN_ICON)));
			}
			logger.debug("Main icon is now " + getMainIcon());
		} else if (property.equals("category")) {
			setCategory(value);
		} else if (property.equals("description")) {
			setDescription(value);
		} else if (property.equals("infoURL")) {
			setInfoURL(value);
		} else {
		    logger.info("Skipping unknown step description property: " + property);
        }
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Class<? extends MungeStep> getLogicClass() {
		return logicClass;
	}
	
	public void setLogicClass(Class<? extends MungeStep> logicClass) {
		this.logicClass = logicClass;
	}
	
	public Class<? extends AbstractMungeComponent> getGuiClass() {
		return guiClass;
	}
	
	public void setGuiClass(Class<? extends AbstractMungeComponent> guiClass) {
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

	public Icon getMainIcon() {
		return mainIcon;
	}

	public void setMainIcon(Icon mainIcon) {
		this.mainIcon = mainIcon;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getInfoURL() {
		return infoURL;
	}

	public void setInfoURL(String infoURL) {
		this.infoURL = infoURL;
	}
}
