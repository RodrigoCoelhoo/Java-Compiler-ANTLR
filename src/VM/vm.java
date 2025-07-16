package VM;

import VM.Instruction.*;

import java.util.*;
import java.io.*;


public class vm {
    private final boolean trace;
    private final byte[] bytecodes;
    private Instruction[] code;
    private int IP;
    private final Stack<Object> stack = new Stack<>();
    private List<Object> constantPool;

    private Object[] global;

    private final Stack<Frame> frameStack = new Stack<>();

    private static class Frame {
        int returnAddress;
        int fp;

        Frame(int returnAddress, int fp) {
            this.returnAddress = returnAddress;
            this.fp = fp;
        }
    }


    public vm( byte [] bytecodes, boolean trace ) {
        this.trace = trace;
        this.bytecodes = bytecodes;
        decode(bytecodes);
        this.IP = 0;
        this.global = new Object[0];
        this.frameStack.push(new Frame(-1, 0)); // Frame global
    }

    private void decode(byte[] bytecodes) {
        ArrayList<Instruction> inst = new ArrayList<>();
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytecodes));

            int poolSize = din.readInt();
            this.constantPool = new ArrayList<>(poolSize);

            for (int i = 0; i < poolSize; i++) {
                byte typeTag = din.readByte();
                switch (typeTag) {
                    case 1: // Double
                        constantPool.add(din.readDouble());
                        break;
                    case 3: // String
                        int length = din.readInt();
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < length; j++) {
                            sb.append(din.readChar());
                        }
                        constantPool.add(sb.toString());
                        break;
                }
            }

            while (true) {
                byte b = din.readByte();
                OpCode opc = OpCode.convert(b);
                switch (opc.nArgs()) {
                    case 0:
                        inst.add(new Instruction(opc));
                        break;
                    case 1:
                        int val = din.readInt();
                        inst.add(new Instruction1Arg(opc, val));
                        break;
                    default:
                        System.out.println("Formato de instrução inválido");
                        System.exit(0);
                }
            }
        } catch (EOFException e) {
            this.code = inst.toArray(new Instruction[0]);
            if (trace) {
                System.out.println("Instruções desmontadas:");
                dumpInstructionsAndBytecodes();
            }
        } catch (IOException e) {
            System.out.println("Erro ao decodificar bytecodes: " + e);
            System.exit(0);
        }
    }

    // dump the instructions, along with the corresponding bytecodes
    public void dumpInstructionsAndBytecodes() {
        int idx = 0;
        for (int i=0; i< code.length; i++) {
            StringBuilder s = new StringBuilder();
            s.append(String.format("%02X ", bytecodes[idx++]));
            if (code[i].nArgs() == 1)
                for (int k=0; k<4; k++)
                    s.append(String.format("%02X ", bytecodes[idx++]));
            System.out.println( String.format("%5s: %-15s // %s", i, code[i], s) );
        }
    }

    // dump the instructions to the screen
    public void dumpInstructions() {
        for (int i=0; i< code.length; i++)
            System.out.println( i + ": " + code[i] );
    }

    private void runtime_error(String msg) {
        System.out.println("erro de runtime: " + msg);
        if (trace)
            System.out.println( String.format("%22s Stack: %s", "", stack ) );
        System.exit(0);
    }

    /**
     * Trabalho 3
     *
     */

    private void exec_lalloc(int n) {
        for (int i = 0; i < n; i++) {
            stack.push(null);
        }
    }


    private void exec_lload(int addr) {
        Frame currentFrame = frameStack.peek();
        int stackIndex = currentFrame.fp + addr;
        if (stackIndex < 0 || stackIndex >= stack.size() || stack.get(stackIndex) == null) {
            runtime_error("tentativa de acesso a valor NULO");
        }
        stack.push(stack.get(stackIndex));
    }


    private void exec_lstore(int addr) {
        Frame currentFrame = frameStack.peek();
        int stackIndex = currentFrame.fp + addr;
        if (stackIndex < 0 || stackIndex >= stack.size()) {
            runtime_error("tentativa de acesso a valor NULO");
        }
        Object value = stack.pop();
        stack.set(stackIndex, value);
    }


    private void exec_pop(int n) {
        for (int i = 0; i < n; i++) {
            stack.pop();
        }
    }

    private void exec_call(int addr) {
        Frame currentFrame = frameStack.peek();
        stack.push(currentFrame.fp);   // FP anterior (deve ser o FP do frame atual)
        stack.push(IP + 1);            // endereço de retorno

        // O novo FP é o topo da stack depois de empilhar os dois valores acima
        int newFp = stack.size() - 2;  // Corrigido: -2 porque já empilhamos FP e return address

        // Cria nova frame
        frameStack.push(new Frame(IP + 1, newFp));

        // Salta para a função
        IP = addr - 1;
    }


    private void exec_retval(int n) {
        Object returnValue = stack.pop();
        Frame currentFrame = frameStack.pop(); // Armazenar a frame atual e voltar para a frame anterior

        // Limpar tudo desta função
        while (stack.size() > currentFrame.fp) {
            stack.pop();
        }

        // Remove os args
        for (int i = 0; i < n; i++) {
            if (stack.isEmpty()) {
                runtime_error("tentativa de remover muitos argumentos");
            }
            stack.pop();
        }

        // Colocar o valor do return na stack
        stack.push(returnValue);

        IP = currentFrame.returnAddress - 1;
    }


    private void exec_ret(int n) {
        // Remove frame atual
        Frame currentFrame = frameStack.pop();

        // Limpar tudo desta função
        int elementsToRemove = stack.size() - currentFrame.fp;
        for (int i = 0; i < elementsToRemove; i++) {
            stack.pop();
        }

        // Remove os args
        for (int i = 0; i < n; i++) {
            if (!stack.isEmpty()) {
                stack.pop();
            }
        }

        IP = currentFrame.returnAddress - 1;
    }

    /**
     * Trabalho 2
     *
     */

    private void exec_galloc(int n) {
        global = new Object[n+ global.length];
        Arrays.fill(global, null);
    }

    private void exec_gload(int addr) {
        if (addr >= global.length || global[addr] == null) {
            runtime_error("tentativa de acesso a valor NULO");
        }

        stack.push(global[addr]);
    }

    private void exec_gstore(int addr) {
        if (addr >= global.length) {
            runtime_error("tentativa de acesso a valor NULO");
        }
        global[addr] = stack.pop();
    }

    private void exec_jump(int addr) {
        IP = addr - 1; // Remover 1 pq dps é adicionado ao "mudar de instrução"
    }

    private void exec_jumpf(int addr) {
        boolean cond = (boolean)stack.pop();
        if (!cond) {
            IP = addr - 1; // Remover 1 pq dps é adicionado ao "mudar de instrução"
        }
    }

    /**
     * Trabalho 1
     *
     */

    private void exec_iconst(int val) {
        stack.push(val);
    }

    private void exec_dconst(int index) {
        stack.push(constantPool.get(index));
    }

    private void exec_sconst(int index) {
        stack.push(constantPool.get(index));
    }

    private void exec_tconst() {
        stack.push(true);
    }

    private void exec_fconst() {
        stack.push(false);
    }

    private void exec_iuminus() {
        stack.push(-(int)stack.pop());
    }

    private void exec_duminus() {
        stack.push(-(double)stack.pop());
    }

    private void exec_not() {
        stack.push(!(boolean)stack.pop());
    }

    private void exec_iadd() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        stack.push(left + right);
    }

    private void exec_dadd() {
        double right = (double)stack.pop();
        double left = (double)stack.pop();
        stack.push(left + right);
    }

    private void exec_isub() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        stack.push(left - right);
    }

    private void exec_dsub() {
        double right = (double)stack.pop();
        double left = (double)stack.pop();
        stack.push(left - right);
    }

    private void exec_imult() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        stack.push(left * right);
    }

    private void exec_dmult() {
        double right = (double)stack.pop();
        double left = (double)stack.pop();
        stack.push(left * right);
    }

    private void exec_idiv() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        if (right != 0) stack.push(left / right);
        else runtime_error("Divisão por zero");
    }

    private void exec_ddiv() {
        double right = (double)stack.pop();
        double left = (double)stack.pop();
        if (right != 0.0) stack.push(left / right);
        else runtime_error("Divisão por zero");
    }

    private void exec_imod() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        if (right != 0) stack.push(left % right);
        else runtime_error("Módulo por zero");
    }

    private void exec_ilt() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        stack.push(left < right);
    }

    private void exec_dlt() {
        double right = (double)stack.pop();
        double left = (double)stack.pop();
        stack.push(left < right);
    }

    private void exec_ileq() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        stack.push(left <= right);
    }

    private void exec_dleq() {
        double right = (double)stack.pop();
        double left = (double)stack.pop();
        stack.push(left <= right);
    }

    private void exec_ieq() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        stack.push(left == right);
    }

    private void exec_deq() {
        double right = (double)stack.pop();
        double left = (double)stack.pop();
        stack.push(left == right);
    }

    private void exec_ineq() {
        int right = (int)stack.pop();
        int left = (int)stack.pop();
        stack.push(left != right);
    }

    private void exec_dneq() {
        double right = (double)stack.pop();
        double left = (double)stack.pop();
        stack.push(left != right);
    }

    private void exec_beq() {
        boolean right = (boolean)stack.pop();
        boolean left = (boolean)stack.pop();
        stack.push(left == right);
    }

    private void exec_bneq() {
        boolean right = (boolean)stack.pop();
        boolean left = (boolean)stack.pop();
        stack.push(left != right);
    }

    private void exec_seq() {
        String right = (String)stack.pop();
        String left = (String)stack.pop();
        stack.push(left.equals(right));
    }

    private void exec_sneq() {
        String right = (String)stack.pop();
        String left = (String)stack.pop();
        stack.push(!left.equals(right));
    }

    private void exec_and() {
        boolean right = (boolean)stack.pop();
        boolean left = (boolean)stack.pop();
        stack.push(left && right);
    }

    private void exec_or() {
        boolean right = (boolean)stack.pop();
        boolean left = (boolean)stack.pop();
        stack.push(left || right);
    }

    private void exec_itod() {
        stack.push((double)(int)stack.pop());
    }

    private void exec_itos() {
        stack.push("\"" + Integer.toString((int)stack.pop()) + "\"");
    }

    private void exec_dtos() {
        stack.push("\"" + Double.toString((double)stack.pop()) + "\"");
    }

    private void exec_btos() {
        stack.push("\"" + ((boolean)stack.pop() == true ? "verdadeiro" : "falso") + "\"");
    }

    private void exec_sconcat() {
        String right = (String)stack.pop();
        String left = (String)stack.pop();
        stack.push(left.substring(0, left.length() - 1) + right.substring(1));
    }

    private void exec_iprint() {
        System.out.println(stack.pop());
    }

    private void exec_dprint() {
        System.out.println(stack.pop());
    }

    private void exec_sprint() {
        String str = (String) stack.pop();
        System.out.println(str.substring(1, str.length() - 1));
    }

    private void exec_bprint() {
        System.out.println(((Boolean)stack.pop()).equals(true) ? "verdadeiro" : "falso");
    }

    private void exec_halt() {
        //if (trace) System.out.println("Programa terminado (halt)");
        if (trace) {
            System.out.println(String.format("%22s Globals: %s", "", Arrays.toString(global)));
            System.out.println(String.format("%22s Stack: %s", "", stack));
        }
        System.exit(0);
    }

    private void exec_inst( Instruction inst ) {
        if (trace) {
            printState();
        }
        OpCode opc = inst.getOpCode();
        switch(opc) {
            case iconst: exec_iconst(((Instruction1Arg)inst).getArg()); break;
            case dconst: exec_dconst(((Instruction1Arg)inst).getArg()); break;
            case sconst: exec_sconst(((Instruction1Arg)inst).getArg()); break;
            case tconst: exec_tconst(); break;
            case fconst: exec_fconst(); break;
            case iuminus: exec_iuminus(); break;
            case duminus: exec_duminus(); break;
            case not: exec_not(); break;
            case iadd: exec_iadd(); break;
            case dadd: exec_dadd(); break;
            case isub: exec_isub(); break;
            case dsub: exec_dsub(); break;
            case imult: exec_imult(); break;
            case dmult: exec_dmult(); break;
            case idiv: exec_idiv(); break;
            case ddiv: exec_ddiv(); break;
            case imod: exec_imod(); break;
            case ilt: exec_ilt(); break;
            case dlt: exec_dlt(); break;
            case ileq: exec_ileq(); break;
            case dleq: exec_dleq(); break;
            case ieq: exec_ieq(); break;
            case deq: exec_deq(); break;
            case ineq: exec_ineq(); break;
            case dneq: exec_dneq(); break;
            case beq: exec_beq(); break;
            case bneq: exec_bneq(); break;
            case seq: exec_seq(); break;
            case sneq: exec_sneq(); break;
            case and: exec_and(); break;
            case or: exec_or(); break;
            case itod: exec_itod(); break;
            case itos: exec_itos(); break;
            case dtos: exec_dtos(); break;
            case btos: exec_btos(); break;
            case sconcat: exec_sconcat(); break;
            case iprint: exec_iprint(); break;
            case dprint: exec_dprint(); break;
            case sprint: exec_sprint(); break;
            case bprint: exec_bprint(); break;
            case halt: exec_halt(); break;
            case jump: exec_jump(((Instruction1Arg)inst).getArg()); break;
            case jumpf: exec_jumpf(((Instruction1Arg)inst).getArg()); break;
            case galloc: exec_galloc(((Instruction1Arg)inst).getArg()); break;
            case gload: exec_gload(((Instruction1Arg)inst).getArg()); break;
            case gstore: exec_gstore(((Instruction1Arg)inst).getArg()); break;
            case lalloc: exec_lalloc(((Instruction1Arg)inst).getArg()); break;
            case lload: exec_lload(((Instruction1Arg)inst).getArg()); break;
            case lstore: exec_lstore(((Instruction1Arg)inst).getArg()); break;
            case pop: exec_pop(((Instruction1Arg)inst).getArg()); break;
            case call: exec_call(((Instruction1Arg)inst).getArg()); break;
            case retval: exec_retval(((Instruction1Arg)inst).getArg()); break;
            case ret: exec_ret(((Instruction1Arg)inst).getArg()); break;

            default:
                System.out.println("This should never happen! In file vm.java, method exec_inst()");
                System.exit(0);
        }
    }

    public void run() {
        System.out.println("*** VM output ***");

        if (trace) {
            System.out.println("Trace while running the code");
            System.out.println("Execution starts at instrution " + IP);
        }
        while (IP < code.length) {
            exec_inst(code[IP]);
            IP++;
        }
        if (trace)
            System.out.println( String.format("%22s Stack: %s", "", stack ) );
    }

    private void printState() {
        System.out.println(String.format("%22s Globals: %s", "", Arrays.toString(global)));
        System.out.println(String.format("%22s Stack: %s", "", stack));
        System.out.println(String.format("%22s IP: %d  FP: %d", "", IP, frameStack.peek().fp));
        System.out.println(String.format("%5s: %-15s", IP < code.length ? IP : code.length-1,
                IP < code.length ? code[IP] : ""));
    }

}