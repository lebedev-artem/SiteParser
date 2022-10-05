import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

public class URLNameFormatter {

    String regexProtocol = "^(http|https)://(www.)?";

    public Integer getLevel(String url) {
        return StringUtils.countMatches(url, "/");
    }

    public String cleanURLName(String s) {
//        Удаляем протокол слэши и двоеточие
        s = s.replaceAll(regexProtocol, "");
//       Удаляем последний слэш
        if (s.lastIndexOf("/") == s.length() - 1) {
            s = s.substring(s.indexOf(s), s.length() - 1);
        }
        return s;
    }

    //    делает ссылку вида host1.hostname.com из http://www.host1.hostname.com/valera/nastalo/tvoe/vremya/
    public String setZeroLevelURL(String r) {
        r = r.replaceAll(regexProtocol, "");
        if (r.lastIndexOf("/") == r.length() - 1) {
            r = r.substring(r.indexOf(r), r.length() - 1);
        }
        if (r.contains("/")) {
            r = r.substring(0, r.indexOf("/"));
        }
        return r;
    }

    public String extractLink(Element element) {
        if (element != null) {
            return element.absUrl("href");
        }
        return "http://google.com";
//        Во время тестирования приложения, случались эксепшены с нулевой строкой, с которой я хочу сделать или
//        substring или equal. Пусть ллучше это будет такая строка, чем эксепнш, логику работы она не портит
    }
}
