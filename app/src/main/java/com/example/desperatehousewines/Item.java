package com.example.desperatehousewines;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import org.apache.commons.codec.binary.StringUtils;
import org.dhatim.fastexcel.reader.Row;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Item {
    final String TAG = "ITEM";

    public enum Type {
        NONE(""),
        RED_WINE("Punaviini"),
        ROSEE_WINE("Roseeviini"),
        WHITE_WINE("Valkoviini"),
        RUM("Rommi"),
        COGNAC("Konjakki"),
        WHISKEY("Viski"),
        BEER("Olut"),
        CIDER("Siideri"),
        BLEND("Sekoitus"),
        NON_ALCOHOL("Alkoholiton"),
        OTHER("Muu"),
        DESSERT_WINE("Jälkiruokaviini"),
        BRANDY("Brandy"),
        GIN("Gini"),
        LIQUOR("Likööri"),
        SPARKLING_WINE("Kuohuviini"),
        VODKA("Vodka");

        private String name;

        Type(String n) {
            this.name = n;
        }

        @Override
        public String toString() { return this.name; }
    }

    /*

        Idx XML Header/JSON Key     Variable    Data Type   Raw Data Example                                Notes
            ------------------------------------------------------------------------------------------------------------------
            id                      id          int         0                                               Auto incremented database ID
        0   Numero	                number      long        945096                                          Is unique
        1   Nimi	                name        String      Adrianna Vineyard Fortuna Terrae Malbec 2015    -
        2   Valmistaja	            producer    String      Catena Zapata                                   -
        3   Pullokoko	            size        float       0,75 l                                          Inconsistent decimal separator; uses a comma where other columns use dot. Contains the unit within data.
        4   Hinta                   price       float       84.99                                           -

        5   Litrahinta		        -                       113.32                                          Can be derived.
        6   Uutuus                  isNew       bool        uutuus                                          Not new items are empty.
        7   Hinnastojärjestyskoodi  -                       110                                             Seems to be actually some sort a category, can have string values sometimes.
        8   Tyyppi                  type        String      punaviinit                                      -
        9   Alatyyppi               subType     String      Mehevä & Hilloinen                              -

        10  Erityisryhmä            -                       ympäristövastuullinen pakkaus                   -
        11  Oluttyyppi              -                       erikoisuus                                      -
        12  Valmistusmaa            country     String      Argentiina                                      -
        13  Alue                    area        String      Mendoza                                         -
        14  Vuosikerta              year        int         2015                                            'Nimi' also contains this information, but not sure if they are always the same.

        15  Etikettimerkintöjä      extraLabel  String      Mendoza                                         Sometimes matches with 'Alue'. This might indicate which winery if the area has many.
        16  Huomautus               extraInfo   String      Sisältää sakkaa                                 Random info, nearly always empty.
        17  Rypäleet                grapes      String[]    Malbec,                                         Can end in a comma.
        18  Luonnehdinta            keywords    String[]    Täyteläinen, tanniininen, karhunvatukka...      Contains multiple characterization keywords separated by a comma, but doesn't actually contain that many unique ones.
        19  Pakkaustyyppi           packaking   String      pullo                                           -

        20  Suljentatyyppi          stopper     String      luonnonkorkki                                   -
        21  Alkoholi-%              alcohol     float       14.0                                            -
        22  Hapot g/l               -                       5.6                                             -
        23  Sokeri g/l              -                       2                                               -
        24  Kantavierrep-%          -                       2                                               -

        25  Väri EBC                -                       2                                               -
        26  Katkerot EBU            -                       2                                               -
        27  Energia kcal/100 ml     -                       80.0                                            -
        28  Valikoima               selection   String      tilausvalikoima                                 -
        29  EAN                     ean         long        7794450005274                                   Almost unique, but less than 'Numero'.

    Raw JSON example:
        "id":217839,
        "Numero":945096,
        "Nimi":"Adrianna Vineyard Fortuna Terrae Malbec 2015",
        "Valmistaja":"Catena Zapata",
        "Pullokoko":"0,75 l",
        "Hinta":85,
        "Litrahinta":113.3,
        "Uutuus":"",
        "Hinnastojarjestyskoodi":110,
        "Tyyppi":"punaviinit",
        "Alatyyppi":"MehevÃ¤ & Hilloinen",
        "Erityisryhma":"",
        "Oluttyyppi":"",
        "Valmistusmaa":"Argentiina",
        "Alue":"Mendoza",
        "Vuosikerta":2015,
        "Etikettimerkintoja":"",
        "Huomautus":"",
        "Rypaleet":"Malbec, ",
        "Luonnehdinta":"TÃ¤ytelÃ¤inen, tanniininen, karhunvatukkainen, mausteinen, aromikas",
        "Pakkaustyyppi":"pullo",
        "Suljentatyyppi":"luonnonkorkki",
        "Alkoholi_prosentti":14,
        "Hapot_g_l":5.6,
        "Sokeri_g_l":2,
        "Kantavierreprosentti":0,
        "Vari_EBC":0,
        "Katkerot_EBU":0,
        "Energia_kcal_100_ml":80,
        "Valikoima":"tilausvalikoima",
        "EAN":2147483647

    Parser HashMap
            Key     JSON's key
            Value   Standard functional interface Consumer<T> and implemented as Consumer<String> because
                    Cell.getRawValue() returns always a String which is then parsed by the implementation.

                    Consumer<T> details: https://docs.oracle.com/javase/8/docs/api/java/util/function/Consumer.html
     */
    Map<String, Consumer<String>> jsonParsers = new HashMap<String, Consumer<String>>() {{
        put("id", (String r) -> id = parseValue(r, 0, true));

        put("Numero", (String r) -> number = parseValue(r, 1L, true));
        put("Nimi", (String r) -> name = parseValue(r, "", true).trim());
        put("Valmistaja", (String r) -> producer = parseValue(r, "", false));
        put("Pullokoko", (String r) -> size = parseValue(r.split(" ", -1)[0].replace(",", "."), 0.0f, false));
        put("Hinta", (String r) -> price = parseValue(r, 0.0f, true));

        put("Uutuus", (String r) -> isNew = parseValue(r, false, false, "uutuus"));
        put("Tyyppi", (String r) -> type = parseValue(r, Type.NONE, true));
        put("Alatyyppi", (String r) -> subType = parseValue(r, "", false));

        put("Valmistusmaa", (String r) -> country = parseValue(r, "", false));
        put("Alue", (String r) -> area = parseValue(r, "", false));
        put("Vuosikerta", (String r) -> year = parseValue(r, 0, false));

        put("Etikettimerkintoja", (String r) -> extraLabel = parseValue(r, "", false));
        put("Huomautus", (String r) -> extraInfo = parseValue(r, "", false));
        put("Rypaleet", (String r) -> grapes = parseValue(r.split(","), new ArrayList<String>(), false));
        put("Luonnehdinta", (String r) -> keywords = parseValue(r.split(","), new ArrayList<String>(), false));
        put("Pakkaustyyppi", (String r) -> packageType = parseValue(r, "", false));

        put("Suljentatyyppi", (String r) -> stopper = parseValue(r, "", false));
        put("Alkoholi_prosentti", (String r) -> alcohol = parseValue(r, -1, false));

        put("Valikoima", (String r) -> selection = parseValue(r, "", false));
        put("EAN", (String r) -> ean = parseValue(r, 0L, false));
    }};

    // Map excel columns to JSON keys.
    Map<Integer, String> indexToKey = new HashMap<Integer, String>() {{
        put(0, "Numero");
        put(1, "Nimi");
        put(2, "Valmistaja");
        put(3, "Pullokoko");
        put(4, "Hinta");

        put(6, "Uutuus");
        put(8, "Tyyppi");
        put(9, "Alatyyppi");

        put(12, "Valmistusmaa");
        put(13, "Alue");
        put(14, "Vuosikerta");

        put(15, "Etikettimerkintoja");
        put(16, "Huomautus");
        put(17, "Rypaleet");
        put(18, "Luonnehdinta");
        put(19, "Pakkaustyyppi");

        put(20, "Suljentatyyppi");
        put(21, "Alkoholi_prosentti");

        put(28, "Valikoima");
        put(29, "EAN");
    }};

    // Starts as true (valid) and set to false by the parsers if the data is required.
    boolean isValid = true;

    int id;

    long number;
    String name;
    String producer;
    float size;
    float price;

    boolean isNew;
    Type type;
    String subType;

    String country;
    String area;
    int year;

    String extraLabel;
    String extraInfo;
    ArrayList<String> grapes = new ArrayList<>();
    ArrayList<String> keywords = new ArrayList<>();
    String packageType;

    String stopper;
    float alcohol;
    String selection;

    long ean;

    String cardTitle;
    String subHeader;

    // Constructor for parsing excel sheet
    public Item (Row r) {
        // Iterates through parsers and tries to fetch data from the given row.
        for (HashMap.Entry<Integer, String> e : indexToKey.entrySet()) {
            int mapIndex = e.getKey();
            String mapValue = e.getValue();

            // Parser is called with an empty string if no data is found for that index.
            if (jsonParsers.containsKey(mapValue)) {
                if (r.hasCell(mapIndex)) {
                    String raw = r.getCell(mapIndex).getRawValue();
                    jsonParsers.get(mapValue).accept(raw != null ? raw : "");
                } else {
                    jsonParsers.get(mapValue).accept("");
                }
            }
        }
    }

    // Constructor for parsing JSON object.
    public Item (JSONObject r) {
        for (HashMap.Entry<String, Consumer<String>> e : jsonParsers.entrySet()) {
            String key = e.getKey();
            Consumer<String> parser = e.getValue();

            if (r.has(key)) {
                try {
                    parser.accept(r.getString(key));
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            } else {
                parser.accept("");
            }
        }
    }

    public String getGrapes () {
        return TextUtils.join(", ", grapes);
    }

    public String getKeywords () {
        return TextUtils.join(", ", keywords);
    }

    public String getName () {
        return name;
    }

    public String getCardTitle () {
        return cardTitle.length() < 5 ? name : cardTitle;
    }

    public String getAlcoholAsString() {
        return alcohol > 0 ? alcohol + " %" : "";
    }

    public String getSizeAsString() {
        return size > 0 ? size + " l" : "";
    }

    public String getPriceAsString() {
        return price >  0 ? price + " e" : "";
    }

    public boolean isWine() {
        switch (type) {
            case RED_WINE:
            case ROSEE_WINE:
            case WHITE_WINE:
            case DESSERT_WINE:
            case SPARKLING_WINE:
                return true;
        }

        return false;
    }

    public int getYear() {
        return year;
    }

    public String getYearAsString() {
        return year > 0 ? String.valueOf(year) : "";
    }

    public String getProducer () {
        return producer;
    }

    public Bundle getBundle () {
        Bundle b = new Bundle();

        b.putInt("id", id);
        b.putString("number", Long.toString(number));
        b.putString("name", name);
        b.putString("producer", producer);
        b.putString("size", getSizeAsString());
        b.putString("price", getPriceAsString());

        b.putBoolean("isnew", isNew);
        b.putString("type", type.toString());
        b.putString("subtype", subType);

        b.putString("country", country);
        b.putString("year", getYearAsString());
        b.putString("area", area);

        b.putString("extralabel", extraLabel);
        b.putString("extrainfo", extraInfo);
        b.putString("grapes", getGrapes());
        b.putString("keywords", getKeywords());
        b.putString("packagetype", packageType);

        b.putString("stopper", stopper);
        b.putString("alcohol", getAlcoholAsString());
        b.putString("selection", selection);

        b.putString("ean", Long.toString(ean));

        b.putString("cardtitle", cardTitle);
        b.putString("subheader", subHeader);

        return b;
    }

    public int getId() {
        return id;
    }

    private int parseValue (String str, int def, boolean isRequired) {
        try {
            return Integer.parseInt(str);
        } catch(NumberFormatException ex) {
            if (isRequired)
                isValid = false;

            return def;
        }
    }

    private float parseValue (String str, float def, boolean isRequired) {
        try {
            return Float.parseFloat(str);
        } catch(NumberFormatException ex) {
            if (isRequired)
                isValid = false;

            return def;
        }
    }

    private long parseValue (String str, long def, boolean isRequired) {
        try {
            return Long.parseLong(str);
        } catch(NumberFormatException ex) {
            if (isRequired)
                isValid = false;

            return def;
        }
    }

    private ArrayList<String> parseValue (String[] s, ArrayList<String> def, boolean isRequired) {
        for (int i = 0; i < s.length; i++) {
            String trim = s[i].trim().toLowerCase();

            if (trim.length() > 0)
                def.add(trim);
        }

        if (def.size() == 0) {
            if (isRequired)
                isValid = false;

            def.add("");
        }

        return def;
    }

    private String parseValue (String str, String def, boolean isRequired) {
        if (str == null) {
            if (isRequired)
                isValid = false;

            return def;
        }

        String trimmed = str.trim();

        if (trimmed.isEmpty() || trimmed == "null") {
            if (isRequired)
                isValid = false;

            return def;
        } else {
            return trimmed;
        }
    }

    private boolean parseValue (String str, boolean def, boolean isRequired, String compare) {
        return str.toLowerCase() == compare ? true : def;
    }

    private Type parseValue (String str, Type def, boolean isRequired) {
        switch (str) {
            case "punaviinit": return Type.RED_WINE;
            case "roseeviinit": return Type.ROSEE_WINE;
            case "valkoviinit": return Type.WHITE_WINE;
            case "rommit": return Type.RUM;
            case "konjakit": return Type.COGNAC;
            case "viskit": return Type.WHISKEY;
            case "oluet": return Type.BEER;
            case "siiderit": return Type.CIDER;
            case "juomasekoitukset": return Type.BLEND;
            case "alkoholittomat": return Type.NON_ALCOHOL;
            case "lahja- ja juomatarvikkeet":
                if (isRequired)
                    isValid = false;

                return Type.OTHER;
            case "Jälkiruokaviinit, väkevöidyt ja muut viinit": return Type.DESSERT_WINE;
            case "Brandyt, Armanjakit ja Calvadosit": return Type.BRANDY;
            case "Ginit ja maustetut viinat": return Type.GIN;
            case "Liköörit ja Katkerot": return Type.LIQUOR;
            case "kuohuviinit & samppanjat": return Type.SPARKLING_WINE;
            case "vodkat ja viinat": return Type.VODKA;
            default:
                if (isRequired)
                    isValid = false;
                
                return def;
        }
    }

    public boolean isValid () {
        return this.isValid;
    }

    private static String[] filteredWords = {
            "muovipullo",
            "50cl",
            "UKKO",
            "lahjapakkaus",
            "brut nature",  "extra Brut",   "brut",
            "extra sec",    "extra Dry",    "extra trocken",    "extra seco",
            "sec",          "dry",          "trocken",          "secco",
            "demi sec",     "halbtrocken",  "medium dry",
            "sweet",        "doux",         "dolce"
    };

    public Item createCardTitle () {
        cardTitle = name;

        // Remove special characters.
        cardTitle = cardTitle.replaceAll("[-+.^:,*]","");

        // Grab year from name if it's there.
        String yearFromName = cardTitle.trim().replaceAll("\\D", "");

        if (tryParseInt(yearFromName)) {
            int yearParsed = Integer.parseInt(yearFromName);

            if (yearParsed > 1900 && yearParsed < 2100) {
                // If we don't have a year, set it as one.
                if (year < 1) {
                    Log.d(TAG, "[gum] found year from name: " + year);
                    year = yearParsed;
                }

                cardTitle = cardTitle.replaceAll(yearFromName, "");
            }
        }

        // Remove producer from the name.
        cardTitle = cardTitle.replaceAll(producer, "");

        // Wine specific bubble gum.
        if (isWine()) {
            // Remove grapes from the name.
            for (String s : grapes)
                cardTitle = cardTitle.replaceAll(s, "");
        }

        // Remove unwanted words from name.
        for (String f : filteredWords) {
            String fCapped = f.substring(0, 1).toUpperCase() + f.substring(1);
            cardTitle = cardTitle.replaceAll(f, "").replaceAll(fCapped, "");
        }

        // Remove trailing and leading spaces and double spaces.
        cardTitle = cardTitle.trim().replaceAll(" +", " ");

        createSubHeader();
        return this;
    }

    private void createSubHeader() {
        List<String> sub = new ArrayList<>();

        if (year > 0)
            sub.add(getYearAsString());

        sub.add(type.toString());

        if (!country.equals(""))
            sub.add(country);

        if (!producer.equals(""))
            sub.add(producer);

        subHeader = TextUtils.join(" - ", sub);
    }

    private boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Strictly for debugging purposes.
    @NonNull
    @Override
    public String toString() {
        return  "ITEM" +
            "\nnumber\t\t: " + number +
            "\nname\t\t: " + name +
            "\nproducer\t: " + producer +
            "\nsize\t\t: " + size +
            "\nprice\t\t: " +  price +
            "\nisNew\t\t: " + isNew +
            "\ntype\t\t: " + type.toString() +
            "\nsubType\t\t: " + subType +
            "\ncountry\t\t: " + country +
            "\narea\t\t: " + area +
            "\nyear\t\t: " + year +
            "\nextraLabel\t: " + extraLabel +
            "\nextraInfo\t: " + extraInfo +
            "\nkeywords\t: (" + keywords.size() + ") " + getKeywords() +
            "\ngrapes\t\t: (" + grapes.size() + ") " + getGrapes() +
            "\npackageType\t: " + packageType +
            "\nstopper\t\t: " + stopper +
            "\nalcohol\t\t: " + alcohol +
            "\nselection\t: " + selection +
            "\nean\t\t\t: " + ean;
    }
}
