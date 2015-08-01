
/**
 * @author R. Aidan Campbell on 7/29/15.
 * 
 * TODO: disconnect from a channel after invite given
 * TODO: conditional command obedience
 */

import org.pircbotx.*;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ChannelInfoEvent;
import org.pircbotx.hooks.events.InviteEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.output.OutputIRC;

import java.nio.charset.MalformedInputException;
import java.util.HashSet;
import java.util.Set;


public class SwiggitySpeare_bot extends ListenerAdapter {
    Set<String> currentChannels = new HashSet<>();
    OutputIRC irc_instance = null;


    /**
     * Honestly, I haven't figured out the difference between OnGenericMessage and OnMessage yet
     *      A MessageEvent seems to be more useful though, because I can tell what channel it came from
     * @param event MessageEvent given when someone says something on a channel the bot is in
     */
    @Override
    public void onMessage(MessageEvent event){
        parseInvite(event);
        parseRemind(event);
        parsePing(event);
        String message = event.getMessage();
        if (message.toLowerCase().startsWith("swiggity")) {
            String query = message.substring(message.indexOf(" ") + 1);
            if (!query.isEmpty()) {
                String response = Swiggityspeare_utils.getString(query);
                event.respond(response);
            }
        }
    }

    /**
     * Whenever a channelList is requested, this is the callback function that gets run once the list is received
     * So we update an object containing our idea of what the channels are:
     * NOTE: This isn't used very well.  I currently call channelList, then plow on through using whatever list I have
     *      I do not wait to receive the new list, so my code currently always uses stale channel lists
     * @param event ChannelInfoEvent containing the list of channels received from the server
     */
    @Override
    public void onChannelInfo(ChannelInfoEvent event){
        currentChannels.clear();
        for (Object chan : event.getList()){
            if (chan.getClass() != ChannelListEntry.class) return;
            currentChannels.add(((ChannelListEntry) chan).getName());
        }
    }

    /**
     * When the bot is invited to a channel, it will join.
     * @param event an InviteEvent to a channel
     */
    @Override
    public void onInvite(InviteEvent event) {
        if(irc_instance == null) irc_instance = new OutputIRC(event.getBot());
        irc_instance.joinChannel(event.getChannel());
    }

    /**
     * plenty of times when people start using (or come back to using after a long time) irc, they
     * screw up the syntax of the `/join` command e.g. `join channel` or `join #channel` instead
     * of the proper `/join #channel`
     * This function takes an event, to check if one of a user performed a mismatched `join` command
     * The bot will then parse the channel name from the message.  It will list the channels on the server
     * and match the parsed channel against the list of channels.  If nothing shows up, the bot silently gives up.
     * If there is a match in the channel list, the bot will 
     * attempt to join the channel that the user requested, then perform an 
     * `invite` command to bring the user to the channel that they wanted.
     *
     * @param event MessageEvent that may contain a malformed `join` command from another user
     */
    public void parseInvite(MessageEvent event){
        String message = event.getMessage();
        if(message.toLowerCase().startsWith("join")){
            String[] words = message.split(" ");
            if(words.length != 2) return;

            User user = event.getUser();
            String strChannel = words[1];
            System.out.println("searching for channel " + strChannel);
            if(irc_instance == null) irc_instance = new OutputIRC(event.getBot());
            irc_instance.listChannels();
            if(currentChannels.contains(strChannel)) {
                System.out.println("attempting to join channel: " + strChannel);
                irc_instance.joinChannel(strChannel);
                try {Thread.sleep(100); } catch (Exception e) { e.printStackTrace();}
                irc_instance.invite(user.toString(), strChannel);
                try {Thread.sleep(10000); } catch (Exception e) { e.printStackTrace();}
                //TODO: part the channel.  Dunno how yet
            } else if (currentChannels.contains("#"+strChannel)) {
                System.out.println("attempting to join channel: " + "#" + strChannel);
                irc_instance.joinChannel("#" + strChannel);
                try {Thread.sleep(100); } catch (Exception e) { e.printStackTrace();}
                irc_instance.invite(user.toString(), "#" +strChannel);
                try {Thread.sleep(10000); } catch (Exception e) { e.printStackTrace();}
                //TODO: part the channel.  Dunno how yet
            }
        }
    }

    /**
     * Parses a GenericMessageEvent to see if it complies with a ping event
     * if it does, it replies with a pong
     * @param event GenericMessageEvent received by the bot
     */
    public void parsePing(GenericMessageEvent event){
        if(event.getMessage().startsWith("!ping")){
            event.respond("pong");
        }
        
    }

    /**
     * Parses a GenericMessageEvent to see if it complies with a reminder event
     * if it does, it enacts the reminder event
     * This will sleep this thread, but the library has several working threads
     * @param event GenericMessageEvent received by the bot
     */
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
                .addAutoJoinChannel("#cwru")
                .addListener(new SwiggitySpeare_bot()) //Add our listener that will be called on Events
                .buildConfiguration();
        
        //Create our bot with the configuration
        PircBotX bot = new PircBotX(configuration);
        //Connect to the server
        bot.startBot();
    }
    

}
