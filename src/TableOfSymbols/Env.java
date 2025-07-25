package src.TableOfSymbols;

import src.LexicalAnalyzer.*;
import java.util.*;

public class Env {
    public Hashtable<String, Id> table;  // <<< Usa String como chave
    public Env prev;

    public Env(Env n) {
        table = new Hashtable<>();
        prev = n;
    }

    public void put(Token w, Id i) {
        table.put(w.getLexeme(), i); // <<< Armazena pelo lexema
    }

    public Id get(Token w) {
        for (Env e = this; e != null; e = e.prev) {
            Id found = e.table.get(w.getLexeme()); // <<< Consulta pelo lexema
            if (found != null)
                return found;
        }
        return null;
    }

    public Hashtable<String, Id> getTable() {
        return table;
    }
}

