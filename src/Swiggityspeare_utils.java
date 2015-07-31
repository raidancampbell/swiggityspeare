import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author R. Aidan Campbell on 6/11/15.
 */
public class Swiggityspeare_utils {
    
    public Swiggityspeare_utils(){
        //constructors, yo.
    }
    
    public static void main (String[] args){
        //main method, yo.
        
        //on every start we're gonna read the raw data, clean it, and write it back out.
        //because that makes sense while we're developing it.
        //writeCleanedData(readRawData());
    }

    /**
     * reads the data from the logcleaner
     * @return the cleaned raw data
     */
    private static String readRawData(){
        LogCleaner cleaner = new LogCleaner("data/raw");
        ShakespeareCleaner shakespeareCleaner = new ShakespeareCleaner("data/shakespeare");
        try {
            return cleaner.clean() + shakespeareCleaner.clean();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getStackTrace().toString();
        }
        
    }

    /**
     * writes the given data to a local cleaned log file
     * @return boolean if file already existed
     */
    private static boolean writeCleanedData(String dataToWrite){
        try {
            BufferedWriter write = new BufferedWriter(new FileWriter("input.txt"));
            write.write(dataToWrite);
        } catch (IOException e){
            return false;
        }
        return true;
    }

    public static String getString(String seed, int length){
        ArrayList<String> commands = new ArrayList<>();
        commands.add("th");
        commands.add("sample.lua");
        commands.add("-length");
        commands.add(((Integer) length).toString());
        if(seed != null){
            commands.add("-primetext");
            commands.add('"'+seed+'"');
        }
        commands.add("-seed");
        commands.add(System.currentTimeMillis()+"");//cheating way to turn it into a string
        commands.add("-verbose");
        commands.add("0");
//        commands.add("-gpuid");
//        commands.add("-1");//use cpu evaluation
        commands.add("irc_network.t7");
        System.out.println("> Querying Neural Net for response to: ");
        for(String s: commands) System.out.print(s);
        System.out.print('\n');
        StringBuilder response = new StringBuilder();
        try {
            Process pr = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]), 
                    null, new File( "data/char-rnn" ).getAbsoluteFile());
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
     * @return the RNN's response
     */
    public static String getString(String seed){
        String value = getString(seed, (int)(Math.random()*100)+100);
        if(value.indexOf('\n') < 0){
            return "huh.  I don't have an answer for you... " + value;
        }
        System.out.println("Responding to query with: " + value);
        value = value.substring(value.indexOf('\n')+1);  // text before first newline is garbage
        if (value.contains("\n")) {
            if(!value.contains(":")) return "";
            value = value.substring(value.indexOf(':') + 2, value.indexOf('\n')).trim();
            return trimNick(value);
        } else {
            if(!value.contains(":")) return "";
            value = value.substring(value.indexOf(':') + 2).trim();
            return trimNick(value);
        }
    }

    /**
     * removes IRC nicks from the beginning of a string
     * @param input string, possibly containing an IRC nick in `nick: ` format
     * @return the given string without the prepended nick
     */
    public static String trimNick(String input){
        Pattern p = Pattern.compile("^\\w+:\\s");  
        // one or more word characters at the beginning of a string, followed by a colon, then a whitespace
        Matcher m = p.matcher(input);
        if(m.find()) return input.substring(0, m.end());
        return input;
    }

    /**
     * elicits a string from the RNN, of the given length
     * @param length length of the desired string
     * @return the string returned from the RNN
     */
    public static String getString(int length){
        return getString(null, length);
    }

    /**
     * removes the 'nick:' from the string
     * @param networkResponse input directly from a getString call
     * @return the text that a nick said
     * TODO: we may receive multiline statements. fluffen, clean, flatten.
     */
    private static String cleanString(String networkResponse){
        String author=networkResponse.substring(0,networkResponse.indexOf(":"));
        String text =networkResponse.substring(networkResponse.indexOf(":"));
        return text;
    }
    
    public static boolean isNumber(String input){
        System.out.println("Checking if input '" + input + "' is a number");
        for(char c : input.toCharArray()){
            if(!Character.isDigit(c)) return false;
        }
        return true;
    }
}
