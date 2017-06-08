import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.*;

public class system_softeware_c2asm {
	public static FileWriter fw;
	public static BufferedWriter bufferOut;
	public static void main(String args[]) {
		try{
			Register AX = new Register("AX");
			Register BX = new Register("BX");
			Register CX = new Register("CX");
			String strLine;
//			args = new String[2];
//			args[0] = "cprog.c";
//			args[1] = "cprog.asm";
			fw = new FileWriter(new File(args[1]));
			FileReader fr = new FileReader(args[0]);
			
			BufferedReader bufferIn = new BufferedReader(fr);
			bufferOut = new BufferedWriter(fw);
			bufferOut.write("\tSTART\t");
			System.out.println("\tSTART\t");
			bufferOut.newLine();
			while((strLine=bufferIn.readLine())!=null){
				FILTER(strLine);
			}
			bufferOut.write("\tEND\t");
			System.out.println("\tEND\t");
			bufferOut.flush();
			fr.close();
			fw.close();
			System.out.println("Program complete.\nasm file path:" + System.getProperty("user.dir")+"/"+args[1]);
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	public static void FILTER(String str) throws IOException { // Assign works to different modules
		// Extract the first string(character only)
		Pattern classify = Pattern.compile("(^[\\w]+)");  
		Matcher a = classify.matcher(str);
		
		while (a.find()) {
			// Assign to DECLARE module
			if(a.group(1).equals("INT")) { 
				DECLARE(str.trim().substring(3, str.length()));
			} 
			// Assign to PRINT module
			else if(a.group(1).equals("PRINT")) { 
				WRITE(str.trim().substring(5, str.length()));
			}
			// Assign to CALCULATE module
			else { 
				CALCULATE(str);
			}
		}
		
	}
	
	private static void DECLARE(String str) throws IOException {
		Pattern p = Pattern.compile("([^ ,;]+)");
		Matcher m = p.matcher(str);

		while (m.find()) {
			if(m.group(1).contains("=")) {
				String var_name = m.group(1).split("=")[0];
				String value = m.group(1).split("=")[1];
				bufferOut.write(var_name+"\tWORD\t"+value);
				System.out.println(var_name+"\tWORD\t"+value);
			} else {
				bufferOut.write(m.group(1)+"\tRESW\t1");
				System.out.println(m.group(1)+"\tRESW\t1");
			}
			bufferOut.newLine();
		}
	}
	
	private static void WRITE(String str) throws IOException {
		Pattern p = Pattern.compile("([^ ();]+)");
		Matcher m = p.matcher(str);
		if (m.find()) {
			bufferOut.write("\tWRT\t"+m.group());
			System.out.println("\tWRT\t"+m.group());
			bufferOut.newLine();
		}
	}
	
	private static void CALCULATE(String str) throws IOException {
		str = str.replaceAll("\\s+","");
		str = str.replaceAll(";+","");
		String desc = str.split("=")[0];
		Register res = Arithmetic.eval(Arithmetic.postfix(str.split("=")[1]));
		Arithmetic.move(res, desc); 
		Register.releaseAllReg();
	}
	
}

class Arithmetic {
	private static List<String> operators = Arrays.asList("(",")","+","-","*","/");

	private static String translateSymbol(String operator){
		String symbol="";
		switch(operator){
			case "+":
				symbol="ADD";
				break;
			case "-":
				symbol="SUB";
				break;
			case "*":
				symbol="MUL";
				break;
			case "/":
				symbol="DIV";
				break;
		}
		return symbol; 
	}
	
	public static Register operation(String operator,Register a,String b) throws IOException {
		Register temp = Register.getAvailableReg();
		move(b, temp);
		system_softeware_c2asm.bufferOut.write("\t"+translateSymbol(operator)+"\t" + a.getRegName()+" , "+ temp.getRegName());
		System.out.println("\t"+translateSymbol(operator)+"\t" + a.getRegName()+" , "+ temp.getRegName());
		system_softeware_c2asm.bufferOut.newLine();
		Register.releaseReg(a);
		return temp;
	}
	
	public static Register operation(String operator,String a,Register b) throws IOException {
		Register temp = Register.getAvailableReg();
		move(a, temp);
		system_softeware_c2asm.bufferOut.write("\t"+translateSymbol(operator)+"\t" + temp.getRegName()+" , "+ b.getRegName());
		System.out.println("\t"+translateSymbol(operator)+"\t" + temp.getRegName()+" , "+ b.getRegName());
		system_softeware_c2asm.bufferOut.newLine();
		Register.releaseReg(temp);
		return b;
	}
	
	public static Register operation(String operator,Register a,Register b) throws IOException {
		system_softeware_c2asm.bufferOut.write("\t"+translateSymbol(operator)+"\t" + a.getRegName()+" , "+ b.getRegName());
		System.out.println("\t"+translateSymbol(operator)+"\t" + a.getRegName()+" , "+ b.getRegName());
		system_softeware_c2asm.bufferOut.newLine();
		return b;
	}
	
	public static Register operation(String operator,String a, String b) throws IOException {
		Register tmp1 = Register.getAvailableReg();
		Register tmp2 = Register.getAvailableReg();
		move(a, tmp1);
		move(b, tmp2);
		system_softeware_c2asm.bufferOut.write("\t"+translateSymbol(operator)+"\t" + tmp1.getRegName() +" , "+ tmp2.getRegName());
		System.out.println("\t"+translateSymbol(operator)+"\t" + tmp1.getRegName() +" , "+ tmp2.getRegName());
		system_softeware_c2asm.bufferOut.newLine();
		Register.releaseReg(tmp1);
		return tmp2;
	}
	
	public static void move(String a,Register b) throws IOException {
		system_softeware_c2asm.bufferOut.write("\tMOV\t" + a +" , "+ b.getRegName());
		System.out.println("\tMOV\t" + a +" , "+ b.getRegName());
		system_softeware_c2asm.bufferOut.newLine();
	}
	
	public static void move(Register a,String b) throws IOException {
		system_softeware_c2asm.bufferOut.write("\tMOV\t" + a.getRegName() +" , "+ b);
		System.out.println("\tMOV\t" + a.getRegName() +" , "+ b);
		system_softeware_c2asm.bufferOut.newLine();
	}
	
	public static void move(Register a,Register b) throws IOException {
		system_softeware_c2asm.bufferOut.write("\tMOV\t" + a.getRegName()+" , "+ b.getRegName());
		System.out.println("\tMOV\t" + a.getRegName()+" , "+ b.getRegName());
		system_softeware_c2asm.bufferOut.newLine();
	}
	
	
	
	private static int comparePriority(String A,String B) {

		/*	return 1, if A>B
		 *		  -1, if A<B
		 *		   0, if A=B	
		 */ 
		
		if(getPriority(A)>getPriority(B)){
			return 1;
		}else if(getPriority(A)<getPriority(B)){
			return -1;
		} else {
			return 0;
		}
	}
	private static int getPriority(String str){
		/*
		 * a_class > b_class > c_class
		 * 
		 * the number(3,2,1,0) means nothing but the precedence
		 * 
		 */
		List<String> a_class = Arrays.asList("(");
		List<String> b_class = Arrays.asList("*","/");
		List<String> c_class = Arrays.asList("+","-");
		if(a_class.contains(str)){
			return 3;
		}else if(b_class.contains(str)){
			return 2;
		}else if(c_class.contains(str)){
			return 1;
		}
		return 0;
	}
	
	public static Register eval(String str[]) throws IOException{
		/*
		 * An infix algorithm
		 * 
		 * Return the register containing final result 
		 * 
		 * (1) Sequentially classify the type of element in input array(str[])
		 * 		if element is an operand  - push it into stack 
		 *      else(element is an operator) - calculate the operator and operand in stack all together 
		 * (2) Pass the correct parameters to the operation(ADD,SUB,MUL,DIV) 
		 * 
		 * (3) Get the calculate result, push it into stack
		 * 
		 */
		Stack<Object> num_stack = new Stack<Object>();
		for(int i=0;i<str.length;i++) {
			String NextToken = str[i];
			if(!operators.contains(NextToken)) {
				num_stack.push(NextToken);
				continue;
			}
			
			Object post = num_stack.pop() ;
			Object pre = num_stack.pop() ;
			Register Reg_pre,Reg_post;
			String Str_pre,Str_post;
			
			if (post.getClass().equals(Register.class)) {
		        Reg_post = ((Register)post);
		        if (pre.getClass().equals(Register.class)) {
			        Reg_pre = ((Register)pre);
			        num_stack.push(operation(NextToken,Reg_pre,Reg_post));
			    }
			    else if (pre.getClass().equals(String.class)) {
			        Str_pre = ((String)pre);
			        num_stack.push(operation(NextToken,Str_pre,Reg_post));
			    }
		    }
		    else if (post.getClass().equals(String.class)) {
		        Str_post = ((String)post);
		        if (pre.getClass().equals(Register.class)) {
			        Reg_pre = ((Register)pre);
			        num_stack.push(operation(NextToken,Reg_pre,Str_post));
			    }
			    else if (pre.getClass().equals(String.class)) {
			        Str_pre = ((String)pre);
			        num_stack.push(operation(NextToken,Str_pre,Str_post));
			    }
		    }	
		}
		return (Register) num_stack.pop();
	}
	
	public static String[] postfix(String str) {
		/*
		 * An postfix algorithm
		 * 
		 * Return an array(for convinence) to eval function 
		 * 
		 * (1) POSTFIX 
		 * 
		 * (2) Using stack to get the ouput array
		 * 
		 */
		Stack<String> op_stack = new Stack<String>();
		Stack<String> res = new Stack<String>();
		String post_string[];
		for(int i=0;i<str.length();i++) {
			String NextToken = str.substring(i,i+1);
			if(!operators.contains(NextToken)){
				int rear = i+1;
				while(rear<str.length() && !operators.contains(str.substring(rear,rear+1))) rear++;
				res.push(str.substring(i,rear));
				i = rear-1;
			}
			else {
				if(op_stack.isEmpty()) {
					op_stack.push(NextToken);
				}
				else if( NextToken.equals(")") ){
					String tmp;
					do {
						tmp = op_stack.pop();
						if( tmp.equals("(") ) break;
						res.push(tmp);
					} while( !tmp.equals("(") );
				}
				else{
					while( comparePriority(NextToken,op_stack.peek())!= 1 ){
						if( op_stack.peek().equals("(") ) break;
						String pop = op_stack.pop();
						res.push(pop);
						if(op_stack.isEmpty() ) break;
					}
					op_stack.push(NextToken);
				}
			}
		}
		while(!op_stack.isEmpty()) {
			res.push(op_stack.pop());
		}
		post_string = new String[res.size()];
		
		for(int j=res.size()-1;j>=0;j--){
			post_string[j] = res.pop();
		}
		
		return post_string;
	}
}

class Register{
	// Only available registers
	private static List<Register> available_list = new ArrayList<Register>();
	// All existing registers
	private static List<Register> exist_list = new ArrayList<Register>();
	private String regName;

	public Register(String regName){
		Register.available_list.add(this);
		Register.exist_list.add(this);
		this.regName = regName;
	}

	public String getRegName(){
		return this.regName;
	}
	
	public static void releaseReg(Register a){
		Register.available_list.add(a);
	}
	
	public static void releaseAllReg(){
		for(int i=0;i<exist_list.size();i++){
			if(available_list.contains(exist_list.get(i))) continue;
			available_list.add(exist_list.get(i));
		}
	}
	
	public static Register getAvailableReg(){
		if(!Register.available_list.isEmpty()) {
			Register tmp = Register.available_list.get(0);
			Register.available_list.remove(0);
			return tmp;		
		}
		// Output error when needing more than 3 registers to store value 
		System.out.println("[Error]Register StackOverFlow!");
		System.exit(1);
		return null;
	}
}

