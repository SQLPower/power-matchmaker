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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

/**
 * ConnectorIcon instances are responsible for drawing both the input and output
 * decorations of IOConnectors and MungeComponents.  This class is not instantiable
 * directly by users; rather, some static factory methods are provided which assemble
 * the various decorations from parts.  The factories cache their results, so they are
 * appropriate for calling over and over while painting (the components don't have
 * to cache the icons themselves). 
 */
public class ConnectorIcon implements Icon {

	private static  final Logger logger = org.apache.log4j.Logger.getLogger(ConnectorIcon.class); 
	
	/**
	 * The colour associated with images we want to be orange.
	 * There are external resources (images) which this particular colour
	 * choice depends on, so exercise caution when changing the value of
	 * this constant.
	 */
	private static final Color ORANGE = new Color(0xff, 0x9a, 0x00);
	
	/**
	 * The colour associated with images we want to be red.
	 * There are external resources (images) which this particular colour
	 * choice depends on, so exercise caution when changing the value of
	 * this constant.
	 */
	private static final Color RED = new Color(0xc3, 0x02, 0x02);
	
	/**
	 * The colour associated with images we want to be purple.
	 * There are external resources (images) which this particular colour
	 * choice depends on, so exercise caution when changing the value of
	 * this constant.
	 */
	private static final Color PURPLE =  new Color(0xb3, 0x52, 0xdb);
	
	/**
	 * The colour associated with images we want to be gray.
	 * There are external resources (images) which this particular colour
	 * choice depends on, so exercise caution when changing the value of
	 * this constant.
	 */
	private static final Color GRAY = new Color(0x99, 0x99, 0x99);
	
	/**
	 * The colour associated with images we want to be brown.
	 * There are external resources (images) which this particular colour
	 * choice depends on, so exercise caution when changing the value of
	 * this constant.
	 */
	private static final Color BROWN = new Color(0x53, 0x4a, 0x31);
	
	/**
	 * The colour associated with images we want to be green.
	 * There are external resources (images) which this particular colour
	 * choice depends on, so exercise caution when changing the value of
	 * this constant.
	 */
	private static final Color GREEN = new Color(0x03, 0xa7, 0x12);
	
	/**
	 * The colour associated with images we want to be blue.
	 * There are external resources (images) which this particular colour
	 * choice depends on, so exercise caution when changing the value of
	 * this constant.
	 */
	private static final Color BLUE = new Color(0x00, 0x30, 0x82);
	
	
	/**
	 * The colour associated with connections that carry String data.
	 * There are external resources (images) which this particular colour
	 * choice depends on, use only colours defined above, or add the 
	 * needed images when changing it.
	 */
	private static final Color STRING_COLOUR = ORANGE; 
	
	/**
	 * The colour associated with connections that can carry any data.
	 * There are external resources (images) which this particular colour
	 * choice depends on, use only colours defined above, or add the 
	 * needed images when changing it.
	 */
	private static final Color OBJECT_COLOUR = BROWN;
	
	/**
	 * The colour associated with connections that carry boolean data.
	 * There are external resources (images) which this particular colour
	 * choice depends on, use only colours defined above, or add the 
	 * needed images when changing it.
	 */
	private static final Color BOOLEAN_COLOUR = BLUE;
	
	/**
	 * The colour associated with connections that carry dates.
	 * There are external resources (images) which this particular colour
	 * choice depends on, use only colours defined above, or add the 
	 * needed images when changing it.
	 */
	private static final Color DATE_COLOUR = RED;
	
	/**
	 * The colour associated with inputs number data.
	 * There are external resources (images) which this particular colour
	 * choice depends on, use only colours defined above, or add the 
	 * needed images when changing it.
	 */
	private static final Color BIGDECIMAL_COLOUR = GREEN;
	
	/**
	 * The colour associated with connections that have a data type unknown
	 * to this version of the MatchMaker.
	 * There are external resources (images) which this particular colour
	 * choice depends on, use only colours defined above, or add the 
	 * needed images when changing it.
	 */
	private static final Color UNKNOWN_COLOUR = GRAY;
	
	/**
	 * The amount of space (in pixels) at the top of the nib icon that should
	 * be painted over (either by the connector handle, or by the munge component
	 * itself) before becoming visible to the user.
	 */
	public static final int NIB_OVERLAP = 2;
	
	/**
	 * Cache of icons already made by the factory method.
	 */
	private static Map<Class, ConnectorIcon> fullPlugCache = new HashMap<Class, ConnectorIcon>();
	
	/**
	 * Cache of icons already made by the factory method.
	 */
	private static Map<Class, ConnectorIcon> nibCache = new HashMap<Class, ConnectorIcon>();
	
	/**
	 * Cache of icons already made by the factory method.
	 */
	private static Map<Class, ConnectorIcon> handleCache = new HashMap<Class, ConnectorIcon>();

	/**
	 * Cache of icons already made by the factory method.
	 */
	private static Map<Class, ConnectorIcon> femaleCache = new HashMap<Class, ConnectorIcon>();

	/**
	 * Returns the 6-digit hex code of the colour associated with the given Class.
	 * 
	 * @param c The data type you want the colour code for.
	 */
	private static String hexColour(Class c) {
		return String.format("%06x", ConnectorIcon.getColor(c).getRGB() & 0xffffff);
	}
	
	/**
	 * Returns the appropriate colour for the given type.
	 * This is used to colour code lines and the IOCs.
	 * 
	 * @param c The type of connection
	 * @return The correct colour
	 */
	public static Color getColor(Class c) {
		if (c.equals(String.class)) {
			return STRING_COLOUR;
		} else if (c.equals(Boolean.class)) {
			return BOOLEAN_COLOUR;
		} else if (c.equals(BigDecimal.class)) {
			return BIGDECIMAL_COLOUR;
		} else if (c.equals(Date.class)) {
			return DATE_COLOUR;
		} else if (c.equals(Object.class)) {
			return OBJECT_COLOUR;
		}
		return UNKNOWN_COLOUR;
	}
	
	/**
	 * The actual image painted by this icon instance.  Gets put together by the
	 * constructor.
	 */
	private final Image image; 
	
	/**
	 * Composes an image based on the given parts.  The base and nib are drawn first
	 * (the nib below the background, less any overlap), then the handle decoration
	 * is drawn on top of the background.
	 * <p>
	 * All three images are optional, and will not be included in the final image if
	 * null.  If none of the images are provided, this icon will be pretty boring
	 * (0 size).
	 * 
	 * @param base The handle decoration image
	 * @param background The part behind the handle decoration
	 * @param nib The nib (the part that actually plugs in)
	 */
	private ConnectorIcon(Image base, Image background, Image nibCover, Image nibBackground) {

		Dimension baseSize = new Dimension(0, 0);
		if (base != null) {
			baseSize.width = base.getWidth(null);
			baseSize.height = base.getHeight(null);
		}
		Dimension bgSize = new Dimension(0, 0);
		if (background != null) {
			bgSize.width = background.getWidth(null);
			bgSize.height = background.getHeight(null);
		}
		Dimension nibSize = new Dimension(0, 0);
		if (nibBackground != null) {
			nibSize.width = nibBackground.getWidth(null);
			nibSize.height = nibBackground.getHeight(null);
			if (base != null || background != null) {
				nibSize.height -= NIB_OVERLAP;
			}
		}
		Dimension nibCoverSize = new Dimension(0, 0);
		if (nibCover != null) {
			nibCoverSize.width = nibCover.getWidth(null);
			nibCoverSize.height = nibCover.getHeight(null);
			if (base != null || background != null) {
				nibCoverSize.height -= NIB_OVERLAP;
			}
		}
		
		
		GraphicsEnvironment graphEnv =GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice graphDevice = graphEnv.getDefaultScreenDevice();
		GraphicsConfiguration graphicConf = graphDevice.getDefaultConfiguration();
		BufferedImage imageData = graphicConf.createCompatibleImage(
				Math.max(nibSize.width,Math.max(baseSize.width, bgSize.width)), 
				Math.max(baseSize.height, bgSize.height) + nibSize.height, Transparency.TRANSLUCENT );
		
		Graphics2D g2 = imageData.createGraphics();
		
		if (nibBackground != null) {
			g2.drawImage(nibBackground, 0, imageData.getHeight() - nibBackground.getHeight(null), null);
		}
		if (nibCover != null) {
			g2.drawImage(nibCover, 0, imageData.getHeight() - nibCover.getHeight(null), null);
		}
		if (background != null) {
			g2.drawImage(background, 0, 0, null);
		}
		if (base != null) {
			g2.drawImage(base, 0, 0, null);
		}

		g2.dispose();
		
		image = imageData;
	}
	
	/**
	 * Factory method for creating variants of the male part of the plug apparatus.
	 * Note that this is a subroutine of other factories, and no caching of the
	 * return value will be done here.
	 * 
	 * @param c The data type this plug is for
	 * @param includeNib If true, the nib will be included
	 * @param includeTop If true, the connector handle will be included
	 * @return A new connector matching the given specs.
	 */
	private static ConnectorIcon getMaleInstance(Class c, boolean includeNib, boolean includeTop) {
		Image base = null;
		Image background = null;
		if (includeTop) {
			base = new ImageIcon(ClassLoader.getSystemResource("icons/plugs/base/base_cover.png")).getImage();
			logger.debug("background resource path: icons/plugs/base/base_" + hexColour(c) + ".png");
			background = new ImageIcon(ClassLoader.getSystemResource("icons/plugs/base/base_" + hexColour(c) + ".png")).getImage();
		}
		
		Image nibBackground = null;
		Image nibCover = null;
		if (includeNib) {
			logger.debug("nib resource path: icons/plugs/nib/nib_" + hexColour(c) + ".png");
			nibBackground = new ImageIcon(ClassLoader.getSystemResource("icons/plugs/nib/nib_" + hexColour(c) + ".png")).getImage();
			nibCover = new ImageIcon(ClassLoader.getSystemResource("icons/plugs/nib/nib_cover.png")).getImage();
		}
		return new ConnectorIcon(base, background, nibCover, nibBackground);
	}
	
	/**
	 * Creates just the nib part of a male connector.  The resulting icon instance
	 * is cached, so calling this method many times is not a performance hit.
	 * 
	 * @param c The data type the new connector is for.
	 * @return The nib icon for the given data type (possibly cached from earlier calls)
	 */
	public static ConnectorIcon getNibInstance(Class c) {
		ConnectorIcon curr = nibCache.get(c); 
		if (curr == null) {
			curr = getMaleInstance(c, true, false);
			nibCache.put(c,curr);
		}
		return curr;
	}
	
	/**
	 * Creates a complete male connector.  The resulting icon instance
	 * is cached, so calling this method many times is not a performance hit.
	 * 
	 * @param c The data type the new connector is for.
	 * @return The full male connector icon for the given data type (possibly cached from earlier calls)
	 */
	public static ConnectorIcon getFullPlugInstance(Class c) {
		ConnectorIcon curr = fullPlugCache.get(c);
		if (curr == null) {
			curr = getMaleInstance(c, true, true);
			fullPlugCache.put(c,curr);
		}
		return curr;
	}
	
	/**
	 * Creates just the handle part of a male connector.  The resulting icon instance
	 * is cached, so calling this method many times is not a performance hit.
	 * 
	 * @param c The data type the new connector is for.
	 * @return The handle icon for the given data type (possibly cached from earlier calls)
	 */
	public static ConnectorIcon getHandleInstance(Class c) {
		ConnectorIcon curr = handleCache.get(c); 
		if (curr == null) {
			curr = getMaleInstance(c, false, true);
			handleCache.put(c,curr);			
		}
		return curr;
	}
	
	/**
	 * Creates the female connector for the given data type.  The resulting icon instance
	 * is cached, so calling this method many times is not a performance hit.
	 * 
	 * @param c The data type the new connector is for.
	 * @return The female connector icon for the given data type (possibly cached from earlier calls)
	 */
	public static ConnectorIcon getFemaleInstance(Class c) {
		ConnectorIcon curr = femaleCache.get(c); 
		if (curr == null) {
			Image base = new ImageIcon(ClassLoader.getSystemResource("icons/plugs/port/port_cover.png")).getImage();
			Image background = new ImageIcon(ClassLoader.getSystemResource("icons/plugs/port/port_" + hexColour(c) + ".png")).getImage();
			curr = new ConnectorIcon(base, background, null, null);
			femaleCache.put(c, curr);
		}
		return curr;
	}
	
	public int getIconHeight() {
		return image.getHeight(null);
	}

	public int getIconWidth() {
		return image.getWidth(null);
	}

	/**
	 * Paints this connector icon centered around the given X coordinate,
	 * with the top of the icon starting at the given Y coordinate.
	 * 
	 * @param c Ignored.  Null is acceptable.
	 * @param g The graphics to paint with
	 * @param x The X coordinate to center the painted icon around.
	 * @param y The Y coordinate for the top of the icon
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(image, x-getIconWidth()/2, y, null);
	}
}
