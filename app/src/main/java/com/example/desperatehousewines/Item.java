package com.example.desperatehousewines;


import androidx.annotation.NonNull;

import org.dhatim.fastexcel.reader.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

enum Type {
    NONE,
    RED_WINE,
    ROSEE_WINE,
    WHITE_WINE,
    RUM,
    COGNAC,
    WHISKEY,
    BEER,
    CIDER,
    BLEND,
    NON_ALCOHOL,
    OTHER,
    DESSERT_WINE,
    BRANDY,
    GIN,
    LIQUOR,
    SPARKLING_WINE,
    VODKA
}

public class Item {
    final String TAG = "ITEM";

    /*
            Column                  Variable    Data Type   Raw Data Example                                Notes
            ------------------------------------------------------------------------------------------------------------------
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
     */

    /*  Parser HashMap
            Key     Cell's index, see the table above.
            Value   Standard functional interface Consumer<T> and implemented as Consumer<String> because
                    Cell.getRawValue() returns always a String which is then parsed by the implementation.

                    Consumer<T> details: https://docs.oracle.com/javase/8/docs/api/java/util/function/Consumer.html
     */
    Map<Integer, Consumer<String>> parsers = new HashMap<Integer, Consumer<String>>() {{
        put(0, (String r) -> number = parseValue(r, 1L, true));
        put(1, (String r) -> name = parseValue(r, "", true));
        put(2, (String r) -> producer = parseValue(r, "", false));
        put(3, (String r) -> size = parseValue(r.split(" ", -1)[0].replace(",", "."), 0.0f, false));
        put(4, (String r) -> price = parseValue(r, 0.0f, true));

        put(6, (String r) -> isNew = parseValue(r, false, false, "uutuus"));
        put(8, (String r) -> type = parseValue(r, Type.NONE, true));
        put(9, (String r) -> subType = parseValue(r, "", false));

        put(12, (String r) -> country = parseValue(r, "", false));
        put(13, (String r) -> area = parseValue(r, "", false));
        put(14, (String r) -> year = parseValue(r, 0, false));

        put(15, (String r) -> extraLabel = parseValue(r, "", false));
        put(16, (String r) -> extraInfo = parseValue(r, "", false));
        put(17, (String r) -> grapes = parseValue(r.split(","), new ArrayList<String>(), false));
        put(18, (String r) -> keywords = parseValue(r.split(","), new ArrayList<String>(), false));
        put(19, (String r) -> packageType = parseValue(r, "", false));

        put(20, (String r) -> stopper = parseValue(r, "", false));
        put(21, (String r) -> alcohol = parseValue(r, -1, false));

        put(28, (String r) -> selection = parseValue(r, "", false));
        put(29, (String r) -> ean = parseValue(r, 0L, false));
    }};

    // Starts as true (valid) and set to false by the parsers if the data is required.
    boolean isValid = true;

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

    public Item (Row r) {
        // Iterates through parsers and tries to fetch data from the given row.
        for (HashMap.Entry<Integer, Consumer<String>> e : parsers.entrySet()) {
            Integer key = e.getKey();
            Consumer<String> parser = e.getValue();

            // Parser is called with an empty string if no data is found for that index.
            if (r.hasCell(key)) {
                String raw = r.getCell(key).getRawValue();

                parser.accept(raw != null ? raw : "");
            } else {
                parser.accept("");
            }
        }
    }

    public String getGrapes () {
        String g = "";

        for (String s : grapes)
            g += "'" + s + "' ";

        return g;
    }

    public String getKeywords () {
        String w = "";

        for (String s : keywords)
            w += "'" + s + "' ";

        return w;
    }

    public String getName () {
        return name;
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

        String trimmed = str.trim().toLowerCase();

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
