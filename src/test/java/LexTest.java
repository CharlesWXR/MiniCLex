import cn.edu.njnu.minic.annotation.GeneratorConfigure;
import cn.edu.njnu.minic.lex.Generator;
import cn.edu.njnu.minic.lex.LexScanner;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

public class LexTest {
	@Test
	public void testLexScanner() {
		try {
			FileInputStream fis = new FileInputStream(new File("F:\\IDEA Workplace\\MiniCLex\\src\\test\\java\\a.txt"));
			System.out.println(LexScanner.getElements(fis));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	@GeneratorConfigure(lexFilePath = "F:\\IDEA Workplace\\MiniCLex\\src\\test\\java\\a.txt",
	supportClasses = {"cn.edu.njnu.minic.utils.ArrayUtils"})
	public void testGenerateInit() {
		try {
			Generator generator = new Generator();
			generator.generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
