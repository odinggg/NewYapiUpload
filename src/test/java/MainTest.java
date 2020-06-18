import com.github.odinggg.newyapiupload.dto.Database;
import com.github.odinggg.newyapiupload.interaction.UploadToYapi;
import com.github.odinggg.newyapiupload.util.PDMUtil;

import java.util.List;

/**
 * @author QinHaoChun
 * @version 2020/6/18
 */
public class MainTest {
    public static void main(String[] args) {
        List<Database> databases = PDMUtil.parseDatabase("D:\\work\\3.研发\\微服务");
        UploadToYapi.DATABASES.addAll(databases);
        String desc = UploadToYapi.getDesc("orders", "order_info", "status");
        System.out.println(desc);
    }
}
