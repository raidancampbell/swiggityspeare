import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aidan on 6/11/15.
 */
public class LogCleaner {
    private final String DIRECTORY;
    
    public LogCleaner(String givenDirectory){
        DIRECTORY = givenDirectory;
    }

    /**
     * concatenate all the files, scrub the timestamps off, then remove the join/part messages 
     * @return username: text 
     */
    public String clean() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for(String s : fluffen(readAllFiles())){
            s = removeStatusMessages(removeTimestamp(s)).trim();
            if(!s.equals(""))stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * everything up until the first space is a timestamp
     * @param input irc log line
     * @return the input, without the prepended timestamp
     */
    private String removeTimestamp(String input){
        return input.substring(input.indexOf(' '));
    }

    /**
     * if there's no colon, no user said anything, and it's a status message 
     * @param input irc log line
     * @return the input if it was an actual string, an empty string if it was a status message
     */
    private String removeStatusMessages(String input){
        input = input.trim();
        Matcher m = Pattern.compile("\\S+:").matcher(input);
        if (m.find()){//also handle when status messages are logged like nicks
            if(input.startsWith("Topic") || input.startsWith("ChanServ") || input.startsWith("Mode")) return "";
            if(input.startsWith(m.group(0))) return input;
        }
        
        return "";
    }

    /**
     * reads all the files from the DIRECTORY set in the constructor
     * @return the concatenated string of all the files
     */
    private String readAllFiles() throws IOException {
        File folder = new File(DIRECTORY);
        File[] listOfFiles = folder.listFiles();
        StringBuilder stringBuilder = new StringBuilder();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                stringBuilder.append( new String(Files.readAllBytes(Paths.get(DIRECTORY+'/'+file.getName()))));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * turn the string into a string[] split at every newline 
     * @param input input string
     * @return a string[], with magnitude equalling the number of lines in the input string
     */
    private String[] fluffen(String input){
        return input.split("\n");
    }
}
