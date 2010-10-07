/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.playground;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.sqlobject.SQLObjectException;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;


public class GoogleDocsSpreadSheet {
	
	/**
	 * Creates a matchMaker translate group from the Google spread sheet at pIOfRi4wZwIh1eNPmWCRhPQ
	 * The sheet is owned by matchmaker@sqlpower.ca called TranslationWords.
	 * 
	 * @return The MatchMakerTranslateWord group from the spread sheet 
	 * @throws SQLObjectException If something goes wrong
	 */
	public MatchMakerTranslateGroup getOnlineTranslateGroup() throws SQLObjectException {
		SpreadsheetService sss = new SpreadsheetService("SQLPower-Power*MatchMaker-0.9.1");
		CellFeed cf;
		
		try {
			URL url = new URL("http://spreadsheets.google.com/feeds/cells/pIOfRi4wZwIh1eNPmWCRhPQ/1/public/values");
			cf = sss.getFeed(url, CellFeed.class);
		} catch (Exception e) {
			throw new SQLObjectException("Error could not generate translation words from google spreadsheet!",e);
		}

		
		//This bit is kind of silly but we don't get entries for empty cells
		int length = 0;
		Map<String, String> entries = new HashMap<String, String>();
		for (CellEntry ce: cf.getEntries()) {
			length = Math.max(length, ce.getCell().getRow());
			entries.put(ce.getCell().getRow() + ":" + ce.getCell().getCol(), ce.getCell().getValue());
		}
		
		
		String[][] vals= new String[2][length];
		for (int y = 1; y<=length;y++) {
			for (int x = 1; x <=2; x++) {
				String key = y + ":" + x;
				vals[x-1][y-1] = entries.get(key);
			}
		}
		

		MatchMakerTranslateGroup mmtg = new MatchMakerTranslateGroup();
		mmtg.setName("SQLPower Translate Words");
		
		for (int x = 1; x<vals[0].length; x++) {
			//this should remove holes in the list
			if (vals[0][x] != null) {
				MatchMakerTranslateWord mmtw = new MatchMakerTranslateWord();
				mmtw.setFrom(vals[0][x]);
				mmtw.setTo(vals[1][x]);
				mmtg.addChild(mmtw);
			}
		}
		
		return mmtg;
	
	}
}
