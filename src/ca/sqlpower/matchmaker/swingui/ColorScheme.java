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


package ca.sqlpower.matchmaker.swingui;

import ca.sqlpower.util.WebColour;

/**
 * A simple holding place for colour schemes we want to use in the MatchMaker class.
 */
public class ColorScheme {

    /**
     * Brewer Colour Scheme "set19".  A nice collection of colours for colour coding
     * sets of information with up to 9 elements.
     */
    public static final WebColour[] BREWER_SET19 = {
        new WebColour("#e41a1c"),
        new WebColour("#377eb8"),
        new WebColour("#4daf4a"),
        new WebColour("#80b1d3"),
        new WebColour("#984ea3"),
        new WebColour("#ff7f00"),
        new WebColour("#ffff33"),
        new WebColour("#a65628"),
        new WebColour("#f781bf"),
        new WebColour("#999999")
    };
}
