import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author QinHaoChun
 * @version 2020/6/18
 */
public class MainTest {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
//        List<Database> databases = PDMUtil.parseDatabase("D:\\work\\3.研发\\微服务");
//        System.out.println(databases.size());

        System.out.println(MainTest.class);
        MainTest m = new MainTest();
        Class mainClass = m.getClass();
        System.out.println(mainClass);
        Class[] classes = new Class[]{MainTest.class};
        System.out.println(MainTest.class == mainClass);
        System.out.println(classes[0]);
    }
}
