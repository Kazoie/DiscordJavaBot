import events.*;
import events.ReadyEventListeners;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class DiscordBot {

    public static void main(String[] args) throws LoginException {
        //Main CLass to run your bot
        final String token = "YourToken";
        JDABuilder builder = JDABuilder.createDefault(token);

        JDA jda =builder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(
                        new ReadyEventListeners()
                        ,new AllEventListeners())
                .build();

        jda.upsertCommand("raider-io-character","Donne des informations concernant le joueur(Ysondre)").setGuildOnly(false).queue();
        OptionData option1 = new OptionData(OptionType.STRING, "realm", "Name of the realm", true);
        OptionData option2 = new OptionData(OptionType.STRING, "player", "Name of player", true);
        jda.upsertCommand("mythicplus", "Show the mythic + score of the current player").addOptions(option1,option2).setGuildOnly(false).queue();
        OptionData option3 = new OptionData(OptionType.STRING, "guildname", "Name of the guild", true);
        jda.upsertCommand("guildranking", "Show the ranking of the current guild").addOptions(option1,option3).setGuildOnly(false).queue();
        OptionData option4 = new OptionData(OptionType.STRING, "name", "Name",false);
        jda.upsertCommand("register","register yourself to the database of the bot").addOptions(option4).setGuildOnly(false).queue();

    }
}
