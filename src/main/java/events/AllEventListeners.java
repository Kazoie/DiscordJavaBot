package events;

import com.google.gson.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.*;

public class AllEventListeners extends ListenerAdapter {
    //Main class, providing information about the character with 1st command (not optimized yet) and mythic score + ranking with 2nd

    //Our Global dataBase, used to stock /register command player and supposed to be stocked into a txt
    HashMap<String,List<int>> scoreBase = new HashMap<String, List<int>>();
    HashMap<String,HashMap<String,int>> dataBase = new HashMap<String, HashMap<String,int>>();

    /*
    The database is build like this =
    id => the discord id of the player using /register command to add himself at the dataBase
    name => name of a specific character
    m+ => a list of int sorted to show the progression of mythic+ score
     */

    /////////////////////////////////////////////////////
    //     |         name           |  List<Score>m+   //
    //     |------------------------|------------------//
    // id  |         name           |  List<Score>m+   //
    //     |------------------------|------------------//
    //     |         name           |  List<Score>m+   //
    /////////////////////////////////////////////////////
    //Illustration of the database

    final static String file = "dataBase.txt";

    public void mythicplus(SlashCommandInteractionEvent event){
        OptionMapping option1 = event.getOption("realm");
        OptionMapping option2 = event.getOption("player");
        String playerRealm = option1.getAsString();
        String playerName = option2.getAsString();
        try {
            // CREATING URL & REQUEST CONNECTION
            // FIRST REQUEST
            URL url = new URL(
                    "https://raider.io/api/v1/characters/profile?region=eu&realm="
                            +URLEncoder.encode(playerRealm, "UTF-8")
                            +"&name="+URLEncoder.encode(playerName, "UTF-8")
                            +"&fields=mythic_plus_scores_by_season:current");
            HttpsURLConnection req = (HttpsURLConnection) url.openConnection();
            req.setRequestMethod("GET");

            //SECOND REQUEST
            URL url2 = new URL(
                    "https://raider.io/api/v1/characters/profile?region=eu&realm="
                            +URLEncoder.encode(playerRealm, "UTF-8")
                            +"&name="+URLEncoder.encode(playerName, "UTF-8")
                            +"&fields=mythic_plus_ranks");
            HttpsURLConnection req2 = (HttpsURLConnection) url2.openConnection();
            req2.setRequestMethod("GET");

            if(req.getResponseCode() == 200 && req2.getResponseCode() == 200) {

                //retrieving information and writing it
                InputStream inputStream = req.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String input;
                StringBuffer reponse = new StringBuffer();
                while ((input = in.readLine()) != null) {
                    reponse.append(input);
                }
                in.close();

                // same thing but the 2nd request
                InputStream inputStream2 = req2.getInputStream();
                BufferedReader in2 = new BufferedReader(new InputStreamReader(inputStream2));
                String input2;
                StringBuffer reponse2 = new StringBuffer();
                while ((input2 = in2.readLine()) != null) {
                    reponse2.append(input2);
                }
                in2.close();

                //FIRST REQUEST TREATMENT
                String info = reponse.toString();
                JsonObject object = new JsonParser().parse(info).getAsJsonObject();
                //Part to get the mythic + score (float)
                JsonArray array = object.get("mythic_plus_scores_by_season").getAsJsonArray();
                JsonObject item = array.get(0).getAsJsonObject().get("scores").getAsJsonObject();

                float dps,heal,tank;
                dps = item.get("dps").getAsFloat();
                heal = item.get("healer").getAsFloat();
                tank = item.get("tank").getAsFloat();

                //SECOND REQUEST TREATMENT
                String infoRank = reponse2.toString();
                JsonObject object2 = new JsonParser().parse(infoRank).getAsJsonObject();
                object2 =  object2.get("mythic_plus_ranks").getAsJsonObject();

                JsonObject objectDPS = null;
                JsonObject objectHeal = null;
                JsonObject objectTank = null;

                String[] tab = new String[]{"class_dps","class_healer","class_tank"};
                for (int i = 0; i < tab.length; i++) {
                    if(object2.get(tab[i])!=null) {
                        switch(i){
                            case 0:objectDPS = object2.get(tab[i]).getAsJsonObject();
                                break;
                            case 1:objectHeal =object2.get(tab[i]).getAsJsonObject();
                                break;
                            case 2: objectTank =object2.get(tab[i]).getAsJsonObject();
                        }
                    }
                }
                int world,region,realm; // variable to provide rankings according to his ranks
                String message;
                message = "Le joueur "+object.get("name") + " a une cote mythique : ";

                if(dps >0) {
                    world = objectDPS.get("world").getAsInt();
                    region = objectDPS.get("region").getAsInt();
                    realm = objectDPS.get("realm").getAsInt();
                    message += "\n DPS : "+dps + " avec le classement suivant, World : "+world + " Region : "+region + " Royaume : "+realm;
                }
                if (heal > 0) {
                    world = objectHeal.get("world").getAsInt();
                    region = objectHeal.get("region").getAsInt();
                    realm = objectHeal.get("realm").getAsInt();
                    message +=  "\n Healer : "+heal + " avec le classement suivant, World : "+world + " Region : "+region + " Royaume : "+realm;
                }
                if (tank >0) {
                    world = objectTank.get("world").getAsInt();
                    region = objectTank.get("region").getAsInt();
                    realm = objectTank.get("realm").getAsInt();
                    message += "\n Tank : "+tank + " avec le classement suivant, World : "+world + " Region : "+region + " Royaume : "+realm;
                }

                event.reply(message).queue(); // PRINT

                // case no mythic score
                if (heal == 0.0 && tank == 0.0 && dps ==0.0) {
                    event.reply("Il semblerait que ce joueur ne possède aucune cote mythique, Je ne fournirai donc aucune information").queue();
                }

            } else event.reply("Erreur dans l'une des deux requètes : REQ1 : "+req.getResponseCode() + " REQ2 : "+req2.getResponseCode()).queue();

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void guildRanking(SlashCommandInteractionEvent event){
    OptionMapping option1 = event.getOption("realm");
    OptionMapping option2 = event.getOption("guildname");
    String realm = option1.getAsString();
    String guild = option2.getAsString();
    try {
        URL url = new URL("https://raider.io/api/v1/guilds/profile?region=eu&realm="
                + URLEncoder.encode(realm, "UTF-8")
                + "&name=" + URLEncoder.encode(guild, "UTF-8")
                + "&fields=raid_progression");
        HttpsURLConnection req = (HttpsURLConnection) url.openConnection();
        req.setRequestMethod("GET");

        if (req.getResponseCode() == 200){

            InputStream inputStream = req.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String input;
            StringBuffer reponse = new StringBuffer();
            while ((input = in.readLine()) != null) {
                reponse.append(input);
            }
            in.close();

            String info = reponse.toString();
            JsonObject object = new JsonParser().parse(info).getAsJsonObject();
            JsonObject item = object.get("raid_progression").getAsJsonObject().get("vault-of-the-incarnates").getAsJsonObject();
            int NM, HM, MM;
            NM = item.get("normal_bosses_killed").getAsInt();
            HM = item.get("heroic_bosses_killed").getAsInt();
            MM = item.get("mythic_bosses_killed").getAsInt();
            String msg = ("La guilde " +guild+" affiche le progress suivant : "
                    + "\n Normal mode : "+NM+ "/8"
                    + "\n Heroic mode : "+HM+ "/8"
                    + "\n Mythic mode : "+MM+ "/8");
            event.reply(msg).queue();
        }

        } catch (MalformedURLException e) {
        throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
        } catch (ProtocolException e) {
        throw new RuntimeException(e);
        } catch (IOException e) {
        throw new RuntimeException(e);
    }
    }
    public void raiderIoCharacter(SlashCommandInteractionEvent event){
        String name = event.getMember().getEffectiveName(); // Get the name of user using command
        try {
            URL url = new URL("https://raider.io/api/v1/characters/profile?region=eu&realm=Ysondre&name="+ URLEncoder.encode(name, "UTF-8"));
            HttpsURLConnection req = (HttpsURLConnection) url.openConnection();
            req.setRequestMethod("GET");
            if (req.getResponseCode() == 200) {
                InputStream inputStream = req.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String input;
                StringBuffer reponse = new StringBuffer();
                while ((input = in.readLine()) != null) {
                    reponse.append(input);
                }
                in.close();
                String info = reponse.toString();
                JsonObject object = new JsonParser().parse(info).getAsJsonObject();

                String profile_url = object.get("profile_url").toString();
                profile_url = profile_url.replace("\"", "");
                event.reply("Le joueur " + object.get("name") + " est un " + object.get("race") + " de la classe" +
                        object.get("class") + " spé " + object.get("active_spec_name") + " voici un lien direct vers sa page RIO : " + profile_url).queue();
            } else event.reply("Erreur dans la requète, code : " +req.getResponseCode()).queue();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void register(SlashCommandInteractionEvent event) {
    String id = (event.getMember().getId());
    //TODO
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        String command = event.getName();

        //switch command to know wich one has been choose
        switch (command) {
            case ("raider-io-character"): //1st command
                raiderIoCharacter(event);
                break;
            case ("mythicplus"): // 2nd command
               mythicplus(event);
               break;
            case ("guildranking"):
                guildRanking(event);
                break;
            case ("register")   :
                register(event);
                break;
        }









    }
}

