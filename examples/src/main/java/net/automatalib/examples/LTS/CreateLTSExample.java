/* Copyright (C) 2014 AutomataLib
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.examples.LTS;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.lts.impl.InputOutputLTS;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.graphs.FiniteLTS;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.InputOutputLabel;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.InputOutputLabelLTS;

/**
 * An example class that creates a random input output transition system.
 * 
 * @author Michele Volpato
 *
 */
public class CreateLTSExample {
	
	private static InputOutputLTS<Character,Integer> constructLTS (int numberOfStates, int numberOfInputSymbols, int numberOfOutputSymbols, long seed){
			
		// random number generator
    	Random generator = new Random(seed);
    	
    	// input alphabet contains characters
    	String abc = "abcdefghijklmnopqrstuvwxyz";
    	char lastInputSymbol = abc.charAt(numberOfInputSymbols-1); 
    	
    	//output labels
    	List<Integer> lstO = CollectionsUtil.intRange(0,  numberOfOutputSymbols);
    	
    	//input labels
    	List<Character> lstI = CollectionsUtil.charRange('a', (char)(lastInputSymbol + 1));
    	
    	List<InputOutputLabel> alpha = new ArrayList<InputOutputLabel>();
    	
    	for(Integer output: lstO){
    		InputOutputLabel label = new InputOutputLabelLTS(output, false);
    		alpha.add(label);
    	}
    	for(Character input: lstI){
    		InputOutputLabel label = new InputOutputLabelLTS(input, true);
    		alpha.add(label);
    	}
    	
    	Alphabet<InputOutputLabel> alphabet = Alphabets.fromList(alpha);

    	// create states
    	InputOutputLTS<Character, Integer> lts = new InputOutputLTS<Character, Integer>(alphabet);
    	List<Integer> states = new ArrayList<Integer>();
    	states.add(lts.addInitialState());
    	for (int i = 1; i <= numberOfStates; i++) {
    		states.add(lts.addState());
    	}
    	
    	//create transitions
    	for (int source : states) {
    		for (InputOutputLabel label : alphabet) {
    			//output are not enabled in every state
    			if(label.isInput() || generator.nextBoolean()){
	    			//create transition
	    			int targetIndex = generator.nextInt(numberOfStates-1);
	    			int target = states.get(targetIndex);
	    			
	    			Integer transition = lts.createTransition(target);
	    			lts.addTransition(source, label, transition);
    			}
    			
    		}
    	}
    	System.out.println(lts.toString());
		
		return lts;
	}
	

	public static void main(String[] args) throws IOException {
		// load LTS and alphabet
        
        InputOutputLTS<Character,Integer> lts = constructLTS(10,3,3,0L);
        Alphabet<InputOutputLabel> inputsLTS = lts.getInputAlphabet();
        
        // print model to file
        //String filename = Settings.DIRECTORY + 10+"_"+3+"_"+3+"_"+String.valueOf(0)+"_LTS.dot";
        String filename = "" + 10+"_"+3+"_"+3+"_"+String.valueOf(0)+"_LTS.dot";
        PrintStream writer = new PrintStream(
        	     new FileOutputStream(filename)); 
        GraphDOT.write(lts, inputsLTS, writer); // may throw IOException!
        writer.close();
        System.out.println("Model written to " + filename);

	}

}
