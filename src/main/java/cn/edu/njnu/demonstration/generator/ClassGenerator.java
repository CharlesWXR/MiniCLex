package cn.edu.njnu.demonstration.generator;

import cn.edu.njnu.minic.annotation.GeneratorConfigure;
import cn.edu.njnu.minic.lex.Generator;

@GeneratorConfigure(
		lexFilePath = ".\\src\\main\\java\\cn\\edu\\njnu\\demonstration\\generator\\Lex.txt",
		supportClasses = {
				"cn.edu.njnu.demonstration.element.OperatorEnum",
				"cn.edu.njnu.demonstration.element.ReservedEnum",
				"cn.edu.njnu.demonstration.element.TypeEnum",
				"cn.edu.njnu.demonstration.element.Word",
				"cn.edu.njnu.demonstration.element.IdentifierClassifier"
		})
public class ClassGenerator {
	public static void main(String[] args) {
		Generator generator = new Generator();
		try {
			generator.generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
