package com.fegeley.steamgametracker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity2 extends ActionBarActivity {

    private String steamID;
    private Drawable drawable = null;
    myKey returnKey = new myKey();
    private String key = returnKey.returnMyKey();
    private String profStatURL = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?format=xml&key=" + key + "&steamids=";
    private String gamesURL = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?format=xml&key=" + key + "&steamid=";
    private String gamePage = "http://store.steampowered.com/api/appdetails/?appids=";
    private int[] time;
    private int[] ids;
    private double[] price;
    private double[] appsFormatedPrice;
    private Document profile;
    private Document games;
    private String[] appsUnFormatedPrice;
    private JSONObject[] apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        TextView textView8 = (TextView) findViewById(R.id.textView8);
        Intent intent = getIntent();
        steamID = intent.getStringExtra("steamID");
        steamID = idTypeProfile();
        if(steamID != "errornotarealaccounterrornotarealaccount") {
            profStatURL = profStatURL + steamID;
            profile = getXML(profStatURL);
            gamesURL = gamesURL + steamID;
            games = getXML(gamesURL);
            if (getProfile("communityvisibilitystate").equals("3")) {
                    setTitle(getProfile("personaname"));
                    new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                            .execute(getProfile("avatarfull"));
                descriptionHead();
            } else if (getProfile("communityvisibilitystate").equals("1")) {
                Toast.makeText(getApplicationContext(), "This Profile is not Public!!", Toast.LENGTH_LONG).show();
                super.finish();
            } else {
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                super.finish();
            }
        }else{
            Toast.makeText(getApplicationContext(), "This is Not a Valid Account!!", Toast.LENGTH_LONG).show();
            super.finish();
        }

    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
    //    return true;
    //}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String idTypeProfile(){
        if(steamID.length() != 17 && steamID.matches("[0-9]+") == false){
            final String URL ="http://steamcommunity.com/id/" + steamID + "/?xml=1";
            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(URL); // getting XML
            Document doc = parser.getDomElement(xml); // getting DOM element
            if(doc.getFirstChild().getNodeName().equals("profile")){
                NodeList nl = doc.getElementsByTagName("profile");
                org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(0);
                return parser.getValue(e, "steamID64");
            }else{
                return "errornotarealaccounterrornotarealaccount";
            }

        }else{
            return steamID;
        }
    }

    public Document getXML(String URL){
        XMLParser parser = new XMLParser();
        String xml = parser.getXmlFromUrl(URL); // getting XML
        return parser.getDomElement(xml); // getting DOM element
    }

    public String getProfile(String tag){
        XMLParser parser = new XMLParser();
        NodeList play = profile.getElementsByTagName("players");

        if(play.getLength() > 0){
            Node n = play.item(0);
            Node child = n.getFirstChild();
            if(child == null){
                return null;
            }else {
                NodeList nl = profile.getElementsByTagName("player");
                org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(0);
                return parser.getValue(e, tag);
            }
        }

        return "error";
    }

    public String gameCount(){
        XMLParser parser = new XMLParser();
        NodeList nl = games.getElementsByTagName("response");
        org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(0);
        return parser.getValue(e, "game_count");
    }

    public String getGameTime(){
        XMLParser parser = new XMLParser();
        NodeList play = games.getElementsByTagName("games");

        if(play.getLength() > 0){
            Node n = play.item(0);
            Node child = n.getFirstChild();
            double totalTime = 0;
            if(child == null){
                return null;
            }else {
                NodeList nl = games.getElementsByTagName("message");
                time = new int[nl.getLength()];
                for(int i = 0; i < nl.getLength(); i++) {
                    org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(i);
                    time[i] = Integer.parseInt(parser.getValue(e, "playtime_forever"));
                }
                for(int i = 0; i < nl.getLength(); i++){
                    totalTime = totalTime + (double)time[i];
                }
                totalTime = totalTime/60;
                NumberFormat formatter = new DecimalFormat("#0.0");
                return formatter.format(totalTime);
            }
        }

        return "error";
    }

    public static int getDiffYears(Date first) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(Calendar.getInstance().getTime());
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    public void descriptionHead(){
        int creationTime = new Integer(getProfile("timecreated"));
        Date date = new Date();
        date.setTime((long) creationTime * 1000);
        TextView description = (TextView) findViewById(R.id.textView8);
        description.setText("Over the " + getDiffYears(date) + "+ years you have been on Steam, your library has grown to " + gameCount() +
                " items, currently valued at $" + libraryPrice() + ", requires " + librarySize() + "GB, and you've spent " + getGameTime() + " hours playing it.");
    }

    public String libraryPrice(){
        XMLParser parser = new XMLParser();
        NodeList play = games.getElementsByTagName("games");

        if(play.getLength() > 0){
            Node n = play.item(0);
            Node child = n.getFirstChild();
            double totalTime = 0;
            if(child == null){
                return null;
            }else {
                NodeList nl = games.getElementsByTagName("message");
                ids = new int[nl.getLength()];
                for(int i = 0; i < nl.getLength(); i++) {
                    org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(i);
                    ids[i] = Integer.parseInt(parser.getValue(e, "appid"));
                }
                apps = new JSONObject[nl.getLength()];
                for(int i = 0; i < nl.getLength(); i++){
                    try {
                        apps[i] = JsonReader.readJsonFromUrl(gamePage + ids[i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                appsUnFormatedPrice = new String[nl.getLength()];
                for(int i = 0; i < nl.getLength(); i++){
                        appsUnFormatedPrice[i] = getAppPrice(apps[i]);
                }
                appsFormatedPrice = new double[nl.getLength()];
                for(int i = 0; i < nl.getLength(); i++){
                    if(appsUnFormatedPrice[i].length() < 3) {
                        appsUnFormatedPrice[i] = "00" + appsUnFormatedPrice[i];
                    }
                    appsFormatedPrice[i] = Double.parseDouble(new StringBuffer(appsUnFormatedPrice[i]).insert(appsUnFormatedPrice[i].length() - 2, ".").toString());
                }
                double totalPrice = 0.0;
                for(int i = 0; i <nl.getLength(); i++){
                    System.out.println(appsFormatedPrice[i]);
                    totalPrice = totalPrice + appsFormatedPrice[i];
                }
                NumberFormat formatter = new DecimalFormat("#0.00");
                return formatter.format(totalPrice);
            }
        }
        return "xxxx.xx";
    }

    public String librarySize(){
        return "xxxx.x";
    }

    public String getAppPrice(JSONObject json){
        String result = null;
        String bool = json.toString().substring(json.toString().indexOf("\"is_free\":") + 10, json.toString().indexOf("\"is_free\":") + 15);
        if(bool.equals("false")) {
            result = json.toString().substring(json.toString().indexOf("\"final\":") + 8, json.toString().indexOf(",\"discount_percent\""));
        } else {
            result = "0000";
        }
        System.out.println(result);
        return result;
    }
}
