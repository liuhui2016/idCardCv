package endless.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ConfUtil {
	
	public static String stepLocal; 
	public static boolean show;
	public static String upLoadTemp;
	
	@Value("${idcard.step.show}")
	public void setShow(String toShow) {
		show = Boolean.valueOf(toShow);
	} 
	
	@Value("${idcard.step.dir}")
	public void setStepLocal(String dir) {
		stepLocal = dir;
	}
	
	@Value("${idcard.temp.dir}")
	public void setUpLoadTemp(String dir) {
		upLoadTemp = dir;
	}
	
}
