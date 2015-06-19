import java.io.*;
import java.util.ArrayList;

/**
 * Created by aidan on 6/11/15.
 */
public class Swiggityspeare {
    
    public Swiggityspeare(){
        //constructors, yo.
    }
    
    public static void main (String[] args){
        //main method, yo.
        
        //on every start we're gonna read the raw data, clean it, and write it back out.
        writeCleanedData(readRawData());
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

    private static String getString(String seed, int length){
        ArrayList<String> commands = new ArrayList<>();
        commands.add("th");
        commands.add("checkpointEvaluator.lua");
        commands.add("-length");
        commands.add(((Integer) length).toString());
        if(seed != null){
            commands.add("-context");
            commands.add(seed);
        }
        commands.add("-seed");
        commands.add(System.currentTimeMillis()+"");//cheating way to turn it into a string
        System.out.println("> Querying Neural Net for response");
        StringBuilder response = new StringBuilder();
        try {
            Process pr = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String error;
            while((error = errorReader.readLine()) != null){
                System.err.println("> ERROR OUTPUT: "+error);
            }
            String respondedLine;
            while ((respondedLine = outputReader.readLine()) != null) {
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
     * elicits a reponse from the RNN, using the given seed as the subject of the response
     * @param seed what the RNN should respond about
     * @return the RNN's response
     */
    private static String getString(String seed){
        String value = getString(seed, (int)(Math.random()*100)+100);
        return value.substring(0,value.indexOf('\n'));
        //TODO: assert that the first line is properly formatted, and is really what I want.
    }

    /**
     * elicits a string from the RNN, of the given length
     * @param length length of the desired string
     * @return the string returned from the RNN
     */
    private static String getString(int length){
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
}
