package fr.army.stelyteam.utils.manager;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.army.stelyteam.StelyTeamPlugin;

public class MessageManager {

    private YamlConfiguration messages;

    public MessageManager(StelyTeamPlugin plugin) {
        this.messages = plugin.getMessages();
    }


    public String getMessageWithoutPrefix(String key) {
        return messages.getString(key);
    }

    public String getMessage(String path) {
        return getPluginPrefix() + messages.getString(path);
    }

    public String getReplaceMessage(String path, String replace) {
        return getPluginPrefix() + messages.getString(path).replace("%VALUE%", replace);
    }

    public String replaceAuthor(String path, String author) {
        return getPluginPrefix() + messages.getString(path).replace("%AUTHOR%", author);
    }

    public String replaceReceiver(String path, String receiver) {
        return getPluginPrefix() + messages.getString(path).replace("%RECEIVER%", receiver);
    }

    public String replaceTeamId(String path, String teamName) {
        return getPluginPrefix() + messages.getString(path).replace("%TEAM_ID%", teamName);
    }

    public String replaceTeamPrefix(String path, String teamPrefix) {
        return getPluginPrefix() + messages.getString(path).replace("%TEAM_PREFIX%", teamPrefix);
    }

    public String replaceAuthorAndReceiver(String path, String author, String receiver) {
        return getPluginPrefix() + messages.getString(path).replace("%AUTHOR%", author).replace("%RECEIVER%", receiver);
    }

    public String replaceAuthorAndTeamName(String path, String author, String teamName) {
        return getPluginPrefix() + messages.getString(path).replace("%AUTHOR%", author).replace("%TEAM%", teamName);
    }

    private String getPluginPrefix (){
        return messages.getString("prefix");
    }
}
