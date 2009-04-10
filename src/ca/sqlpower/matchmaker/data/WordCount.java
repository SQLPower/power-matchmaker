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

package ca.sqlpower.matchmaker.data;

/**
 * Simple class for keeping track of how many times a certain word
 * has been spotted.
 */
public class WordCount implements Comparable<WordCount> {

    /**
     * The word this count pertains to.
     */
    private final String word;
    
    /**
     * The number of times this word has been spotted.
     */
    private int count = 0;
    
    /**
     * @param word The word this count pertains to.
     */
    public WordCount(String word) {
        this.word = word;
    }
    
    /**
     * Increments this word's count by 1.
     */
    public void incrementCount() {
        count++;
    }

    /**
     * Returns the current count.
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the word this count pertains to.
     */
    public String getWord() {
        return word;
    }

    /**
     * Compares by count, or alphabetically by word when the counts are equal.
     */
    public int compareTo(WordCount o) {
        if (count < o.count) {
            return -1;
        } else if (count > o.count) {
            return 1;
        } else {
            return word.compareTo(o.word);
        }
    }
    
}
