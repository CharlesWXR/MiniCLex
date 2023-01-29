package cn.edu.njnu.minic.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GeneratorConfigure {
	// The target lex filepath
	String lexFilePath();

	// The supporting classes required in the generated class, such as custom containers
	String[] supportClasses();
}
