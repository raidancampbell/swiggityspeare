import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
    
    
}
