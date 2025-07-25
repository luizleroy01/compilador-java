package src.TableOfSymbols;

import src.LexicalAnalyzer.*;
import java.util.*;

public class Env {
    public Hashtable<Token, Id> table;  // Tipado corretamente
    public Env prev;  // Ambiente anterior

    public Env(Env n) {
        table = new Hashtable<>();
        prev = n;
    }

    public void put(Token w, Id i) {
        table.put(w, i);
    }

    public Id get(Token w) {
        for (Env e = this; e != null; e = e.prev) {
            Id found = e.table.get(w);
            if (found != null)
                return found;
        }
        return null;
    }
}
