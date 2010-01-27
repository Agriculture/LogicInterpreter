import logicinterpreter.base.*;
import logicinterpreter.cnf.*;
import logicinterpreter.expressions.*;
import java.util.*;

public class MyInterpreter  implements IInterpreter
{
    private Expression expression;
    //Bereitstellen der sowieso nötigen Umwandlung in konjunktive
    //Normalform
    public CnfExpression toCnf(Expression expression)
    {
        //Expression ist die Basisklasse für viele mögliche Ausdrücke: Konstanten, Symbole, Und-verknüpfte Ausdrücke, ...
        //welche davon gemeint ist, erfährt man über getTyp()
        //Die Expression kann man dann in die entsprechende Klasse casten, um ihre Details auszulesen
        //und sie in KNF umwandeln zu können:
        //if(expression.getTyp()==Expression.Typ.CONSTANT)
        //{
        //     ConstExpression ce=(ConstExpression) expression;
        //     boolean val=ce.getValue();
        //     return makeKNFFromConst(val);
        //}

        //Die konjunktive Normalform ist wie folgt repräsentiert:
        //->Auf oberster Ebene gibt es die CnfExpression
        //  ->diese beinhaltet eine Liste von Clause-Objekten (d.h. konjunktiv verknüpfte Klauseln)
        //    ->jede Klausel beinhaltet eine List von Term-Objekten (d.h. disjunktiv verknüpfte Terme)
        //      -> ein Term ist eine boolsche Variable, die negiert sein kann, also bspw. "A" oder "~B"
        //Zusammengesetzt wird das folgendermassen:
        //Term a=new Term("A",false); //nicht negiert
        //Term nicht_b=new Term("B",true); //negiert
        //ArrayList<Term> terme=new ArrayList<Term>();
        //terme.add(a);
        //terme.add(nicht_b);
        //Clause clause=new Clause(terme); //A,~B.
        //ArrayList<Clause> clauses=new ArrayList<Clause>():
        //clauses.add(clause);
        //return new CnfExpression(clauses); //KNF hat nur eine Klausel: A,~B.

        //****************************************
        //Student begin
        this.expression = expression;
        eliminateImplications();

        return new CnfExpression(new ArrayList<Clause>());

        //Student end
        //****************************************
    }

    //Lösbarkeit des logischen Ausdrucks prüfen, falls nicht lösbar
    //null zurückgeben, sonst Symbolbelegung, die den Ausdruck gültig macht
    public HashMap<String,Boolean> getSolution(CnfExpression exprCnf)
    {
        // Es gibt 3 Möglichkeiten für diese Expression:
        // a) Tautologie, d.h. jede Variablenbelegung macht sie gültig
        //    -> return new HashMap<String,Boolean>();
        // b) nicht erfüllbar:
        //   -> return null;
        // c) eine bestimmte Variablenbelegung macht die Aussage wahr:
        //   -> bei der Symbolbelegung ist der Key der Name der zu belegenden Variable
        //      und der Wert die Belegung
        //      D.h. bei der CnfExpression "A,~B." würde die Belegung von A mit true
        //      den Ausdruck wahr machen. Eine Lösung wäre also bspw.:
        //        HashMap<String,Boolean> belegung=new HashMap<String,Boolean>();
        //        belegung.put("A",true);
        //        return belegung;

        //****************************************
        //Student begin

        return null;

        //Student end
        //****************************************
    }

    //Falls bereits getSolution aufgerufen wurde, weitere Lösungen suchen,
    //falls keine mehr gefunden wird, null zurückgeben
    //falls bereits in einem Schritt keine Lösung gefunden wurde, eine
    //Exception werfen
    public HashMap<String,Boolean> getNextSolution() throws Exception
    {
        //oftmals gibt es mehrere Lösungen.
        //Man kann den Löser dazu bringen, weitere Lösungen zu finden, indem man die
        //letzte Lösung negiert hinzufügt, d.h. das ist nun keine Lösung mehr.
        //Im obigen Bsp. war der urspüngliche KNF-Ausdruck "A,~B."
        //gefunden wurde die Lösung "A=true"
        //um weitere Lösungen zu finden fügt man "~A" zum Ausdruck hinzu und sucht erneut:
        //"A,~B.
        // ~A."
        //diesmal wäre eine Lösung "A=false, B=false"
        //d.h.
        //HashMap<String,Boolean> belegung=new HashMap<String,Boolean>();
        //belegung.put("A",false);
        //belegung.put("B",false);
        //return belegung;

        //****************************************
        //Student begin

        return null;

        //Student end
        //****************************************
    }

    List<Expression> traverse(Expression exp){
        List<Expression> result = new LinkedList<Expression>();
        switch(exp.getTyp()){
            case CONSTANT:      break; //do nothing
            case SYMBOL:        break; //do nothing
            case NOT:           result.add(((NotExpression) exp).getInnerExpression());
                                break;
            case EQUIVALENCE:   result.add(((EquivalenceExpression) exp).getExpression1());
                                result.add(((EquivalenceExpression) exp).getExpression2());
                                break;
            case IMPLICATION:   result.add(((ImplicationExpression) exp).getPremise());
                                result.add(((ImplicationExpression) exp).getConclusion());
                                break;
            case AND:           for(Expression child : ((AndExpression) exp).getExpressions()){
                                    result.add(child);
                                }
                                break;
            case OR:            for(Expression child : ((OrExpression) exp).getExpressions()){
                                    result.add(child);
                                }
                                break;
            case XOR:           result.add(((XorExpression) exp).getExpression1());
                                result.add(((XorExpression) exp).getExpression2());
                                break;
        }
        return result;
    }

    private void eliminateImplications() {
        List<Expression> queue = new LinkedList<Expression>();
        queue.add(expression);
        while(!queue.isEmpty()){
            Expression exp = queue.remove(0);

            // here is the work
            if(exp.getTyp().equals(Expression.Typ.IMPLICATION)){
                System.out.println("implication found "+exp);
                System.out.println("parent is "+findParent(exp));
            }

            queue.addAll(traverse(exp));
        }

    }

    private Expression findParent(Expression child){
        List<Expression> queue = new LinkedList<Expression>();
        queue.add(expression);
        while(!queue.isEmpty()){
            Expression parent = queue.remove(0);
            List<Expression> children = traverse(parent);
            for(Expression possibleChild : children){
                if(child.equals(possibleChild)){
                    // found parent
                    return parent;
                }
            }
            queue.addAll(children);
        }


        return null;
    }

}