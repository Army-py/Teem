package fr.army.stelyteam.utils.builder;

public enum Colors {

    BLACK("§0"),
    DARK_BLUE("§1"),
    DARK_GREEN("§2"),
    DARK_AQUA("§3"),
    DARK_RED("§4"),
    DARK_PURPLE("§5"),
    GOLD("§6"),
    GRAY("§7"),
    DARK_GRAY("§8"),
    BLUE("§9"),
    GREEN("§a"),
    AQUA("§b"),
    RED("§c"),
    LIGHT_PURPLE("§d"),
    YELLOW("§e"),
    WHITE("§f"),
    MAGIC("§k"),
    BOLD("§l"),
    STRIKETHROUGH("§m"),
    UNDERLINE("§n"),
    ITALIC("§o"),
    C("§r");

    private final String color;

    Colors(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
