package cn.edu.njnu.demonstration;

import cn.edu.njnu.demonstration.scanner.WordScanner;

import java.io.*;


/*
 * BugFixed: 补上了忘记调用的最后一次确定化，解决了有多个开始节点的问题
 *
 * cn.edu.njnu.demonstration 生成器的示例
 * | .element 自定义容器，用于存放生成的单词类型
 * | .generator
 *      | ClassGenerator 自动生成器使用示例，将Lex.txt生成为States.java和WordScanner.java
 *      | Lex.txt 描述自动生成语法的文件，匹配后的代码可以通过字符串类型的变量_content获取匹配得到的内容
 * | .scanner 将自动生成的两个.java文件放在这里，暂时没有实现自动生成对应路径并存放，需要手动放进去
 *      | States 生成的enum，用于描述MFA，每个状态提供两个方法：
 *               一个是next，转移下一状态，一个是execute，如果该状态是最终态，将执行Lex中提供的代码
 *      | WordScanner 生成的驱动状态机的类，可以换成自己的，但还是提供了一系列的操作方法，
 *                    最主要是为了保存开始和终止转态，如果需要自定义方法可以扩展类或者覆写方法
 * Application 演示通过调用自动生成的词法分析程序对Test.txt中的C语言程序进行词法分析
 * Test.txt 被分析的C语言程序，去掉了其中的各种宏定义
 *
 * cn.edu.njnu.minic 词法分析程序的自动生成器
 * | .annotation
 *      | GeneratorConfigure 用于自动生成器配置的注解，写在调用生成器的类上，提供lex文件的路径和一些补充内容，
 *                           如在自动生成的程序中需要调用的自定义类
 * | .exception
 * | .fa 自动机相关
 *      | DFA 提供了确定有限状态机的实现，提供了将多个DFA合并并将终止节点映射的方法、NFA转为DFA和DFA化简的方法
 *      | Edge FA的边
 *      | FA 有限自动机的父类，通过邻接表表示节点关系
 *      | NFA 提供了不确定有限状态机的实现，提供了将正则转化为NFA的方法
 * | .lex Lex文件相关
 *      | Generator 自动代码生成器，将根据GeneratorConfigure注解中的路径读入Lex文件，调用Lex词法分析类进行分析
 *      | LexElement 用于表示Lex中的单词
 *      | LexElementEnum 表示Lex中单词的类型
 *      | LexScanner 对Lex文件进行词法分析，返回LexElement作为分词结果
 *      | State 自动生成的Enum类中States.java下的各个状态都是实现该接口
 * | .re 正则表达式相关
 *      | RegexExpression 会将输入的字符串预处理，把所有的[]\w\d a-z一类的替换为a|b|...，只支持比较简单的语法
 * | .templates 生成器模板，生成的文件均通过模板渲染出来
 * | .utils 数组和模板渲染的工具类
 *
 * 还没有经过仔细的测试，只使用了去掉了宏定义的
 * 已知存在的一些问题：
 * 因为对于Lex的词法分析使用了自带的正则表达式进行处理，使用了非贪心的匹配来提取%%[...%%]中的代码段，
 * 所以如果代码段过长很可能会导致爆栈一类的问题，可以使用自定义的类将代码段进行封装，然后在配置中导入类即可，
 * 如在示例中对保留字识别的代码。暂时没有想到更好的操作方式
 *
 * 同时，由于一个终止节点很可能是多个匹配的终点，虽然生成时对返回语句进行了替换处理，不太会出现编译阶段的错误，
 * 但是如果不进行自定义的判断，将返回在Lex文件中按照顺序的第一个可用的返回值，可能与用户期望逻辑不同。
 * 如果对于各个结果在返回前进行验证，效率可能不太高，也需要自行编写相关返回代码，暂时没有想到更合适的办法
 *
 * 其中在NFA转DFA过程中需要在插入前保证数组的唯一性，没有找到非常合适的算法来进行hash计算，自己实现的
 * 冲突率应该比较高，没有更好的思路
 *
 */
public class Application {
	public static void main(String[] args) {
		try {
			// Init scanner
			WordScanner ws = new WordScanner();
			// Open file and push back stream for mismatched contents
			FileInputStream fis = new FileInputStream(new File(".\\src\\main\\java\\cn\\edu\\njnu\\demonstration\\Test.txt"));
			PushbackInputStream pis = new PushbackInputStream(fis, 16);

			int buffer = -1;

			while ((buffer = pis.read()) != -1) {
				String s = ws.next((char) buffer);
				if (s != null) {
					if (s.length() > 0)
						pis.unread(s.getBytes());

					System.out.println(ws.execute());
					ws.init();
				}
			}

			if (ws.hasMatched()) {
				System.out.println(ws.execute());
			}

			// Clear the unmatched buffer
			String unmatched = ws.getUnMatched();
			if (unmatched != null && unmatched.length() > 0) {
				ws.init();
				for (int i = 0; i < unmatched.length(); i++) {
					String s = ws.next(unmatched.charAt(i));
					if (s != null) {
						i -= s.length();
						System.out.println(ws.execute());
						ws.init();
					}
				}
			}
			pis.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
