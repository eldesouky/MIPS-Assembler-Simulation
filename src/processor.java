import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.sun.xml.internal.bind.v2.model.core.ID;

public class processor {

	String[] Data_Memory = new String[2048 * 4];
	Hashtable<String, String> bin_reg = new Hashtable<String, String>();
	Hashtable<String, String> reg_value = new Hashtable<String, String>();
	String sp_string;
	boolean isBegin;
	ArrayList<String> instructions_code = new ArrayList<String>();
	int pc1;
	int sp;
	String opcode;
	String opfunc;
	boolean is_begin;
	boolean enter;
	Hashtable<String, String> reg_bin = new Hashtable<String, String>();
	Hashtable<String, String> reg_data = new Hashtable<String, String>();
	Hashtable<String, Integer> methods = new Hashtable<String, Integer>();
	ArrayList<ArrayList<String>> pipelinedArray = new ArrayList<ArrayList<String>>();
	Hashtable<String, String> IF_ID = new Hashtable<String, String>();
	Hashtable<String, String> ID_EX = new Hashtable<String, String>();
	Hashtable<String, String> EX_MEM = new Hashtable<String, String>();
	Hashtable<String, String> MEM_WB = new Hashtable<String, String>();
	Hashtable<String, String> START = new Hashtable<String, String>();
	Hashtable<String, String> END = new Hashtable<String, String>();

	public processor() throws IOException {

	}

	// op[0]=opcode op[1]=functioc code
	public String[] opcode(String t) {
		String[] op = new String[5];
		String x = t.toLowerCase();
		if (x.equalsIgnoreCase("add")) {
			op[0] = "000000";
			op[1] = "100000";
		} else if (x.equalsIgnoreCase("addi")) {
			op[0] = "001000";
			op[1] = "";
		} else if (x.equalsIgnoreCase("sub")) {
			op[0] = "000000";
			op[1] = "100010";
		} else if (x.equalsIgnoreCase("lw")) {
			op[0] = "100011";
			op[1] = "";
		} else if (x.equalsIgnoreCase("sw")) {
			op[0] = "101011";
			op[1] = "";
		} else if (x.equalsIgnoreCase("sll")) {
			op[0] = "000000";
			op[1] = "000000";
		} else if (x.equalsIgnoreCase("srl")) {
			op[0] = "000000";
			op[1] = "000010";
		} else if (x.equalsIgnoreCase("and")) {
			op[0] = "000000";
			op[1] = "100100";
		} else if (x.equalsIgnoreCase("andi")) {
			op[0] = "001100";
			op[1] = "";
		} else if (x.equalsIgnoreCase("or")) {
			op[0] = "000000";
			op[1] = "100101";
		} else if (x.equalsIgnoreCase("ori")) {
			op[0] = "001101";
			op[1] = "";
		} else if (x.equalsIgnoreCase("nor")) {
			op[0] = "000000";
			op[1] = "100111";
		} else if (x.equalsIgnoreCase("beq")) {
			op[0] = "000100";
			op[1] = "";
		} else if (x.equalsIgnoreCase("bne")) {
			op[0] = "000101";
			op[1] = "";
		} else if (x.equalsIgnoreCase("j")) {
			op[0] = "000010";
			op[1] = "";
		} else if (x.equalsIgnoreCase("jal")) {
			op[0] = "000011";
			op[1] = "";
		} else if (x.equalsIgnoreCase("jr")) {
			op[0] = "000000";
			op[1] = "001000";
		} else if (x.equalsIgnoreCase("slt")) {
			op[0] = "000000";
			op[1] = "101010";
		} else if (x.equalsIgnoreCase("sltu")) {
			op[0] = "000000";
			op[1] = "101011";
		}
		return op;
	}

	Hashtable<String, String> fetch(Hashtable<String, String> parse) {
		Hashtable<String, String> IF_ID = new Hashtable<String, String>();
		int pcNormal = Integer.parseInt(reg_data.get("11101"), 2);
		// System.out.println(pcNormal);
		String pcNotNormal = parse.get("add_result");
		String bitMux = parse.get("pcsrc");
		if (bitMux == null) {
			bitMux = "0";
		}
		String currentIns = "";
		if (bitMux.equalsIgnoreCase("0")) {
			currentIns = this.instructions_code.get(pcNormal);
		} else {
			reg_data.put("11101", check_length(pcNotNormal));
			currentIns = this.instructions_code.get(Integer.parseInt(
					reg_data.get("11101"), 2));
			START.put("pcsrc", "0");

		}

		int x = Integer.parseInt(reg_data.get("11101"), 2) + 4;
		reg_data.put("11101", check_length(Integer.toBinaryString(x)));
		IF_ID.put("pc", reg_data.get("11101"));
		IF_ID.put("instruction", currentIns);
		return IF_ID;
	}

	public String reg(String reg) {

		return reg_bin.get(reg);
	}

	public String shamount(String x, String type) {
		if (type.equals("sll") || type.equals("srl")) {
			while (x.length() != 5)
				x = "0" + x;
			return x;
		} else {
			x = "";
			while (x.length() != 5)
				x = "0" + x;
			return x;

		}
	}

	public String[] offset(String x) {
		String[] result = new String[2];
		int i = 1;
		int off;
		String y = x.charAt(0) + "";
		String z = x.charAt(i) + "";
		while (!z.equals("(")) {
			y = y + x.charAt(i) + "";
			i++;
			z = x.charAt(i) + "";
		}
		off = Integer.parseInt(y);
		result[0] = immediate(Integer.toBinaryString(off));
		String yy = x.charAt(i + 1) + "";
		yy += x.charAt(i + 2) + "";
		yy += x.charAt(i + 3) + "";
		result[1] = reg_bin.get(yy);
		return result;
	}

	public String immediate(String x) {
		if (x.length() < 16) {
			while (x.length() != 16)
				x = "0" + x;
			return x;
		} else if (x.length() > 16) {
			x = x.substring(16, 32);
			return x;
		} else
			return x;
	}

	public String signExtend(String x) {
		if (x.length() < 32) {
			while (x.length() != 32)
				x = "0" + x;
			return x;
		} else {
			return x;
		}
	}

	private String pits(String x) {
		while (x.length() != 5)
			x = "0" + x;

		return x;
	}

	// |0 | 1| 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16
	// | 17 | 18 | 19 | 20 | 21 | 22 | 23 | 24 | 25 | 26 | 27 | 28 | 29 | 30 |
	// 31
	// |31| 30| 29| 28| 27| 26| 25| 24| 23 | 22| 21 | 20 | 19 | 18 | 17 | 16 |
	// 15 | 14 | 13 | 12 | 11 | 10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0
	public String sll(String x, int c) {
		while (c != 0) {
			x = x.substring(1, x.length());
			x = x + "0";
			c--;
		}
		return x;
	}

	public String jadd(String x, String pc) {
		if (x.length() < 22) {
			while (x.length() != 22)
				x = "0" + x;
		} else if (x.length() > 22) {
			x = x.substring(10, 32);
		}
		x = sll(x, 2);
		pc = pc.substring(0, 4);
		x = pc + x;

		return x;

	}

	public void jump(String csvfile) throws IOException {
		BufferedReader b = new BufferedReader(new FileReader(csvfile));

		String l = "";
		l = b.readLine();
		int counter = Integer.parseInt(l);
		l = b.readLine();
		int x = l.length() - 1;
		String z = l.charAt(x) + "";
		if (z.equals(":")) {
			String y = l.substring(0, x);
			methods.put(y, counter);
		}
		while ((l = b.readLine()) != null) {
			int xx = l.length() - 1;
			String zz = l.charAt(xx) + "";
			if (l.contains(":")) {

				String yy = l.substring(0, xx);
				methods.put(yy, counter + 4);
			} else
				counter += 4;
		}
	}

	public String getBinary(int num, int countBits) {
		String res = "";
		for (int i = 0; i < countBits; i++) {
			if (num % 2 == 1) {
				res = '1' + res;
			}
			if (num % 2 == 0) {
				res = '0' + res;
			}
			num = num / 2;
		}
		return res;
	}

	public ArrayList<String> intialize(String csvFile) throws IOException {

		BufferedReader br = null;
		String line = "";
		br = new BufferedReader(new FileReader(csvFile));
		jump(csvFile);
		try {
			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();
			pc1 = Integer.parseInt(line);
			sp = Integer.parseInt(line);
			set_inst(sp);
			reg_data.put("11101", check_length(Integer.toBinaryString(sp)));
			while ((line = br.readLine()) != null) {
				String[] split1 = line.split(" ");
				if (split1.length > 1) {
					String[] op = opcode(split1[0]);
					String xx = split1[1];
					String[] split2 = xx.split(",");
					String instruction;
					String x = split1[0].toLowerCase();
					if (x.equals("add") || x.equals("sub") || x.equals("and")
							|| x.equals("or") || x.equals("nor")
							|| x.equals("slt") || x.equals("sltu")) {
						String opcode = op[0];
						String function = op[1];
						String rd = reg_bin.get(split2[0]);
						String rs = reg_bin.get(split2[1]);
						String rt = reg_bin.get(split2[2]);
						String shamount = shamount(getBinary(0, 5), split1[0]);
						instruction = opcode + rs + rt + rd + shamount
								+ function;
						instructions_code.add(instruction);
						set_mem(pc1);
						pc1 = pc1 + 4;
					} else if (x.equals("sll") || x.equals("srl")) {
						String opcode = op[0];
						String function = op[1];
						String rd = reg_bin.get(split2[0]);
						String rs = reg_bin.get(split2[1]);
						String rt = reg_bin.get(split2[2]);
						int sha = Integer.parseInt(split2[2]);
						String shamount = shamount(getBinary(sha, 5), split1[0]);
						instruction = opcode + "00000" + rd + rs + shamount
								+ function;
						instructions_code.add(instruction);
						set_mem(pc1);
						pc1 = pc1 + 4;
					} else if (x.equals("addi") || x.equals("andi")
							|| x.equals("ori")) {
						String opcode = op[0];
						String rd = reg_bin.get(split2[0]);
						String rs = reg_bin.get(split2[1]);
						String immediate = immediate(Integer
								.toBinaryString(Integer.parseInt(split2[2])));

						instruction = opcode + rs + rd
								+ immediate.substring(0, 16);
						instructions_code.add(instruction);
						set_mem(pc1);
						pc1 = pc1 + 4;
					} else if (x.equals("bne") || x.equals("beq")) {
						String opcode = op[0];
						String rd = reg_bin.get(split2[0]);
						String rs = reg_bin.get(split2[1]);

						String target = check_length(
								Integer.toBinaryString(methods.get(split1[1]
										.split(",")[2]))).substring(16);
						instruction = opcode + rd + rs + target;
						instructions_code.add(instruction);
						set_mem(pc1);
						pc1 = pc1 + 4;
					} else if (x.equals("lw") || x.equals("sw")) {
						String opcode = op[0];
						String rd = reg_bin.get(split2[0]);
						String[] reg = offset(split2[1]);
						String rs = reg[1];
						String offset = reg[0];

						instruction = opcode + rs + rd + offset;
						instructions_code.add(instruction);
						set_mem(pc1);
						pc1 = pc1 + 4;
					}

					else if (x.equals("j") || x.equals("jal")) {
						// System.out.println(split1[1]);
						String opcode = op[0];

						String jaddress = check_length(
								Integer.toBinaryString(methods.get(split1[1]) / 4))
								.substring(6);

						instruction = opcode + jaddress;
						instructions_code.add(instruction);
						set_mem(pc1);
						pc1 = pc1 + 4;
					} else if (x.equals("jr")) {
						String opcode = op[0];
						String function = op[1];

						instruction = opcode + reg_bin.get(split1[1])
								+ "000000000000000" + function;
						instructions_code.add(instruction);
						set_mem(pc1);
						pc1 = pc1 + 4;
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("The instructions are converted into binary ");
		return instructions_code;
	}

	private void set_mem(int pc) {
		int pcc = pc;
		int i = pc + 1;
		while (i < pc + 4) {

			instructions_code.add("This is instrction " + pcc + " memory cell");
			i++;
		}
	}

	Hashtable<String, String> execute(Hashtable<String, String> decode) {
		Hashtable<String, String> result = new Hashtable<String, String>();
		String sign_extend = decode.get("signExtend");
		String add_operand_1 = decode.get("pc");
		String Read_data_1 = decode.get("read_data_1");
		String Read_data_2 = decode.get("read_data_2");
		String add_operand_2_shifted = sign_extend.substring(2) + "00";
		String add_result = check_length(Integer.toBinaryString(Integer
				.parseInt(add_operand_1, 2)
				+ Integer.parseInt(add_operand_2_shifted, 2)));
		result.put("add_result", add_result);
		result.put("regWrite", decode.get("regWrite"));
		String AlUOp = decode.get("aluOp");
		String alusrc = decode.get("aluSrc");
		String ALU_result = "";
		result.put("branch", decode.get("branch"));
		String Zero = "0";
		String ALUfunction = decode.get("signExtend").substring(26, 32);
		switch (AlUOp) {
		case "00":
			if (decode.get("opcode").equals("000011")) {
				result.put("branch", "1");
				Zero = "1";
				String y = check_length(Integer.toBinaryString(Integer
						.parseInt(add_operand_2_shifted, 2)));
				result.put("add_result", y);
				String ra = check_length(Integer.toBinaryString(Integer
						.parseInt(reg_data.get("11101"), 2)));
				reg_data.put("11111", ra);
			} else if (decode.get("opcode").equals("000010")) {
				result.put("branch", "1");
				Zero = "1";
				String y = check_length(Integer.toBinaryString(Integer
						.parseInt(add_operand_2_shifted, 2)));
				result.put("add_result", y);

			} else if (decode.get("opcode").equals("001100")) {
				ALU_result = check_length(Integer.toBinaryString(Integer
						.parseInt(Read_data_1, 2)
						& Integer.parseInt(sign_extend, 2)));
			} else if (decode.get("opcode").equals("000101")) {
				if (alusrc.equals("0")) {
					if (Integer.parseInt(Read_data_1, 2) != Integer.parseInt(
							Read_data_2, 2)) {
						result.put("branch", "1");
						Zero = "1";
						ALU_result = "00000000000000000000000000000001";

						add_result = check_length(Integer
								.toBinaryString(Integer
										.parseInt(sign_extend, 2)));
						result.put("add_result", add_result);
					}

				} else {
					if (Integer.parseInt(Read_data_1, 2) != Integer.parseInt(
							sign_extend, 2)) {
						result.put("branch", "1");
						Zero = "1";
						ALU_result = "00000000000000000000000000000001";
						add_result = check_length(Integer
								.toBinaryString(Integer
										.parseInt(sign_extend, 2)));
						result.put("add_result", add_result);
					}

				}
			} else if (decode.get("opcode").equals("001101")) {
				ALU_result = check_length(Integer.toBinaryString(Integer
						.parseInt(Read_data_1, 2)
						| Integer.parseInt(sign_extend, 2)));
			} else if (alusrc.equals("0")) {
				ALU_result = check_length(Integer.toBinaryString(Integer
						.parseInt(Read_data_1, 2)
						+ Integer.parseInt(Read_data_2, 2)));
			} else {
				ALU_result = check_length(Integer.toBinaryString(Integer
						.parseInt(Read_data_1, 2)
						+ Integer.parseInt(sign_extend, 2)));
			}
			break;
		case "01":

			if (alusrc.equals("0")) {
				ALU_result = check_length(Integer.toBinaryString(Integer
						.parseInt(Read_data_1, 2)
						- Integer.parseInt(Read_data_2, 2)));
			} else {
				ALU_result = check_length(Integer.toBinaryString(Integer
						.parseInt(Read_data_1, 2)
						- Integer.parseInt(sign_extend, 2)));
			}
			if (ALU_result.equals("00000000000000000000000000000000")) {
				Zero = "1";

				add_result = check_length(Integer.toBinaryString(Integer
						.parseInt(sign_extend, 2)));
				result.put("add_result", add_result);

			}
			break;
		case "10":
			switch (ALUfunction) {
			case "100000":
				if (alusrc.equals("0")) {
					ALU_result = check_length(Integer.toBinaryString(Integer
							.parseInt(Read_data_1, 2)
							+ Integer.parseInt(Read_data_2, 2)));

				} else {
					ALU_result = check_length(Integer.toBinaryString(Integer
							.parseInt(Read_data_1, 2)
							+ Integer.parseInt(sign_extend, 2)));
				}
				break;
			case "100010":
				if (alusrc.equals("0")) {
					ALU_result = check_length(Integer.toBinaryString(Integer
							.parseInt(Read_data_1, 2)
							- Integer.parseInt(Read_data_2, 2)));
				} else {
					ALU_result = check_length(Integer.toBinaryString(Integer
							.parseInt(Read_data_1, 2)
							- Integer.parseInt(sign_extend, 2)));
				}
				if (Integer.parseInt(ALU_result, 2) == 0)
					Zero = "1";
				break;
			case "100100":
				if (alusrc.equals("0")) {
					ALU_result = check_length(Integer.toBinaryString(Integer
							.parseInt(Read_data_1, 2)
							& Integer.parseInt(Read_data_2, 2)));
				} else {
					ALU_result = check_length(Integer.toBinaryString(Integer
							.parseInt(Read_data_1, 2)
							& Integer.parseInt(sign_extend, 2)));
				}
				break;
			case "100101":
				if (alusrc.equals("0")) {
					ALU_result = check_length(Integer.toBinaryString(Integer
							.parseInt(Read_data_1, 2)
							| Integer.parseInt(Read_data_2, 2)));
				} else {
					ALU_result = check_length(Integer.toBinaryString(Integer
							.parseInt(Read_data_1, 2)
							| Integer.parseInt(sign_extend, 2)));
				}
				break;
			case "101010":
				if (alusrc.equals("0")) {
					if (Integer.parseInt(Read_data_1, 2) < Integer.parseInt(
							Read_data_2, 2)) {
						ALU_result = "00000000000000000000000000000001";
					} else {
						ALU_result = "00000000000000000000000000000000";
					}

				} else {
					if (Integer.parseInt(Read_data_1, 2) < Integer.parseInt(
							sign_extend, 2)) {
						ALU_result = "00000000000000000000000000000001";
					} else {
						ALU_result = "00000000000000000000000000000000";
					}

				}
				break;
			case "101011":
				if (alusrc.equals("0")) {
					if (Integer.parseInt(Read_data_1, 2) < Integer.parseInt(
							Read_data_2, 2)) {
						ALU_result = "00000000000000000000000000000001";
					} else {
						ALU_result = "00000000000000000000000000000000";
					}

				} else {
					if (Integer.parseInt(Read_data_1, 2) < Integer.parseInt(
							sign_extend, 2)) {
						ALU_result = "00000000000000000000000000000001";
					} else {
						ALU_result = "00000000000000000000000000000000";
					}

				}
				break;
			case "000000":
				int shamt = Integer.parseInt(Read_data_2, 2);
				ALU_result = shift(Read_data_1, shamt, "left");
				break;
			case "000010":
				int shamtr = Integer.parseInt(Read_data_2, 2);
				ALU_result = shift(Read_data_1, shamtr, "right");
				break;
			case "100111":
				ALU_result = (check_length(Integer.toBinaryString(~(Integer
						.parseInt(Read_data_1, 2) | Integer.parseInt(
						Read_data_2, 2)))));
				break;
			case "001000":
				result.put("branch", "1");
				result.put("regWrite", "0");
				Zero = "1";
				String y = check_length(Read_data_1);
				result.put("add_result", y);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		result.put("ALU_result", ALU_result);
		result.put("Zero", Zero);
		String regdst = decode.get("regDst");
		if (regdst.equals("0")) {
			result.put("regdst", decode.get("instruction_20_16"));

		} else {
			result.put("regdst", decode.get("instruction_15_11"));
		}
		result.put("read_data_2", Read_data_2);
		result.put("memWrite", decode.get("memWrite"));
		result.put("memRead", decode.get("memRead"));
		result.put("memToReg", decode.get("memToReg"));
		return result;
	}

	private String shift(String y, int x, String type) {
		switch (type) {
		case "left":
			for (int i = 0; i < x; i++) {
				y += "0";
			}
			y = y.substring(x, y.length());
			break;
		case "right":
			for (int i = 0; i < x; i++) {
				y = "0" + y;
			}
			y = y.substring(0, y.length() - x);
			break;
		default:
			break;
		}
		return y;
	}

	String check_length(String s) {
		String new_s = "";
		if (s.length() < 32) {
			for (int i = 0; i < 32 - s.length(); i++) {
				new_s += "0";
			}
			new_s = new_s + s;
			return new_s;
		}

		return s;

	}

	Hashtable<String, String> memory(Hashtable<String, String> ID_EX) {
		Hashtable<String, String> result = new Hashtable<String, String>();
		String writeData = ID_EX.get("read_data_2");
		String branch = ID_EX.get("branch");
		String memWrite = ID_EX.get("memWrite");
		String memRead = ID_EX.get("memRead");
		result.put("regWrite", ID_EX.get("regWrite"));
		result.put("memToReg", ID_EX.get("memToReg"));
		String Zero = ID_EX.get("Zero");
		String address = ID_EX.get("ALU_result");
		result.put("regdst", ID_EX.get("regdst"));
		result.put("ALU_result", ID_EX.get("ALU_result"));
		result.put("add_result", ID_EX.get("add_result"));

		if (branch.equals("0") || Zero.equals("0")) {
			result.put("pcsrc", "0");
		} else {
			result.put("pcsrc", "1");
		}

		if (memRead.equals("1")) {
			result.put(
					"readData",
					(Data_Memory[Integer.parseInt(address, 2)]
							+ Data_Memory[Integer.parseInt(address, 2) + 1]
							+ Data_Memory[Integer.parseInt(address, 2) + 2] + Data_Memory[Integer
							.parseInt(address, 2) + 3]));
		} else if (memWrite.equals("1")) {
			// 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10
			// 9 8 7 6 5 4 3 2 1 0
			// 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
			// 25 26 27 28 29 30 31
			Data_Memory[Integer.parseInt(address, 2)] = writeData.substring(24);
			Data_Memory[Integer.parseInt(address, 2) + 1] = writeData
					.substring(16, 24);
			Data_Memory[Integer.parseInt(address, 2) + 2] = writeData
					.substring(8, 16);
			Data_Memory[Integer.parseInt(address, 2) + 3] = writeData
					.substring(0, 8);

			result.put("readData", "00000000000000000000000000000000");
		}

		return result;
	}

	Hashtable<String, String> write_back(Hashtable<String, String> MEM_WB) {
		Hashtable<String, String> h = new Hashtable<String, String>();

		String regWrite = MEM_WB.get("regWrite");
		String memToReg = MEM_WB.get("memToReg");
		String regdst = MEM_WB.get("regdst");
		String ALU_result = MEM_WB.get("ALU_result");
		String readData = MEM_WB.get("readData");
		String result = "";
		memToReg = "1";
		if (memToReg.equalsIgnoreCase("0")) {
			result = readData;

		}
		if (memToReg.equalsIgnoreCase("1")) {
			result = ALU_result;
		}

		if (regWrite.equals("1")) {

			reg_data.put(regdst, result);
		}
		h.put("regWrite", regWrite);
		h.put("writeData", result);
		h.put("regDst", regdst);
		return h;

	}

	public String and_bin(String a, String b) {

		if (a.equals("1") && b.equals("1"))
			return 1 + "";
		else
			return 0 + "";
	}

	public String or_bin(String a, String b) {

		if (a.equals("1") || b.equals("1"))
			return 1 + "";
		else
			return 0 + "";
	}

	void parser() {

	}

	public int negate(int c) {
		if (c == 0) {
			return 1;
		} else {
			return 0;
		}

	}

	public String getRegByCode(String code) {
		return (this.bin_reg.get(code));
	}

	public String getRegValue(String register) {
		return this.reg_data.get(register);
	}

	public void updateRegValue(String register, String value) {
		this.reg_data.put(register, value);
	}

	Hashtable<String, String> decode(Hashtable<String, String> IF_ID) {
		Hashtable<String, String> ID_EX = new Hashtable<String, String>();
		String instruction = IF_ID.get("instruction");
		String pc = IF_ID.get("pc");
		String regDst;
		String jump;
		String branch;
		String memRead;
		String memToReg;
		String aluOp;
		String memWrite;
		String aluSrc;
		String op = instruction.substring(0, 6);
		ID_EX.put("opcode", op);
		switch (op) {
		case "001000": // addi
			ID_EX.put("aluOp", "00");
			ID_EX.put("regDst", "0");
			ID_EX.put("aluSrc", "1");
			ID_EX.put("memToReg", "0");
			ID_EX.put("regWrite", "1");
			ID_EX.put("memRead", "0");
			ID_EX.put("memWrite", "0");
			ID_EX.put("branch", "0");
			ID_EX.put("jump", "0");
			break;
		case "001010": // andi
			ID_EX.put("aluOp", "00");
			ID_EX.put("regDst", "0");
			ID_EX.put("aluSrc", "1");
			ID_EX.put("memToReg", "0");
			ID_EX.put("regWrite", "1");
			ID_EX.put("memRead", "0");
			ID_EX.put("memWrite", "0");
			ID_EX.put("branch", "0");
			ID_EX.put("jump", "0");
			break;
		case "001101": // ori
			ID_EX.put("aluOp", "00");
			ID_EX.put("regDst", "0");
			ID_EX.put("aluSrc", "1");
			ID_EX.put("memToReg", "0");
			ID_EX.put("regWrite", "1");
			ID_EX.put("memRead", "0");
			ID_EX.put("memWrite", "0");
			ID_EX.put("branch", "0");
			ID_EX.put("jump", "0");
			break;
		default:
			String op5 = op.charAt(0) + "";
			String op4 = op.charAt(1) + "";
			String op3 = op.charAt(2) + "";
			String op2 = op.charAt(3) + "";
			String op1 = op.charAt(4) + "";
			String op0 = op.charAt(5) + "";

			String ans1 = (negate(Integer.parseInt(op0))
					& negate(Integer.parseInt(op1))
					& negate(Integer.parseInt(op2))
					& negate(Integer.parseInt(op3))
					& negate(Integer.parseInt(op4)) & negate(Integer
						.parseInt(op5))) + "";
			String ans2 = (Integer.parseInt(op0) & Integer.parseInt(op1)
					& negate(Integer.parseInt(op2))
					& negate(Integer.parseInt(op3))
					& negate(Integer.parseInt(op4)) & Integer.parseInt(op5))
					+ "";
			String ans3 = (Integer.parseInt(op0) & Integer.parseInt(op1)
					& negate(Integer.parseInt(op2)) & Integer.parseInt(op3)
					& negate(Integer.parseInt(op4)) & Integer.parseInt(op5))
					+ "";

			String ans4 = (negate(Integer.parseInt(op0))
					& negate(Integer.parseInt(op1)) & Integer.parseInt(op2)
					& negate(Integer.parseInt(op3))
					& negate(Integer.parseInt(op4)) & negate(Integer
						.parseInt(op5))) + "";

			regDst = ans1;
			aluSrc = or_bin(ans2, ans3);
			memToReg = ans2;

			memRead = ans2;
			memWrite = ans3;
			branch = ans4;

			String aluOp0 = ans4;
			String aluOp1 = ans1;

			String temp = aluOp1 + "" + aluOp0;
			ID_EX.put("aluOp", temp);
			aluOp = temp;
			String s = op5 + op4 + op3 + op2 + op1 + op0;
			String writeReg = or_bin(ans1, ans2);
			if (s.equals("000010")) {
				jump = "1";
			} else {
				jump = "0";
			}

			String Rtemp = regDst + "";
			ID_EX.put("regDst", Rtemp);

			Rtemp = aluSrc + "";
			ID_EX.put("aluSrc", Rtemp);

			Rtemp = memToReg + "";
			ID_EX.put("memToReg", Rtemp);

			ID_EX.put("regWrite", writeReg);

			Rtemp = memRead + "";
			ID_EX.put("memRead", Rtemp);

			Rtemp = memWrite + "";
			ID_EX.put("memWrite", Rtemp);

			Rtemp = branch + "";
			ID_EX.put("branch", Rtemp);

			Rtemp = jump + "";
			ID_EX.put("jump", Rtemp);
			break;
		}

		String regRead1 = instruction.substring(6, 11);
		String instruction_20_16 = instruction.substring(11, 16);// mux value =
																	// 0
		String instruction_15_11 = instruction.substring(16, 21);// mux value =
																	// 1
		String signExtend = instruction.substring(16, instruction.length());
		String regName = this.getRegByCode(regRead1);
		String read_data_1 = "";
		String read_data_2 = "";
		// Handling sll and srl
		if (op.equals("000000")
				&& (instruction.substring(26, 32).equals("000010") || instruction
						.substring(26, 32).equals("000000"))) {
			read_data_1 = this.getRegValue(instruction_15_11);
			read_data_2 = check_length(instruction.substring(21, 26));
			ID_EX.put("regDst", "0");

		} else {
			read_data_1 = this.getRegValue(regRead1);
			String regName2 = this.getRegByCode(instruction_20_16);
			read_data_2 = this.getRegValue(instruction_20_16);
		}
		String sign32 = this.signExtend(signExtend);

		ID_EX.put("signExtend", sign32);
		ID_EX.put("pc", pc);
		ID_EX.put("read_data_1", read_data_1);
		ID_EX.put("read_data_2", read_data_2);
		ID_EX.put("instruction_20_16", instruction_20_16);
		ID_EX.put("instruction_15_11", instruction_15_11);

		return ID_EX;
	}

	public void set_inst(int sp) {

		int i = 0;

		while (i != sp) {
			instructions_code.add("Empty Memory Cell");
			i++;
		}

	}

	public void intiate() throws IOException {
		is_begin = true;
		reg_bin.put("$0", "00000");// 0
		reg_bin.put("$at", "00001");// 1
		reg_bin.put("$v0", "00010");// 2
		reg_bin.put("$v1", "00011");// 3
		reg_bin.put("$a0", "00100");// 4
		reg_bin.put("$a1", "00101");// 5
		reg_bin.put("$a2", "00110");// 6
		reg_bin.put("$a3", "00111");// 7
		reg_bin.put("$t0", "01000");// 8
		reg_bin.put("$t1", "01001");// 9
		reg_bin.put("$t2", "01010");// 10
		reg_bin.put("$t3", "01011");// 11
		reg_bin.put("$t4", "01100");// 12
		reg_bin.put("$t5", "01101");// 13
		reg_bin.put("$t6", "01110");// 14
		reg_bin.put("$t7", "01111");// 15
		reg_bin.put("$s0", "10000");// 16
		reg_bin.put("$s1", "10001");// 17
		reg_bin.put("$s2", "10010");// 18
		reg_bin.put("$s3", "10011");// 19
		reg_bin.put("$s4", "10100");// 20
		reg_bin.put("$s5", "10101");// 21
		reg_bin.put("$s6", "10110");// 22
		reg_bin.put("$s7", "10111");// 23
		reg_bin.put("$t8", "11000");// 24
		reg_bin.put("$t9", "11001");// 25
		reg_bin.put("$k0", "11010");// 26
		reg_bin.put("$k1", "11011");// 27
		reg_bin.put("$gp", "11100");// 28
		reg_bin.put("$sp", "11101");// 29
		reg_bin.put("$fp", "11110");// 30
		reg_bin.put("$ra", "11111");// 31
		reg_data.put("00000", "00000000000000000000000000000000");// 0
		reg_data.put("00001", "00000000000000000000000000000000");// 1
		reg_data.put("00010", "00000000000000000000000000000000");// 2
		reg_data.put("00011", "00000000000000000000000000000000");// 3
		reg_data.put("00100", "00000000000000000000000000000000");// 4
		reg_data.put("00101", "00000000000000000000000000000000");// 5
		reg_data.put("00110", "00000000000000000000000000000000");// 6
		reg_data.put("00111", "00000000000000000000000000000000");// 7
		reg_data.put("01000", "00000000000000000000000000000000");// 8
		reg_data.put("01001", "00000000000000000000000000000000");// 9
		reg_data.put("01010", "00000000000000000000000000000000");// 10
		reg_data.put("01011", "00000000000000000000000000000000");// 11
		reg_data.put("01100", "00000000000000000000000000000000");// 12
		reg_data.put("01101", "00000000000000000000000000000000");// 13
		reg_data.put("01110", "00000000000000000000000000000000");// 14
		reg_data.put("01111", "00000000000000000000000000000000");// 15
		reg_data.put("10000", "00000000000000000000000000000000");// 16
		reg_data.put("10001", "00000000000000000000000000000000");// 17
		reg_data.put("10010", "00000000000000000000000000000000");// 18
		reg_data.put("10011", "00000000000000000000000000000000");// 19
		reg_data.put("10100", "00000000000000000000000000000000");// 20
		reg_data.put("10101", "00000000000000000000000000000000");// 21
		reg_data.put("10110", "00000000000000000000000000000000");// 22
		reg_data.put("10111", "00000000000000000000000000000000");// 23
		reg_data.put("11000", "00000000000000000000000000000000");// 24
		reg_data.put("11001", "00000000000000000000000000000000");// 25
		reg_data.put("11010", "00000000000000000000000000000000");// 26
		reg_data.put("11011", "00000000000000000000000000000000");// 27
		reg_data.put("11100", "00000000000000000000000000000000");// 28
		reg_data.put("11101", "00000000000000000000000000000000");// 29
		reg_data.put("11110", "00000000000000000000000000000000");// 30
		reg_data.put("11111", "00000000000000000000000000000000");// 31
		String csvFile = "p1.csv";
		// p.jump(csvFile);

		ArrayList<String> a = new ArrayList<String>();
		instructions_code = intialize(csvFile);
		if (instructions_code != null) {
			System.out.println(instructions_code.toString());
		} else {
			System.out.println("the list is null");
		}
		pipelined();
	}

	public static void main(String[] args) throws IOException {
		processor p = new processor();
		p.intiate();

	}

	void pipelined() throws IOException {
		System.out.println(instructions_code.toString());
		ArrayList<String> stages = new ArrayList<String>();
		stages.add("fetch");
		stages.add("decode");
		stages.add("execute");
		stages.add("memory");
		stages.add("WB");
		int count = 0;
		int NO = 0;
		boolean flag = false;
		int d = 2;
		while (true) {
			// checking last instruction
			String mmm = "";
			String mmn = "";
			if (pipelinedArray.size() >= 2) {
				ArrayList<String> mm = pipelinedArray
						.get(pipelinedArray.size() - 2);
				mmm = mm.get(mm.size() - 1);
				mm = pipelinedArray.get(pipelinedArray.size() - 1);

				mmn = mm.get(mm.size() - 1);
			}

			if ((mmm.equals("NoOp") && mmn.equals("NoOp"))) {
				flag = true;
				d--;
				if (d == -1) {
					flag = false;
					d = 2;
				}
			}
			if ((!(Integer.parseInt(reg_data.get("11101"), 2) >= instructions_code
					.size())) && !flag) {

				pipelinedArray.add(shiftArray(stages, pipelinedArray.size()));
				String instruction = instructions_code.get(Integer.parseInt(
						reg_data.get("11101"), 2));
				// checking Branch Instruction
				if (isBranch(instruction)) {
					pipelinedArray.add(stall(pipelinedArray.size()));
					pipelinedArray.add(stall(pipelinedArray.size()));
				}

			} else {

				ArrayList<String> uu = pipelinedArray
						.get(pipelinedArray.size() - 1);
				String y = uu.get(uu.size() - 1);
				if (y.equals("WBdone")) {
					break;

				}

			}
			System.out.println(pipelinedArray.toString());

			for (int i = 0; i < pipelinedArray.size(); i++) {
				String stage = "";
				if (count >= pipelinedArray.get(i).size()) {
					stage = "null";
				} else {
					stage = pipelinedArray.get(i).get(count);
					ArrayList<String> nn = pipelinedArray.get(i);
					nn.set(count, stage + "done");
					pipelinedArray.set(i, nn);
				}
				Enumeration x;
				switch (stage) {
				case "null":
					break;
				case "NoOp":
					System.out
							.println("                                  NO_OP");
					break;
				case "fetch":
					System.out
							.println("                                     fetch"
									+ "    " + i);
					IF_ID = fetch(START);
					x = IF_ID.keys();
					while (x.hasMoreElements()) {
						String str = (String) x.nextElement();
						System.out.println(str + ": " + IF_ID.get(str));
					}
					break;
				case "decode":
					System.out
							.println("                                     decode"
									+ "    " + i);
					ID_EX = decode(IF_ID);
					x = ID_EX.keys();
					while (x.hasMoreElements()) {
						String str = (String) x.nextElement();
						System.out.println(str + ": " + ID_EX.get(str));
					}
					break;
				case "execute":
					EX_MEM = execute(ID_EX);
					System.out
							.println("                                     execute"
									+ "    " + i);
					x = EX_MEM.keys();
					while (x.hasMoreElements()) {
						String str = (String) x.nextElement();
						System.out.println(str + ": " + EX_MEM.get(str));
					}
					break;
				case "memory":

					MEM_WB = memory(EX_MEM);
					System.out
							.println("                                      MEMORY"
									+ "    " + i);
					START.put("add_result", MEM_WB.get("add_result"));
					START.put("pcsrc", MEM_WB.get("pcsrc"));
					x = MEM_WB.keys();
					while (x.hasMoreElements()) {
						String str = (String) x.nextElement();
						System.out.println(str + ": " + MEM_WB.get(str));
					}
					x = START.keys();
					while (x.hasMoreElements()) {
						String str = (String) x.nextElement();
						System.out.println(str + ": " + START.get(str));
					}
					break;
				case "WB":
					write_back(MEM_WB);
					System.out
							.println("                                      Write_back"
									+ "    " + i);
					x = reg_data.keys();
					while (x.hasMoreElements()) {
						String str = (String) x.nextElement();
						System.out.println(str + ": " + reg_data.get(str));
					}
					break;
				default:
					break;
				}
			}
			count++;
		}

	}

	private ArrayList<String> shiftArray(ArrayList<String> x, int y) {
		ArrayList<String> shifted = new ArrayList<String>();
		for (int i = 0; i < y; i++) {
			shifted.add("null");
		}
		for (String string : x) {
			shifted.add(string);
		}

		return shifted;
	}

	private ArrayList<String> stall(int y) {
		ArrayList<String> shifted = new ArrayList<String>();
		for (int i = 0; i < y; i++) {
			shifted.add("null");
		}
		for (int i = 0; i < 5; i++) {
			shifted.add("NoOp");
		}

		return shifted;
	}

	boolean isBranch(String s) {
		String x = s.substring(0, 6);
		String y = s.substring(26);
		if (x.equals("000100") || x.equals("000101") || x.equals("000010")
				|| x.equals("000011")
				|| (x.equals("000000") && y.equals("001000")))
			return true;
		return false;
	}

	boolean isLast(String x) {
		return false;
	}

}
