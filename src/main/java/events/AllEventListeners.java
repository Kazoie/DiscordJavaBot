package events;

import com.google.gson.*;
import kotlin.Triple;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class AllEventListeners extends ListenerAdapter {
    //Main class, providing information about the character with 1st command (not optimized yet) and mythic score + ranking with 2nd

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        String command = event.getName();

        //switch command to know wich one has been choose
        switch (command) {

            case ("raider-io-character"): //1st command
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
                break;

            case ("mythicplus"): // 2nd command
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
                        event.reply(message).queue();

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
                break;
        }









    }
}

