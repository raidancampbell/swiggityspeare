
/**
 * @author R. Aidan Campbell on 7/29/15.
 */

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.nio.charset.MalformedInputException;


public class SwiggitySpeare_bot extends ListenerAdapter {

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        parseRemind(event);
        parsePing(event);
        
        String message = event.getMessage();
        if (message.toLowerCase().startsWith("swiggity")){
            String query = message.substring(message.indexOf(" ")+1);
            if(! query.isEmpty()) {
                String response = Swiggityspeare_utils.getString(query);
                event.respond(response);
            }
        }
    }
    
    public void parsePing(GenericMessageEvent event){
        if(event.getMessage().startsWith("!ping")){
            event.respond("pong");
        }
        
    }
    
    public void parseRemind(GenericMessageEvent event){
        String message = event.getMessage();
        if(!message.startsWith("!remind")) return;
        try {
            String MINUTES = "minutes";
            if(!message.contains(MINUTES)) throw new MalformedInputException(0);
            String strTime = message.substring("!remind ".length());
            System.out.println("Looking for time in string: " + strTime);
            int intTime = -1;
            for(String s : strTime.split(" ")){
                if(Swiggityspeare_utils.isNumber(s)){
                    System.out.println("Found a number "+ s + " for time!");
                    intTime = Integer.parseInt(s);
                }
            }
            if(intTime < 1) {
                System.err.println("ERROR: time " + intTime + " is not valid!");
                throw new MalformedInputException(1);
            }
            String reminderText = message.substring(message.indexOf(MINUTES) + MINUTES.length()).trim();
            Thread.sleep((long)60000 * (long)intTime);
            event.respond(reminderText);
        } catch (Exception e) {
            String usage = "Proper usage is: `!remind n minutes [...]`";
            event.respond("sorry, I derped.  " + usage);
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args) throws Exception {
        //Configure what we want our bot to do
        Configuration configuration = new org.pircbotx.Configuration.Builder()
                .setName("swiggityspeare") //Set the nick of the bot.
                .setServerHostname("irc.case.edu") //Join the network
                .setServerPort(6697) // at this port
                .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates()) // using SSL
                .addAutoJoinChannel("#swag") //Join the test channel
                //.addAutoJoinChannel("#cwru")
                .addListener(new SwiggitySpeare_bot()) //Add our listener that will be called on Events
                .buildConfiguration();
        
        //Create our bot with the configuration
        PircBotX bot = new PircBotX(configuration);
        //Connect to the server
        bot.startBot();
    }
    

}
