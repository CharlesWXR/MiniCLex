import cn.edu.njnu.minic.exception.REException;
import cn.edu.njnu.minic.fa.DFA;
import cn.edu.njnu.minic.fa.NFA;
import cn.edu.njnu.minic.re.RegexExpression;
import org.junit.Test;

import java.util.ArrayList;

public class DFATest {
	@Test
	public void testDFA() {
		try {
			String s = "([\\w])*";
//			String s = "(0|1(01*0)*1)*";
			RegexExpression re = new RegexExpression(s);
			NFA n = new NFA(re, 1);
			System.out.println(n.toString());
			DFA d = new DFA(n);
			System.out.println(d.toString());
		} catch (REException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testMerge() {
		try {
			String s1 = "([\\w])*";
			String s2 = "([\\d])*";
			RegexExpression re1 = new RegexExpression(s1);
			RegexExpression re2 = new RegexExpression(s2);
			NFA n1 = new NFA();
			int index = n1.convertRE2NFA(re1, 1);
			System.out.println(n1.toString());
			NFA n2 = new NFA();
			index = n2.convertRE2NFA(re2, index);
			System.out.println(n2.toString());
			ArrayList<NFA> NFAs = new ArrayList<NFA>();
			NFAs.add(n1);
			NFAs.add(n2);
			NFA all = NFA.mergeAll(NFAs);
			System.out.println(all.toString());
			DFA d = new DFA(all);
			System.out.println(d);
		} catch (REException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDFAMerge() {
		try {
			String s1 = "+";
			String s2 = "++";
			RegexExpression re1 = new RegexExpression(s1);
			RegexExpression re2 = new RegexExpression(s2);
			NFA n1 = new NFA();
			int index = n1.convertRE2NFA(re1, 1);
			System.out.println(n1.toString());
			NFA n2 = new NFA();
			index = n2.convertRE2NFA(re2, index);
			System.out.println(n2.toString());
			ArrayList<DFA> DFAs = new ArrayList<DFA>();
			DFA d1 = new DFA(n1);
			System.out.println("f1");
			DFA d2 = new DFA(n2);
			System.out.println("f");
		} catch (REException e) {
			e.printStackTrace();
		}
	}
}
