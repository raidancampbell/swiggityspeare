
/**
 * @author R. Aidan Campbell on 7/29/15.
 */

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;


public class SwiggitySpeare_bot extends ListenerAdapter {

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        //When someone says ?helloworld respond with "Hello World"
        String message = event.getMessage();
        if (message.toLowerCase().startsWith("swiggity")){
            
            String query = message.substring(message.indexOf("swiggity"));
            if(! query.isEmpty()) {
                String response = Swiggityspeare_utils.getString(query);
                event.respond(response);
            }
            
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
                .addListener(new SwiggitySpeare_bot()) //Add our listener that will be called on Events
                .buildConfiguration();
        
        //Create our bot with the configuration
        PircBotX bot = new PircBotX(configuration);
        //Connect to the server
        bot.startBot();
    }
    

}
