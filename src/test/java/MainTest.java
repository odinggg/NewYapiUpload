import com.github.odinggg.newyapiupload.dto.Database;
import com.github.odinggg.newyapiupload.util.PDMUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * @author QinHaoChun
 * @version 2020/6/18
 */
public class MainTest {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        List<Database> databases = PDMUtil.parseDatabase("D:\\work\\3.研发\\微服务");
        System.out.println(databases.size());
    }
}
