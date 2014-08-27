package com.scrippsnetworks.wcm.asset.recipe;

import java.util.*;
import com.scrippsnetworks.wcm.taglib.Functions;

/**
 * @author Jason Clark
 * Date: 6/2/12
 * These are static functions for JSTL/EL use in formatting Recipe pages.
 */
@Deprecated
public final class FormatFunctions {

    private static final String OPEN_B = "<b>";
    private static final String CLOSE_B = "</b>";
    private static final String DASHES = "--";
    private static final String NONE = "none";

    private FormatFunctions() {}

    /**
     * Creates ISO 8601 micro-formatted time stamps representing duration.
     * @param minutes String which represents time in minutes
     * @return a String as a formatted ISO 8601 value
     */
    public static String getIso8601Duration(final String minutes) {
        if (minutes != null && minutes.length() > 0) {
            Integer totalMins;
            try {
                totalMins = Integer.valueOf(minutes);
            } catch (NumberFormatException e) {
                return null;
            }
            Integer iso8601hours = totalMins >= 60 ? totalMins / 60 : 0;
            Integer iso8601mins = totalMins - (iso8601hours * 60);
            return "PT" + iso8601hours + "H" + iso8601mins + "M";
        } else {
            return null;
        }
    }

    /**
     * Static method for transforming values in a String that aren't good for web display
     * into something that is good for web display.  Right now, transforms any permutation of
     * "none" or null values into "--".
     * @param property String value to munge
     * @return String munged value
     */
    public static String prettyPrintEmptyProperty(final String property) {
        if (property == null) {
            return DASHES;
        }
        return property.trim().toLowerCase().equals(NONE) ? DASHES : property;
    }

    /**
     * @param minutes String representing a duration in minutes
     * @return String representing time duration in plain english
     *
     * this is for formatting time durations as plain english Strings.
     * accepts a String that represents a duration in minutes.
     * returns a String as a formatted time duration busted into hrs/mins.
     */
    public static String getFormattedTime(final String minutes) {
        if (minutes != null && minutes.length() > 0 && !minutes.equals("0")) {
            Integer totalMins;
            try {
                totalMins = Integer.valueOf(minutes);
            } catch (NumberFormatException e) {
                return null;
            }
            Integer parsedHours = totalMins >= 60 ? totalMins / 60 : 0;
            Integer parsedMins = totalMins - (parsedHours * 60);
            String formattedHours = "";
            String formattedMinutes = "";
            if (parsedHours > 0) {
                formattedHours = parsedHours + " hr";
              
            }
            if (parsedMins > 0) {
                formattedMinutes = parsedMins + " min";
              
            }
            return formattedHours + " " + formattedMinutes;
        } else {
            return "--";
        }
    }

    /**
     * Comb through the Instructions/Directions blocks looking for titles to bold.
     * A title is defined as a line ending in a colon, or all caps.
     * @param input String block to format
     * @return String formatted block
     */
    public static String formatRecipeInstructions(final String input) {
        
    	if (input == null) {
            return null;
        }
        String[] blockLines = input.split("\n");
        StringBuilder output = new StringBuilder();
        for (String line : blockLines) {
            if (line.trim().matches(".*:$")
                    || Functions.removeMarkup(line)
                    .equals(Functions.removeMarkup(line).toUpperCase())) {
                output
                    .append(OPEN_B)
                    .append(line)
                    .append(CLOSE_B);
            } else {
                output.append(line);
            }
        }
        
        if (output.length() > 0) {
            return output.toString();
        } else {
            return input;
        }
    }

    /**
     * This takes the recipe ingredient data as it is formatted in the RMA and munges it for web display.
     *
     * assumptions:
     * blocks that are labeled "ingredients" can contain the following: just a title, just a list of
     * ingredients, a title with a list of ingredients, or N title/ingredients lists
     *
     * givens:
     * individual lines are broken by \n.
     * titles are lines that end with a colon, or are all upper case
     * data contains unescaped HTML tags, including paragraph tags
     *
     * if there is no title, this function treats the input as a single list of ingredients
     *
     * TODO: wrap SEO spans around "amount" and "name" of recipe ingredients
     *
     * @param  input String of raw ingredient data from RMA
     * @return String of recipe ingredients formatted for web
     */
    public static String formatRecipeIngredients(final String input) {
        if (input != null && input.length() > 0) {
            String[] splitInput = input.split("\n");
            List<String> ingredients = new LinkedList<String>();

            //cleanup incoming data
            for (String line : splitInput) {
                if (line.equals("")) {
                    continue;
                }
              	// After removing html markup , new line was getting inserted.Replacing newline with space. 
                ingredients.add((Functions.removeMarkupExceptAnchors(line)).replaceAll("\\r|\\n", " "));
            }

            List<Integer> ingredientTitleIndices = new LinkedList<Integer>();

            //build a list of titles to indicate start/end of ingredient blocks
            //colon at the end or all caps denotes a title
            for (int i = 0; i < ingredients.size(); i++) {                   	
                if (ingredients.get(i).trim().matches(".*:$")
                        || ingredients.get(i).equals(ingredients.get(i).toUpperCase())) {
                    ingredientTitleIndices.add(i);
                }
            }

            //check to see if there is a title with no ingredients list
            //because apparently that is something that people like to do
            if (!ingredientTitleIndices.isEmpty() && ingredients.size() == 1) {
                return null;
            }

            //for breaking ingredients blocks into columns
            List<Integer> firstColumnBreak = new LinkedList<Integer>();
            if (ingredientTitleIndices.isEmpty()) {
                int length = ingredients.size();
                int firstColStop = length % 2 == 0 ? length / 2 : (length + 1) / 2;
                firstColumnBreak.add(firstColStop - 1);
            } else {
                for (Integer titleIndex : ingredientTitleIndices) {
                    Iterator<Integer> itr = ingredientTitleIndices
                            .listIterator(ingredientTitleIndices.indexOf(titleIndex) + 1);
                    if (itr.hasNext()) {
                        //there is at least one more title after this one
                        int sum = titleIndex + itr.next() - 1;
                        int firstColStop = sum % 2 == 0 ? sum / 2 : (sum + 1) / 2;
                        firstColumnBreak.add(firstColStop);
                    } else {
                        //on last title, measure from end of ingredients list
                        int sum = titleIndex + ingredients.size() - 1;
                        int firstColStop = sum % 2 == 0 ? sum / 2 : (sum + 1) / 2;
                        firstColumnBreak.add(firstColStop);
                    }
                }
            }

            StringBuilder output = new StringBuilder(input.length() * 3);

            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredientTitleIndices.contains(i)) {
                    if (i != 0) {
                        output.append("</ul>");
                    }
                    output
                        .append("<div class=\"list clear\"></div>")
                        .append("<span class=\"ingr-divider\"><b>")
                        .append(ingredients.get(i).toUpperCase())
                        .append("</b></span><div class=\"list clear\"></div>")
                        .append("<ul class=\"col1\">");
                } else if (ingredientTitleIndices.isEmpty() && i == 0) {
                    output
                        .append("<ul class=\"col1\">")
                        .append("<li class=\"ingredient\">")
                        .append(ingredients.get(i)
                            .replaceFirst("^([0-9/ ]+)", "<em>$1</em>"))
                        .append("</li>");
                    if (firstColumnBreak.contains(i)) {
                        output.append("</ul><ul class=\"col2\">");
                    }
                } else {
                    output
                        .append("<li class=\"ingredient\">")
                        .append(ingredients.get(i)
                            .replaceFirst("^([0-9/ ]+)", "<em>$1</em>"))
                        .append("</li>");
                    if (firstColumnBreak.contains(i)) {
                        output.append("</ul><ul class=\"col2\">");
                    }
                }
            }
            output.append("</ul>");

            return output.toString();
        } else {
            return null;
        }
    }
    
    /**
     * This takes the recipe ingredient data and format it for mobile page display.
     * @param  input String of raw ingredient data from RMA
     * @return String of recipe ingredients formatted for mobile web
     */
    public static String formatRecipeIngredientsForMobile(final String input) {
        if (input != null && input.length() > 0) {
            String[] splitInput = input.split("\n");
            List<String> ingredients = new LinkedList<String>();

            //cleanup incoming data
            for (String line : splitInput) {
                if (line.equals("")) {
                    continue;
                }
                ingredients.add(Functions.removeMarkupExceptAnchors(line));
            }

            List<Integer> ingredientTitleIndices = new LinkedList<Integer>();

            //build a list of titles to indicate start/end of ingredient blocks
            //colon at the end or all caps denotes a title
            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredients.get(i).trim().matches(".*:$")
                        || ingredients.get(i).equals(ingredients.get(i).toUpperCase())) {
                    ingredientTitleIndices.add(i);
                }
            }

            //check to see if there is a title with no ingredients list
            //because apparently that is something that people like to do
            if (!ingredientTitleIndices.isEmpty() && ingredients.size() == 1) {
                return null;
            }

            //for breaking ingredients blocks into columns
            List<Integer> firstColumnBreak = new LinkedList<Integer>();
            if (ingredientTitleIndices.isEmpty()) {
                int length = ingredients.size();
                int firstColStop = length % 2 == 0 ? length / 2 : (length + 1) / 2;
                firstColumnBreak.add(firstColStop - 1);
            } else {
                for (Integer titleIndex : ingredientTitleIndices) {
                    Iterator<Integer> itr = ingredientTitleIndices
                            .listIterator(ingredientTitleIndices.indexOf(titleIndex) + 1);
                    if (itr.hasNext()) {
                        //there is at least one more title after this one
                        int sum = titleIndex + itr.next() - 1;
                        int firstColStop = sum % 2 == 0 ? sum / 2 : (sum + 1) / 2;
                        firstColumnBreak.add(firstColStop);
                    } else {
                        //on last title, measure from end of ingredients list
                        int sum = titleIndex + ingredients.size() - 1;
                        int firstColStop = sum % 2 == 0 ? sum / 2 : (sum + 1) / 2;
                        firstColumnBreak.add(firstColStop);
                    }
                }
            }

            StringBuilder output = new StringBuilder(input.length() * 3);
            boolean flag=false;
            for (int i = 0; i < ingredients.size(); i++) {
            	if(ingredientTitleIndices.contains(i)){
            		if (i != 0) {
                        output.append("</ul>");
                    }
            		output
            		.append("<h5>")
                    .append(ingredients.get(i).toUpperCase())
                    .append("</h5><ul class=\"list\">");
            		flag=true;
            	}else if (ingredientTitleIndices.isEmpty() && i == 0) {
                    output
                    .append("<ul class=\"list\">")
                    .append("<li>")
                    .append(ingredients.get(i)
                        .replaceFirst("^([0-9/ ]+)", "<B>$1</B>"))
                    .append("</li>");
                    flag=true;
            	} else{
            		
            		output
                    .append("<li>")
                    .append(ingredients.get(i)
                        .replaceFirst("^([0-9/ ]+)", "<B>$1</B>"))
                    .append("</li>");
            	}
            }
            if(flag)
            	output.append("</ul>");
            

            return output.toString();
        } else {
            return null;
        }
    }
	 /**
     * This code will be merged with the web version method after sprint 3 completes.
     *
     * assumptions:
     * blocks that are labeled "ingredients" can contain the following: just a title, just a list of
     * ingredients, a title with a list of ingredients, or N title/ingredients lists
     *
     * givens:
     * individual lines are broken by \n.
     * titles are lines that end with a colon, or are all upper case
     * data contains unescaped HTML tags, including paragraph tags
     *
     * if there is no title, this function treats the input as a single list of ingredients
     *
     *This code will be merged with the web version method after sprint 3 completes.
     * @return String of recipe ingredients formatted for print
     */
    public static String printFormatRecipeIngredients(final String input) {
        if (input != null && input.length() > 0) {
            String[] splitInput = input.split("\n");
            List<String> ingredients = new LinkedList<String>();

            //cleanup incoming data
            for (String line : splitInput) {
                if (line.equals("")) {
                    continue;
                }
                ingredients.add(Functions.removeMarkup(line));
            }

            List<Integer> ingredientTitleIndices = new LinkedList<Integer>();

            //build a list of titles to indicate start/end of ingredient blocks
            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredients.get(i).trim().matches("[()a-zA-Z0-9' -]+:")) {
                    ingredientTitleIndices.add(i);
                }
            }

            //check to see if there is a title with no ingredients list
            //because apparently that is something that people like to do
            if (!ingredientTitleIndices.isEmpty() && ingredients.size() == 1) {
                return null;
            }

            //for breaking ingredients blocks into columns
            List<Integer> firstColumnBreak = new LinkedList<Integer>();
            if (ingredientTitleIndices.isEmpty()) {
                int length = ingredients.size();
                int firstColStop = length % 2 == 0 ? length / 2 : (length + 1) / 2;
                firstColumnBreak.add(firstColStop - 1);
            } else {
                for (Integer titleIndex : ingredientTitleIndices) {
                    Iterator<Integer> itr = ingredientTitleIndices
                            .listIterator(ingredientTitleIndices.indexOf(titleIndex) + 1);
                    if (itr.hasNext()) {
                        //there is at least one more title after this one
                        int sum = titleIndex + itr.next() - 1;
                        int firstColStop = sum % 2 == 0 ? sum / 2 : (sum + 1) / 2;
                        firstColumnBreak.add(firstColStop);
                    } else {
                        //on last title, measure from end of ingredients list
                        int sum = titleIndex + ingredients.size() - 1;
                        int firstColStop = sum % 2 == 0 ? sum / 2 : (sum + 1) / 2;
                        firstColumnBreak.add(firstColStop);
                    }
                }
            }

            StringBuilder output = new StringBuilder(input.length() * 3);

            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredientTitleIndices.contains(i)) {
                    if (i != 0) {
                        output
                            .append("</ul></div>")
                            .append("<div class=\"list clrfix\">");
                	}
	                output
                        .append("<ul class=\"col1\">")
	                    .append("<li>")
	                    .append(ingredients.get(i).toUpperCase())
	                    .append("</li>");
                    
                } else if (ingredientTitleIndices.isEmpty() && i == 0) {
                    output
                        .append("<ul class=\"col1\">")
                        .append("<li>")
                        .append(ingredients.get(i))
                        .append("</li>");
                    if (firstColumnBreak.contains(i)) {
                        output.append("</ul><ul class=\"col2\">");
                    }
                } else {
                    output
                        .append("<li>")
                        .append(ingredients.get(i))
                        .append("</li>");
                    if (firstColumnBreak.contains(i)) {
                        output.append("</ul><ul class=\"col2\">");
                    }
                }
                
            }
            output
                .append("</ul></div>")
        	    .append("<div class=\"list clrfix\">");
            return output.toString();
        } else {
            return null;
        }
    }

    /**
     * This code will remove all HTML from a recipe instruction block except simple inline elements
     * and select elements that do have attributes.
     *
     * assumptions:
     * None
     *
     * givens:
     * None
     *
     * @return String of recipe instructions formatted for print
     */
    public static String printFormatRecipeInstructions(final String input) {
        if (input != null && input.length() > 0) {
            String instructions = Functions.removeMarkupExceptPrintable(input);
            instructions = instructions.replaceAll("<(\\w+)\\s+/>\\s?", "<$1/>");

            return instructions;
        } else {
            return null;
        }
    }

	/**
	 * This code will remove the "<div class=\"list clear\"></div>" from the
	 * ingredients block.
	 * 
	 * Reason to implement this method: There is an extra div coming in the
	 * ingredients block after the format is done. Due to this, extra white
	 * spaces are coming on the page. This method is to remove the extra div
	 * without touching the base code
	 * 
	 * @param input
	 * @return
	 */
	public static String removeExtraListClearDiv(final String input) {
		String output = "";
		if (input != null && input.length() > 0) {
			String extraDiv = "<div class=\"list clear\"></div>";

			int indexOf = input.indexOf(extraDiv);
			if (indexOf == 0) {
				output = input.replaceFirst(extraDiv, "");
			}
			else
			{
				return input;
			}
		}
		return output;
	}
}
