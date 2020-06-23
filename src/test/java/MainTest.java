import com.github.odinggg.newyapiupload.dto.Database;
import com.github.odinggg.newyapiupload.util.PDMUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author QinHaoChun
 * @version 2020/6/18
 */
public class MainTest {
    public static void main(String[] args) {
//        List<Database> databases = PDMUtil.parseDatabase("D:\\work\\3.研发\\微服务");
//        PDMUtil.DATABASES.addAll(databases);
//        String desc = PDMUtil.getDesc( "order_info", "status");
//        String desc1 = PDMUtil.getDesc("orders", "order_info", "status");
//        System.out.println(desc);
//        System.out.println(desc1);
        System.out.println(PDMUtil.className2TableName("OrderInfoItem"));
    }
}
