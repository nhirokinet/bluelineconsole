package net.nhiroki.bluelineconsole.commands.urls;

public class WebSearchEngine {
    public WebSearchEngine(String display_name, String url_base) {
        this.display_name = display_name;
        this.url_base = url_base;
    }

    public final String display_name;
    public final String url_base;
}
