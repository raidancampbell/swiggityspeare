/*
 * SwiggitySpeare_bot is the IRC bot interface
 * it receives information from the irc, and parses it up to deal with
 * This is the main entry point for the .jar execution,
 * and contains plenty of useful CLI switches
 */
/**
 * @author R. Aidan Campbell on 7/29/15.
 */

import org.pircbotx.*;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ChannelInfoEvent;
import org.pircbotx.hooks.events.InviteEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.output.OutputChannel;
import org.pircbotx.output.OutputIRC;

import org.apache.commons.cli.*;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.nio.charset.MalformedInputException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;


public class SwiggitySpeare_bot extends ListenerAdapter {
    Set<String> currentChannels = new HashSet<>();
    OutputIRC irc_instance = null;
    String neuralNetworkDirectory;
    String neuralNetworkFile;
    String bot_nick;
    boolean include_nick_in_query;
    static final String source = "https://github.com/raidancampbell/swiggityspeare";
    static final String INCLUDE_NICK = "include_nick_in_irc_query";
    static final String AUTOJOIN_CHANNELS = "channels_to_join_on_connect";
    static final String RNN_DIR = "char-rnn_directory";
    static final String IRC_PORT_NUMBER = "irc_port_number";
    static final String IRC_HOSTNAME = "irc_hostname";
    static final String RNN_FILE = "neural_network_filename";
    
    
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
        parseSource(event);
        parsePart(event);
        String message = event.getMessage();
        if (message.toLowerCase().startsWith(bot_nick.toLowerCase())) {
            String query = message;
            if (!query.isEmpty()) {
                query = Swiggityspeare_utils.trimNick(query, bot_nick); //trim off `swiggityspeare` from input
                if(include_nick_in_query) query = event.getUser().getNick() + ": " + query;
                String response = Swiggityspeare_utils.getString(query, neuralNetworkDirectory, neuralNetworkFile);
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
        try {
            FileInputStream inputStream = new FileInputStream(bot_nick + ".prop");
            Properties prop = new Properties();
            prop.load(inputStream);
            inputStream.close();
            System.out.println("Joining channel " + event.getChannel());
            prop.setProperty(AUTOJOIN_CHANNELS, prop.getProperty(AUTOJOIN_CHANNELS) + ' ' + event.getChannel());
            System.out.println("Updating autojoin channels to: " + prop.getProperty(AUTOJOIN_CHANNELS) + ' ' + event.getChannel());
            FileOutputStream outputStream = new FileOutputStream(bot_nick + ".prop");
            prop.store(outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("failed to update configuration file: '"+bot_nick+".prop'. exiting!");
            System.exit(2);
        }
    }

    /**
     * This is 80% duplicated code from the onMessage function.
     * This allows it to respond as expected to a private query.
     * There is no parseInvite, and neural network queries don't need to be prepended
     * TODO: don't duplicate code...
     * @param event PrivateMessageEvent received by the bot
     */
    @Override
    public void onPrivateMessage(PrivateMessageEvent event) {
        if( parseRemind(event) || parsePing(event) || parseSource(event)) return;
        String query = event.getMessage();
        if (!query.isEmpty()) {
            query = Swiggityspeare_utils.trimNick(query, bot_nick); //trim off `swiggityspeare` from input
            if(include_nick_in_query) query = event.getUser().getNick() + ": " + query;
            String response = Swiggityspeare_utils.getString(query, neuralNetworkDirectory, neuralNetworkFile);
            event.respond(response);
        }

    }

    /**
     *  Parses a MessageEvent to see if someone is asking for the bot's source
     *  if it does, it replies with a link to the github page
     * @param event GenericMessageEvent received by the bot
     */
    public boolean parseSource(GenericMessageEvent event) {
        if (event.getMessage().startsWith("!source")) {
            event.respond(source);
            return true;
            //look abdallah, I'm using an else if!
        } else if(event.getMessage().toLowerCase().startsWith("who is "+ bot_nick.toLowerCase())) {
            event.respond(source);
            return true;
        } else if(event.getMessage().toLowerCase().equals("what is "+ bot_nick.toLowerCase())) {
            event.respond(source);
            return true;
        }
        return false;
    }
    
    public boolean parsePart(MessageEvent event) {
        if (event.getMessage().startsWith("!leave") || event.getMessage().startsWith("!part")) {
            new OutputChannel(event.getBot(), event.getChannel()).part();
            try {
                FileInputStream inputStream = new FileInputStream(bot_nick + ".prop");
                Properties prop = new Properties();
                prop.load(inputStream);
                inputStream.close();
                System.out.println("leaving channel " + event.getChannel());
                prop.setProperty(AUTOJOIN_CHANNELS, prop.getProperty(AUTOJOIN_CHANNELS).replace(event.getChannel().getName(), "").replace("  ", " ").trim());
                System.out.println("Updating autojoin channels to: " + prop.getProperty(AUTOJOIN_CHANNELS).replace(event.getChannel().getName()+" ?", ""));
                FileOutputStream outputStream = new FileOutputStream(bot_nick + ".prop");
                prop.store(outputStream, null);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("failed to update configuration file: '"+bot_nick+".prop'. exiting!");
                System.exit(2);
            }
            
            
            return true;
        }
        return false;
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
    public boolean parseInvite(MessageEvent event){
        String message = event.getMessage();
        if(message.toLowerCase().startsWith("join")){
            String[] words = message.split(" ");
            if(words.length != 2) return false;

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

                if(irc_instance == null) irc_instance = new OutputIRC(event.getBot());
                irc_instance.action(strChannel, "part");

                return true;
            } else if (currentChannels.contains("#"+strChannel)) {
                System.out.println("attempting to join channel: " + "#" + strChannel);
                irc_instance.joinChannel("#" + strChannel);
                try {Thread.sleep(100); } catch (Exception e) { e.printStackTrace();}
                irc_instance.invite(user.toString(), "#" +strChannel);
                try {Thread.sleep(10000); } catch (Exception e) { e.printStackTrace();}
                
                if(irc_instance == null) irc_instance = new OutputIRC(event.getBot());
                irc_instance.action("#" + strChannel, "part");
                
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a GenericMessageEvent to see if it complies with a ping event
     * if it does, it replies with a pong
     * @param event GenericMessageEvent received by the bot
     */
    public boolean parsePing(GenericMessageEvent event){
        if(event.getMessage().startsWith("!ping")){
            event.respond("pong");
            return true;
        }
        return false;
    }

    /**
     * Parses a GenericMessageEvent to see if it complies with a reminder event
     * if it does, it enacts the reminder event
     * This will sleep this thread, but the library has several working threads
     * @param event GenericMessageEvent received by the bot
     */
    public boolean parseRemind(GenericMessageEvent event){
        String message = event.getMessage();
        if(!message.startsWith("!remind")) return false;
        if(message.toLowerCase().startsWith("!remind random")) {
            String reminderText = message.substring("!remind random".length()).trim();
            Random random = new Random();
            int sleepingTime = random.nextInt((1000 - 1) + 1) + 1;
            System.out.println("sleeping for " + sleepingTime + " minutes for the random remind!");
            event.respond("okay, I'll remind you sometime");
            try {
                Thread.sleep((long) 60000 * (long) sleepingTime);
            } catch (InterruptedException e) {
                event.respond("sorry, I derped.");
                e.printStackTrace();
                return true;
            }
            event.respond(reminderText);
            return true;
        }
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
                    break;  //only use the first number for time.
                }
            }
            if(intTime < 1) {
                System.err.println("ERROR: time " + intTime + " is not valid!");
                return true;
            }
            String reminderText = message.substring(message.indexOf(MINUTES) + MINUTES.length()).trim();
            event.respond("okay, reminding in " + intTime + " minutes.");
            Thread.sleep((long)60000 * (long)intTime);
            event.respond(reminderText);
        } catch (Exception e) {
            String usage = "Proper usage is: `!remind n minutes [...]`";
            event.respond("sorry, I derped.  " + usage);
            e.printStackTrace();
            return true;
        }
        return false;
    }

    /**
     * Generates a PircBotX configuration object given command line input
     * This method could probably use some cleaning 
     * @param args CLI args
     * @return the generated PircBotX configuration object
     */
    public static Configuration getConfigFromCLI(String[] args){
        //add options to the command line parser
        CommandLine cmdLineInstance;
        //for future reference: Option(String switch, does the option have args, String description)
        Option option_botname = new Option("n", true, "nick for the bot to take [swiggityspeare]");
        
        Options options = (new Options()).addOption(option_botname);
        //set defaults for the options
        String servername="irc.case.edu", botname="swiggityspeare", nnDir="dependencies/char-rnn", nnFile="irc_network.t7";
        String[] channels = {"#cwru", "#swag"};
        int port_number = 6697;
        boolean include_nick = false;
        
        //try and apply the parsed options.  if we didn't find them, then the defaults stick
        try {
            CommandLineParser parser = new DefaultParser();
            cmdLineInstance = parser.parse(options, args, false);
            if(cmdLineInstance.hasOption("n")) {
                botname = cmdLineInstance.getOptionValue("n");
            }
            Properties prop = getPropertiesFromFile(botname);
            servername = prop.getProperty(IRC_HOSTNAME);
            port_number = Integer.parseInt(prop.getProperty(IRC_PORT_NUMBER));
            channels = prop.getProperty(AUTOJOIN_CHANNELS).split(" ");
            nnDir = prop.getProperty(RNN_DIR);
            nnFile = prop.getProperty(RNN_FILE);
            include_nick = prop.getProperty(INCLUDE_NICK).toLowerCase().equals("true");
        } catch (ParseException e) {
            new HelpFormatter().printHelp("swiggityspeare", options );
            e.printStackTrace();
            System.exit(0);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: malformed port number given");
            e.printStackTrace();
            System.exit(1);
        }
        // not the cleanest way of getting this info out of static scoping, but it works
        SwiggitySpeare_bot bot_instance = new SwiggitySpeare_bot();
        bot_instance.neuralNetworkFile = nnFile;
        bot_instance.neuralNetworkDirectory = nnDir;
        bot_instance.bot_nick = botname;
        bot_instance.include_nick_in_query = include_nick;
        
        //Configure what we want our bot to do
        Configuration.Builder confBuilder = new org.pircbotx.Configuration.Builder()
                .setName(botname) //Set the nick of the bot.
                .setServerHostname(servername) //Join the network
                .setServerPort(port_number) // at this port
                .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates()) // using SSL
                .addListener(bot_instance); //Add our listener that will be called on Events
        for(String s : channels) confBuilder.addAutoJoinChannel(s);  //add all the channels you want it to join
        
        return confBuilder.buildConfiguration();
    }
    
    public static Properties getPropertiesFromFile(String botNick) {
        Properties prop = new Properties();
        try {
            try (FileInputStream inputStream = new FileInputStream(botNick + ".prop")) {
                prop.load(inputStream);
                inputStream.close();
            }
            return prop;
        } catch (IOException ioe_) {
            try {
                FileOutputStream outputStream = new FileOutputStream(botNick + ".prop");
                prop.setProperty(INCLUDE_NICK, "False");
                prop.setProperty(AUTOJOIN_CHANNELS, "#cwru");
                prop.setProperty(RNN_DIR, "dependencies/char-rnn");
                prop.setProperty(IRC_PORT_NUMBER, "6697");
                prop.setProperty(IRC_HOSTNAME, "irc.case.edu");
                prop.setProperty(RNN_FILE, botNick + ".t7");
                prop.store(outputStream, null);
                outputStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.err.println("failed to write initial configuration to file '"+botNick+".prop'. exiting!");
                System.exit(2);
            }
        }
        return prop;
    }

    /**
     * main method, .jar execution enters here.  We configure and begin a bot.
     * @param args CLI args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //parse bot parameters from any given CLI switches
        Configuration config = getConfigFromCLI(args);
        //Create our bot with the configuration
        PircBotX bot = new PircBotX(config);
        //Connect to the server
        bot.startBot();
    }
    

}
