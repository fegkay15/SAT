package com.fegeley.steamactivitytracker;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.renderscript.Element;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


public class MainActivity2 extends ActionBarActivity {

    private String steamID;
    private Drawable drawable = null;
    myKey returnKey = new myKey();
    private String key = returnKey.returnMyKey();
    private String profStatURL = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?format=xml&key=" + key + "&steamids=";
    private String gamesURL = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?format=xml&key=" + key + "&steamid=";
    private Document profile;
    private Document games;

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

    public String getGames(String tag){
        XMLParser parser = new XMLParser();
        NodeList play = games.getElementsByTagName("players");

        if(play.getLength() > 0){
            Node n = play.item(0);
            Node child = n.getFirstChild();
            if(child == null){
                return null;
            }else {
                NodeList nl = games.getElementsByTagName("player");
                org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(0);
                return parser.getValue(e, tag);
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
                " items, currently valued at " + libraryPrice() + ", requires " + librarySize() + "GB, and you've spent " + libraryHours() + " hours playing it.");
    }

    public String libraryPrice(){
        return "$xxxx.xx";
    }

    public String librarySize(){
        return "xxxx.x";
    }

    public String libraryHours(){
        return "xxxx.x";
    }

    public Document getXMLFromJSON (String url){
        String xml = "";
        XMLParser parser = new XMLParser();
        JsonReader read = new JsonReader();
        JSONObject json;
        try {
            json = read.readJsonFromUrl(url);
            xml = XML.toString(json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parser.getDomElement(xml); // getting DOM element
    }
}
