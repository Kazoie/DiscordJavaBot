import events.*;
import events.MessageEventListener;
import events.ReadyEventListeners;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class DiscordBot {

    public static void main(String[] args) throws LoginException {
        //Main CLass to run your bot
        final String token = "MTA2ODYxNjExMzY4MTg2Njg4Mw.G7fE-P.J95BrENuC6uhAqpuPvQZme_DK9OORyG5BYN3vc";
        JDABuilder builder = JDABuilder.createDefault(token);

        JDA jda =builder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(
                        new ReadyEventListeners()
                        ,new RaiderEventListener())
                .build();


        jda.upsertCommand("raider-io","Donne des informations concernant le joueur(Ysondre)").setGuildOnly(true).queue();

    }
}
