package src.TableOfSymbols;

import src.LexicalAnalyzer.Token;
import java.util.Hashtable;

public class Env {
    public Hashtable<Token, Id> table;
    public Env prev;

    public Env(Env n) {
        table = new Hashtable<>();
        prev = n;
    }

    public void put(Token w, Id i) {
        table.put(w, i);
    }

    public Id get(Token w) {
        for (Env e = this; e != null; e = e.prev) {
            for (Token key : e.table.keySet()) {
                if (key.getLexeme().equals(w.getLexeme())) {
                    return e.table.get(key);
                }
            }
        }
        return null;
    }

    public Hashtable<Token, Id> getTable() {
        return table;
    }
}
