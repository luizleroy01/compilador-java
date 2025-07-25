package src.TableOfSymbols;

import src.LexicalAnalyzer.*;
import java.util.*;
public class Env {
    public Hashtable<String, Id> table;
    public Env prev;

    public Env(Env n) {
        table = new Hashtable<>();
        prev = n;
    }

    public void put(Token w, Id i) {
        table.put(w.getLexeme(), i);
    }

    public Id get(Token w) {
        for (Env e = this; e != null; e = e.prev) {
            Id found = e.table.get(w.getLexeme());
            if (found != null)
                return found;
        }
        return null;
    }

    public Hashtable<String, Id> getTable() {
        return table;
    }
}


