/*
 * Swiggityspeare_utils contains odds-and-ends, mostly for interfacing with the neural network
 * The main method cleans and outputs IRC log files, while other methods are more geared towards
 * cleaning data before and after querying the neural network
 */

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author R. Aidan Campbell on 6/11/15.
 */
public class Swiggityspeare_utils {
    
    public Swiggityspeare_utils(){
        //where we're going, we don't need any constructors
    }
    
    public static void main (String[] args){
        //main method, yo.
        
        //on start we're gonna read the raw data, clean it, and write it back out.
        //because that makes sense while we're developing it.
        writeCleanedData(readRawData());
    }

    /**
     * reads the data from the logcleaner
     * @return the cleaned raw data
     */
    public static String readRawData(){
        LogCleaner cleaner = new LogCleaner("data/raw");
        ShakespeareCleaner shakespeareCleaner = new ShakespeareCleaner("data/shakespeare");
        try {
            return cleaner.clean() + shakespeareCleaner.clean();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString(); //probably not terribly useful
        }
        
    }

    /**
     * writes the given data to a local cleaned log file
     * @return boolean if file already existed
     */
    public static boolean writeCleanedData(String dataToWrite){
        try {
            BufferedWriter write = new BufferedWriter(new FileWriter("input.txt"));
            write.write(dataToWrite);
        } catch (IOException e){
            return false;
        }
        return true;
    }

    /**
     * Here's where the neural network actually gets queried. 
     * This requires a working char-rnn environment, with a trained network
     * called `irc_network.t7` placed in the root directory of the char-rnn folder
     * @param seed primetext to input to the network
     * @param length number of characters requested for a response
     * @param directory the location (inclusive) of char-rnn
     * @param file the name of the .t7 trained network to query                 
     * @return the neural network's response: a String of length `length`
     */
    public static String getString(String seed, int length, String directory, String file){
        ArrayList<String> commands = new ArrayList<>();
        commands.add("th");
        commands.add("sample.lua");
        commands.add("-length");
        commands.add(((Integer) length).toString());
        if(seed != null){
            commands.add("-primetext");
            commands.add(seed); //there's funniness with what a command is.  This CAN have spaces, and still be legal!
        }
        commands.add("-seed");
        commands.add(System.currentTimeMillis()+"");//cheating way to turn it into a string
        commands.add("-verbose");
        commands.add("0"); // make sure what char-rnn gives us is the network's output
        commands.add(file);
        System.out.println("> Querying Neural Net for response to: ");
        for(String s: commands) System.out.print(s + ' ');
        System.out.print('\n');
        StringBuilder response = new StringBuilder();
        try {
            Process pr = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]), 
                    null, new File(directory).getAbsoluteFile());
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String error;
            while((error = errorReader.readLine()) != null){
                System.err.println("> ERROR OUTPUT: "+error);
            }
            String respondedLine;
            while ((respondedLine = outputReader.readLine()) != null) {
                System.out.println("> stdout output: " + respondedLine);
                response.append(respondedLine).append("\n");
            }
            errorReader.close();
            outputReader.close();
            return response.toString();
        } catch(IOException e){
            System.err.println("> ERROR WHILE QUERYING RNN");
            e.printStackTrace();
            System.exit(1);
        }
        return "";
    }

    /**
     * elicits a response from the RNN, using the given seed as the subject of the response
     * @param seed what the RNN should respond about
     * @param directory the location (inclusive) of char-rnn
     * @param file the name of the .t7 trained network to query                 
     * @return the RNN's response
     */
    public static String getString(String seed, String directory, String file){
        //the Math.rand is to produce an approximately tweet-length string
        String value = getString(seed, (int)(Math.random()*100)+100, directory, file);
        if(value.indexOf('\n') < 0){
            return "huh.  I don't have an answer for you... " + value;
        }
        System.out.println("Responding to query with: " + value);
        value = value.substring(value.indexOf('\n')+1);  // text before first newline is garbage
        if (value.contains("\n")) {
            if(!value.contains(":")) return value; //all sorts of a patchwork solution.
            // if we treat IRC-trained neural networks differently than normal ones, query it better (TODO)
            value = value.substring(value.indexOf(':') + 2, value.indexOf('\n')).trim();
            return trimNick(trimNick(value));
        } else {
            if(!value.contains(":")) return value;
            value = value.substring(value.indexOf(':') + 2).trim();
            return trimNick(trimNick(value));
        }
    }
    
    /**
     * elicits a string from the RNN, of the given length
     * @param length length of the desired string
     * @param directory the location (inclusive) of char-rnn
     * @param file the name of the .t7 trained network to query                  
     * @return the string returned from the RNN
     */
    public static String getString(int length, String directory, String file){
        return getString(null, length, directory, file);
    }

    /**
     * removes IRC nicks from the beginning of a string
     * @param input string, possibly containing an IRC nick in `nick: ` format
     * @return the given string without the prepended nick
     */
    public static String trimNick(String input){
        Pattern p = Pattern.compile("^ ?\\w+:\\s");
        // one or more word characters at the beginning of a string, followed by a colon, then a whitespace
        Matcher m = p.matcher(input);
        if(m.find()) return input.substring(m.end()).trim();
        return input;
    }

    /**
     * You give me a string, I tell you if every character is a digit
     * decimals and negative signs are NOT considered numeric.  This will
     * simply check if every character is a digit
     * @param input String to check whether it is a number
     * @return whether the input String is a number
     */
    public static boolean isNumber(String input){
        System.out.println("Checking if input '" + input + "' is a number");
        for(char c : input.toCharArray()){
            if(!Character.isDigit(c)) return false;
        }
        return true;
    }
}
