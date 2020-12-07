package endless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * <pre>
 * ifast 入口
 * </pre>
 * <small> 2018年3月22日 | Aron</small>
 */
@ServletComponentScan
@SpringBootApplication
public class Application {
	
	private static Logger log = LoggerFactory.getLogger(Application.class);
	
	/**
	 * <pre>
	 * </pre>
	 * <small> 2018年3月22日 | Aron</small>
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args); 
	}
	 
}