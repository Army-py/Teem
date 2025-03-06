package fr.army.stelyteam.utils.builder;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.army.stelyteam.StelyTeamPlugin;

public class ColorsBuilder {

    private YamlConfiguration config;


    public ColorsBuilder(StelyTeamPlugin plugin) {
        this.config = plugin.getConfig();
    }

    public static String replaceColor(final String input) {
        final StringBuffer legacyBuilder = new StringBuffer();
        final Pattern allPattern = Pattern.compile("(&)?&([0-9a-fk-orA-FK-OR])");
        final Matcher legacyMatcher = allPattern.matcher(input);
        legacyLoop:
        while (legacyMatcher.find()) {
            final boolean isEscaped = legacyMatcher.group(1) != null;
            if (!isEscaped) {
                final char code = legacyMatcher.group(2).toLowerCase(Locale.ROOT).charAt(0);
                for (final ChatColor color : ChatColor.values()) {
                    if (color.getChar() == code) {
                        legacyMatcher.appendReplacement(legacyBuilder, ChatColor.COLOR_CHAR + "$2");
                        continue legacyLoop;
                    }
                }
            }
            legacyMatcher.appendReplacement(legacyBuilder, "&$2");
        }
        legacyMatcher.appendTail(legacyBuilder);

        final StringBuffer rgbBuilder = new StringBuffer();
        Pattern hexPattern = Pattern.compile("&#[A-Fa-f0-9]{6}");
        final Matcher rgbMatcher = hexPattern.matcher(legacyBuilder.toString());
        while (rgbMatcher.find()) {
            final String hexCode = rgbMatcher.group().replace("&#", "");
            rgbMatcher.appendReplacement(rgbBuilder, parseHexColor(hexCode));
        }
        rgbMatcher.appendTail(rgbBuilder);

        return rgbBuilder.toString();
    }

    public static String convertColors(String input) {
        String output = input;

        // Replace hex colors: convert '&#FFFFFF' to '<#FFFFFF>'
        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(output);
        output = matcher.replaceAll("<#$1>");

        // Replace the legacy color codes from the Colors enum (only process the first 16 actual colors)
        for (Colors color : Colors.values()) {
            output = output.replace(color.getColor(), "<" + color.name().toLowerCase() + ">");
        }

        return output;
    }

    public boolean prefixTeamIsTooLong(String prefixTeam){
        int colors = getPrefixColors(prefixTeam).size();
        int hexColors = getPrefixHexColors(prefixTeam).size();

        return prefixTeam.codePointCount(0, prefixTeam.length()) - (colors * 2 + hexColors * 8) > config.getInt("teamPrefixMaxLength");
    }

    public boolean prefixTeamIsTooShort(String prefixTeam){
        int colors = getPrefixColors(prefixTeam).size();
        int hexColors = getPrefixHexColors(prefixTeam).size();

        return prefixTeam.codePointCount(0, prefixTeam.length()) - (colors * 2 + hexColors * 8) <= config.getInt("teamPrefixMinLength");
    }


    public boolean descriptionTeamIsTooLong(String prefixTeam){
        int colors = getPrefixColors(prefixTeam).size();
        int hexColors = getPrefixHexColors(prefixTeam).size();

        return prefixTeam.codePointCount(0, prefixTeam.length()) - (colors * 2 + hexColors * 8) > config.getInt("teamDescriptionMaxLength");
    }


    public boolean containsBlockedColors(String prefixTeam){
        for (String color : config.getStringList("blockedColors")) {
            if (prefixTeam.contains(color)) {
                return true;
            }
        }

        for (String str : config.getConfigurationSection("blockedHexColors").getKeys(false)) {
            int hMin = config.getInt("blockedHexColors."+str+".h.min");
            int hMax = config.getInt("blockedHexColors."+str+".h.max");
            int sMin = config.getInt("blockedHexColors."+str+".s");
            int vMin = config.getInt("blockedHexColors."+str+".v");

            for (String hexColor : getPrefixHexColors(prefixTeam)){
                hexColor = hexColor.substring(2);
                ArrayList<Integer> rgbColor = getRGBFromHex(hexColor);
                ArrayList<Double> hslColor = getHSVFromRGB(rgbColor.get(0), rgbColor.get(1), rgbColor.get(2));
                if (hslColor.get(0) <= hMin || hslColor.get(0) >= hMax) {
                    if (hslColor.get(1) < sMin || hslColor.get(2) < vMin) {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }


    private ArrayList<String> getPrefixHexColors(String prefixTeam){
        Pattern hexPattern = Pattern.compile("&#[A-Fa-f0-9]{6}");
        Matcher hexMatcher = hexPattern.matcher(prefixTeam);
        ArrayList<String> hexColors = new ArrayList<>();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group();
            hexColors.add(hex);
        }
        return hexColors;
    }


    public ArrayList<String> getPrefixColors(String prefixTeam){
        Pattern pattern = Pattern.compile("[&§][^#]");
        Matcher matcher = pattern.matcher(prefixTeam);
        ArrayList<String> colors = new ArrayList<>();
        while (matcher.find()) {
            String hex = matcher.group();
            colors.add(hex);
        }
        return colors;
    }


    private ArrayList<Integer> getRGBFromHex(String hex){
        ArrayList<Integer> rgb = new ArrayList<>();
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        rgb.add(r);
        rgb.add(g);
        rgb.add(b);
        return rgb;
    }


    private ArrayList<Double> getHSVFromRGB(double r, double g, double b){
        ArrayList<Double> hsv = new ArrayList<>();
        // R, G, B values are divided by 255
        // to change the range from 0..255 to 0..1
        r = r / 255.0;
        g = g / 255.0;
        b = b / 255.0;

        // h, s, v = hue, saturation, value
        double cmax = Math.max(r, Math.max(g, b)); // maximum of r, g, b
        double cmin = Math.min(r, Math.min(g, b)); // minimum of r, g, b
        double diff = cmax - cmin; // diff of cmax and cmin.
        double h = -1, s = -1;

        // if cmax and cmax are equal then h = 0
        if (cmax == cmin)
            h = 0;

        // if cmax equal r then compute h
        else if (cmax == r)
            h = (60 * ((g - b) / diff) + 360) % 360;

        // if cmax equal g then compute h
        else if (cmax == g)
            h = (60 * ((b - r) / diff) + 120) % 360;

        // if cmax equal b then compute h
        else if (cmax == b)
            h = (60 * ((r - g) / diff) + 240) % 360;

        // if cmax equal zero
        if (cmax == 0)
            s = 0;
        else
            s = (diff / cmax) * 100;

        // compute v
        double v = cmax * 100;
        hsv.add(h);
        hsv.add(s);
        hsv.add(v);
        return hsv;
    }


    private static String parseHexColor(String hexColor) {
        Color.fromRGB(Integer.decode("#" + hexColor));
        final StringBuilder assembledColorCode = new StringBuilder();
        assembledColorCode.append(ChatColor.COLOR_CHAR + "x");
        for (final char curChar : hexColor.toCharArray()) {
            assembledColorCode.append(ChatColor.COLOR_CHAR).append(curChar);
        }
        return assembledColorCode.toString();
    }
}
