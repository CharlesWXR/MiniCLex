import cn.edu.njnu.minic.exception.REException;
import cn.edu.njnu.minic.fa.NFA;
import cn.edu.njnu.minic.re.RegexExpression;
import org.junit.Test;

public class NFATest {
	@Test
	public void testRETransformer() {
		String s = "([\\d])*";
		try {
			RegexExpression re = new RegexExpression(s);
			NFA n = new NFA(re, 1);
			System.out.println(n.toString());
		} catch (REException e) {
			e.getMessage();
		}
	}
}
