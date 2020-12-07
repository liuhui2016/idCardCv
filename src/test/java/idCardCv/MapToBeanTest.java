package idCardCv;

import java.util.Map;


import com.google.common.collect.Maps;

import net.sf.cglib.beans.BeanMap;

public class MapToBeanTest {
	public static void main(String[] args) {
		   Map<String,Object>map = Maps.newHashMap();
		   map.put("name", "1");
           A reInfo  = new A();
		   BeanMap beanMap = BeanMap.create(reInfo);
           beanMap.putAll(map);
           System.out.println(reInfo.getName());
	}
}
