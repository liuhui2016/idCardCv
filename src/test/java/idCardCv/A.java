package idCardCv;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class A {
	private String name;
	public static void main(String[] args) {
		String xxname = "xx4.jpg";
		System.out.println(xxname.split("\\.")[1]);
	}
}
