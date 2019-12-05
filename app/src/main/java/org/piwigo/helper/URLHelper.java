package org.piwigo.helper;

public class URLHelper
{

    public static URLHelper INSTANCE;

    public URLHelper()
    {
        INSTANCE = this;
    }

    public String getUrlWithMethod(String url, String method)
    {
        url = url
                .replaceAll("(?i)https://", "")
                .replaceAll("(?i)http://", "");
        return (method + "://" + getPiwigoBaseFor(url));
    }

    /**
     * Remove trailing URL parts specify piwigo-internal pages
     *
     * @param url the url to stip
     * @return the base address of a piwigo URL
     */
    private String getPiwigoBaseFor(String url) {
        String newUrl;

        newUrl = url.replaceAll("(^.*)((?:about|admin|comments|feed|index|notification|picture|profile|ws).php(?:[?]\\/)?(?:.*))$", "$1");

        return newUrl;
    }
}
