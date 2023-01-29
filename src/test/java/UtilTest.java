import cn.edu.njnu.minic.utils.ArrayUtils;
import org.junit.Test;
import sun.security.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Random;

public class UtilTest {
	@Test
	public void testArrayUtilHash() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		ArrayList<Integer> hash = new ArrayList<Integer>();


		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				list.add(i + j);
			}
			hash.add(ArrayUtils.getHashCode(list));
			list.clear();
		}
		for (int j = 9; j >= 0; j--)
			list.add(j);
		hash.add(ArrayUtils.getHashCode(list));

		System.out.println(hash.toString());
	}
}
