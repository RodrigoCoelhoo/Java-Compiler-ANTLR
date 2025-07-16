package ErrorListener.SymbolTable;
import org.antlr.v4.runtime.Token;
import ErrorListener.TypeChecker.Type;

public class Symbol {
    Token token;
    Type type;
    Scope scope;

    public Symbol(Token token) { this.token = token; }
    public Symbol(Token token, Type type) { this(token); this.type = type; }

    public Token getToken() { return token; }
    public String lexeme() { return getToken().getText(); }
    public Type type() { return type; }

    @Override
    public String toString() {
        return '<' + lexeme() + ":" + type + '>';
    }
}

