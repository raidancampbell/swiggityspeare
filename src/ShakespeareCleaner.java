/*
 * ShakespeareCleaner is analogous to LogCleaner:
 * it takes the works of shakespeare and cleans and reformats them into
 * a more IRC-style format.
 * Input the works of shakespeare, 
 * Output the works of shakespeare in irc-style, which, come to think of it, is a really weird thing to do.
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author R. Aidan Campbell on 6/19/2015.
 */
public class ShakespeareCleaner {
    private final String DIRECTORY;

    public ShakespeareCleaner(String givenDirectory){
        DIRECTORY = givenDirectory;
    }

    /**
     * concatenate all the files, add a nick for Shakespeare, then condense it all back down.
     * @return username: text
     */
    public String clean() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for(String s : fluffen(readShakespeare())){
            s = s.trim();
            if(!s.equals(""))stringBuilder.append(addNick(s)).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * reads all the files in the given directory, and concatenates them
     * as the entirety of shakespeare's works
     * @return all the files in the constructor's directory, concatenated
     * @throws IOException if no files are found in the constructor's directory
     */
    private String readShakespeare()  throws IOException{
        File folder = new File(DIRECTORY);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles == null) throw new IOException();
        StringBuilder stringBuilder = new StringBuilder();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                stringBuilder.append( new String(Files.readAllBytes(Paths.get(DIRECTORY + '/' + file.getName()))));
            }
        }
        return stringBuilder.toString();
    }

    private String addNick(String shakespeareanLine){
        return "Shakespeare: "+shakespeareanLine;
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
