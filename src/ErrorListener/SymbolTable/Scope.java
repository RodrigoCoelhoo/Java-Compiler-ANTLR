package ErrorListener.SymbolTable;

import java.util.LinkedHashMap;
import java.util.Map;

public class Scope {
    Scope enclosingScope;    // null if global (outermost) scope
    String name;             // for debug, there's really no need for it otherwise.
    Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();

    public Scope(Scope enclosingScope) {
        this.enclosingScope = enclosingScope;
        this.name = "noname";
    }

    public Scope(Scope enclosingScope, String name) {
        this.enclosingScope = enclosingScope;
        this.name = name;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    // return true if identifier name is in this scope, and return false otherwise
    public boolean contains(String name) {
        return resolve_local(name) != null;
    }

    // look for identifier name in this scope alone.
    // return Symbol if found, and null otherwise.
    public Symbol resolve_local(String name) {
        return symbols.get(name);
    }

    public Symbol resolve(String name) {
        Symbol s = resolve_local(name);
        if ( s!=null ) return s;
        // if not here, check any enclosing scope
        if ( enclosingScope != null ) return enclosingScope.resolve(name);
        return null; // not found
    }

    public void define(Symbol sym) {
        symbols.put(sym.lexeme(), sym);
        sym.scope = this; // track the scope in each symbol
    }

    public Scope getEnclosingScope() { return enclosingScope; }

    public String toString() {
        if ( enclosingScope != null )
            return getName() + ":" + symbols.keySet().toString()
                      + " --> " + enclosingScope.toString() ;
        else
            return getName() + ":" + symbols.keySet().toString();   // global scope
    }
}
