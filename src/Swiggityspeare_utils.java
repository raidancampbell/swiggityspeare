import java.io.*;
import java.util.ArrayList;

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

        try {
            return cleaner.clean();
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
                response.append(respondedLine);
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

    private static String getString(String seed){
        return getString(seed, 140);
    }

    private static String getString(int length){
        return getString(null, length);
    }
}
