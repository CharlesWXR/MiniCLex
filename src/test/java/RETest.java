import cn.edu.njnu.minic.exception.REException;
import cn.edu.njnu.minic.re.RegexExpression;
import org.junit.Test;

public class RETest {
	@Test
	public void TestRE() {
		String s = "a[e-d]*([\\w])";
		try {
			RegexExpression r = new RegexExpression(s);
			System.out.println(r.getRe());
		} catch (REException e) {
			System.out.println(e.getMessage());
		}
	}
}
