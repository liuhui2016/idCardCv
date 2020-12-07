package endless.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 地图首页
 * @author endless㊚
 * @date 2020年4月29日16:20:35
 */
@RequestMapping("/idCard")
@Controller
public class IdCardController {
	 
   
    @GetMapping("/index") 
    String index(Model model) {
        return "idcard";
    }

    @GetMapping("/index1") 
    String threads(Model model) {
        return "threads";
    }
}