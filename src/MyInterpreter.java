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
		System.out.println("start\n"+this.expression);
        eliminateImpEquXor();
		System.out.println("eliminate implications equivalence xor \n"+this.expression);
		deMorganNot();
		System.out.println("deMorgan and Not: \n"+this.expression);
		pullOutAnd();
		System.out.println("pull out and:\n"+this.expression);

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

	private void pullOutAnd() {
		Boolean flag = true;
        List<Expression> queue = new LinkedList<Expression>();
		while(flag){
			flag = false;
			queue.add(expression);
			while(!queue.isEmpty()){
				Expression exp = queue.remove(0);
				// from (a & b) | c
				// to	(a | c) & (b | c)
				/**
				 * V = (v1 | ... | vn)
				 * vi = (w1 & ... & wk)
				 * => V = W1 & ... Wk)
				 * Wj = (wj & vt) for all t € {1 ... n}\{i}
				 */
				if(exp.getTyp().equals(Expression.Typ.OR)){
					ArrayList<Expression> V = new ArrayList<Expression>();
					for(Expression e : ((OrExpression) exp).getExpressions()){
						V.add(e);
					}
					for(int i=0; i<V.size(); i++){
						if(V.get(i).getTyp().equals(Expression.Typ.AND)){
							ArrayList<Expression> newV = new ArrayList<Expression>();
							AndExpression vi = (AndExpression) V.get(i);
							ArrayList<Expression> vt = new ArrayList<Expression>(V);
							vt.remove(i);
							for(Expression wj : vi.getExpressions()){
								ArrayList<Expression> Wj = new ArrayList<Expression>();
								Wj.add(wj);
								Wj.addAll(vt);

								newV.add(new OrExpression(Wj));
							}
							Expression newChild = new AndExpression(newV);
							replace(exp, newChild);
							exp = newChild;
							flag = true;
							i = V.size();
							queue.clear();
						}
					}
				}
				if(flag == false){
					queue.addAll(traverse(exp));
				}
			}
		}

	}

	private void deMorganNot() {
		Boolean flag = true;
        List<Expression> queue = new LinkedList<Expression>();
		while(flag){
			flag = false;
			queue.add(expression);
			while(!queue.isEmpty()){
				Expression exp = queue.remove(0);
				if(exp.getTyp().equals(Expression.Typ.NOT)){
					NotExpression var = (NotExpression) exp;
					Expression child = var.getInnerExpression();
					if(child.getTyp().equals(Expression.Typ.NOT)){
						// from ¬¬a
						// to	a
						NotExpression var2 = (NotExpression) child;
						Expression newChild = var2.getInnerExpression();
						replace(exp, newChild);
						exp = newChild;
						flag = true;
					}
					if(child.getTyp().equals(Expression.Typ.CONSTANT)){
						// from ¬const
						// to	const
						ConstExpression var2 = (ConstExpression) child;
						Expression newChild = new ConstExpression(!var2.getValue());
						replace(exp, newChild);
						flag = true;
					}
					if(child.getTyp().equals(Expression.Typ.AND)){
						// from ¬(a & b)
						// to	(¬a | ¬b)
						AndExpression var2 =(AndExpression) child;
						ArrayList<Expression> grandChildren = new ArrayList<Expression>();
						for(Expression e : var2.getExpressions()){
							grandChildren.add(new NotExpression(e));
						}
						Expression newChild = new OrExpression(grandChildren);
						replace(exp, newChild);
						exp = newChild;
						flag = true;
					}
					if(child.getTyp().equals(Expression.Typ.OR)){
						// from ¬(a | b)
						// to	(¬a & ¬b)
						OrExpression var2 =(OrExpression) child;
						ArrayList<Expression> grandChildren = new ArrayList<Expression>();
						for(Expression e : var2.getExpressions()){
							grandChildren.add(new NotExpression(e));
						}
						Expression newChild = new AndExpression(grandChildren);
						replace(exp, newChild);
						exp = newChild;
						flag = true;
					}
				}
				queue.addAll(traverse(exp));
			}
		}
	}

    private void eliminateImpEquXor() {
        List<Expression> queue = new LinkedList<Expression>();
        queue.add(expression);
        while(!queue.isEmpty()){
            Expression exp = queue.remove(0);

            // here is the work
            if(exp.getTyp().equals(Expression.Typ.IMPLICATION)){
				ImplicationExpression var = (ImplicationExpression) exp;
				// from a -> b
				// to	b v ¬a
				Expression newChild = new OrExpression(var.getConclusion(), new NotExpression(var.getPremise()));
				replace(exp, newChild);
				//return;
				exp = newChild;
//				System.out.println("new Expression:\n"+expression);
            }
			if(exp.getTyp().equals(Expression.Typ.EQUIVALENCE)){
				EquivalenceExpression var = (EquivalenceExpression) exp;
				// from a <==> b
				// to	(a & b) | (¬a & ¬b)
				Expression newChild = new OrExpression( new AndExpression(var.getExpression1(), var.getExpression2()),
					new AndExpression(new NotExpression(var.getExpression1()), new NotExpression(var.getExpression2())));
				replace(exp, newChild);
				exp = newChild;
			}
			if(exp.getTyp().equals(Expression.Typ.XOR)){
				XorExpression var = (XorExpression) exp;
				// from a ^ b
				// to	(a & ¬b) | (¬a & b)
				Expression newChild = new OrExpression( new AndExpression(var.getExpression1(), new NotExpression(var.getExpression2())),
					new AndExpression(new NotExpression(var.getExpression1()), var.getExpression2()));
				replace(exp, newChild);
				exp = newChild;
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

	private void replace(Expression exp, Expression newChild) {
//		System.out.println("replace\n"+exp+"\nwith\n"+newChild);
		Expression parent = findParent(exp);
//		System.out.println("parent\n"+parent);
		if(parent == null){
			expression = newChild;
		} else {
			Expression child;
			ArrayList<Expression> children;
			switch(parent.getTyp()){
				case NOT:			child = new NotExpression(newChild);
									replace(parent, child);
									break;
				case IMPLICATION:	if(((ImplicationExpression) parent).getPremise().equals(exp)){
										child = new ImplicationExpression(newChild, ((ImplicationExpression) parent).getConclusion());
									} else {
										child = new ImplicationExpression(((ImplicationExpression) parent).getPremise(), newChild);
									}
									replace(parent, child);
									break;
				case EQUIVALENCE:	if(((EquivalenceExpression) parent).getExpression1().equals(exp)){
										child = new EquivalenceExpression(newChild, ((EquivalenceExpression) parent).getExpression2());
									} else {
										child = new EquivalenceExpression(((EquivalenceExpression) parent).getExpression1(), newChild);
									}
									replace(parent, child);
									break;
				case XOR:			if(((XorExpression) parent).getExpression1().equals(exp)){
										child = new XorExpression(newChild, ((XorExpression) parent).getExpression2());
									} else {
										child = new XorExpression(((XorExpression) parent).getExpression1(), newChild);
									}
									replace(parent, child);
									break;
				case AND:			children = new ArrayList<Expression>();
									for(Expression possibleChild : ((AndExpression) parent).getExpressions()){
										if(!possibleChild.equals(exp)){
											children.add(possibleChild);
										} else {
											children.add(newChild);
										}
									}
									child = new AndExpression(children);
									replace(parent, child);
									break;
				case OR:			children = new ArrayList<Expression>();
									for(Expression possibleChild : ((OrExpression) parent).getExpressions()){
										if(!possibleChild.equals(exp)){
											children.add(possibleChild);
										} else {
											children.add(newChild);
										}
									}
									child = new OrExpression(children);
									replace(parent, child);
									break;
			}
		}
	}

}