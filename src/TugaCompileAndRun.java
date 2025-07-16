import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;
import ErrorListener.*;
import CodeGenerator.*;
import Tuga.*;
import VM.*;


public class TugaCompileAndRun {
    private static boolean showLexerErrors = false;
    private static boolean showParserErrors = false;
    private static boolean showTypeCheckingErrors = true;

    public static void main(String[] args) {
        try {
            processArguments(args);
            CharStream input;

            if (args.length > 0 && !args[0].startsWith("-"))
            {
                if (!args[0].matches(".*\\.Tuga$"))
                {
                    System.err.println("Error: The file must have .Tuga extension");
                    System.exit(0);
                }

                try {
                    input = CharStreams.fromFileName(args[0]);
                } catch (IOException e) {
                    System.err.println("Error opening file: " + args[0]);
                    e.printStackTrace();
                    System.exit(0);
                    return;
                }
            }
            else
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                StringBuilder inputBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    inputBuilder.append(line).append("\n");
                }

                input = CharStreams.fromString(inputBuilder.toString());
            }

            // Lexer
            TugaLexer lexer = new TugaLexer(input);
            lexer.removeErrorListeners();
            MyErrorListener lexerErrorListener = new MyErrorListener(showLexerErrors, false);
            lexer.addErrorListener(lexerErrorListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Parser
            TugaParser parser = new TugaParser(tokens);
            parser.removeErrorListeners();
            MyErrorListener parserErrorListener = new MyErrorListener(false, showParserErrors);
            parser.addErrorListener(parserErrorListener);

            ParseTree tree = parser.program();

            // Check for lexer or parser errors
            if (lexerErrorListener.getNumLexerErrors() > 0) {
                System.out.println("Input has lexical errors");
                System.exit(0);
            }

            if (parserErrorListener.getNumParsingErrors() > 0) {
                System.out.println("Input has parsing errors");
                System.exit(0);
            }

            // Type checking
            TypeChecker typeChecker = new TypeChecker();
            typeChecker.visit(tree);

            if (!typeChecker.getErrors().isEmpty()) {
                if (showTypeCheckingErrors) {
                    typeChecker.getErrors().sort((e1, e2) -> {
                        int line1 = Integer.parseInt(e1.split(" ")[3].replace(":", ""));
                        int line2 = Integer.parseInt(e2.split(" ")[3].replace(":", ""));
                        return Integer.compare(line1, line2);
                    });

                    for (String error : typeChecker.getErrors()) {
                        System.out.println(error);
                    }
                } else {
                    System.out.println("Input has type checking errors");
                }
                System.exit(0);
            }

            // Code generation
            CodeGen codeGen = new CodeGen(typeChecker);
            codeGen.visit(tree);

            // Save and execute bytecodes
            codeGen.saveBytecodes("bytecodes.bc");
            codeGen.dumpCode();

            // Run VM
            byte[] bytecodes = loadBytecodes("bytecodes.bc");
            vm VM = new vm(bytecodes, true);
            VM.run();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void processArguments(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case "-showLexerErrors":
                    showLexerErrors = true;
                    break;
                case "-showParserErrors":
                    showParserErrors = true;
                    break;
                case "-showTypeCheckingErrors":
                    showTypeCheckingErrors = true;
                    break;
                case "-allErrors":
                    showLexerErrors = true;
                    showParserErrors = true;
                    showTypeCheckingErrors = true;
                    break;
            }
        }
    }


    public static byte[] loadBytecodes(String filename) throws IOException {
        try {
            File file = new File(filename);
            byte [] bytecodes = new byte[(int) file.length()];
            try(FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytecodes);
            }
            return bytecodes;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
