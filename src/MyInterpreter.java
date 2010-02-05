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
//		onlyBinaryFunctions();
//		System.out.println("only binary:\n"+this.expression);
		surround();
		System.out.println("surround:\n"+this.expression);

		transform();
		System.out.println("toCNF:\n"+this.expression);


		ArrayList<Clause> cnf = makeResult();
		System.out.println("result:\n"+cnf);


        return new CnfExpression(cnf);

//		return new CnfExpression(new ArrayList<Clause>());
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
		List<String> symbols = collectVariables(exprCnf);

		HashMap<String, Boolean> result = DPLL(exprCnf, symbols, new HashMap<String, Boolean>());



        return result;

        //Student end
        //****************************************
    }

	private HashMap<String, Boolean> DPLL(CnfExpression cnf, List<String> symbols, HashMap<String, Boolean> hashMap) {
		if(cnf.getClauses().isEmpty()){
			// empty Conjunction
			// so every mapping make it true
			return new HashMap<String, Boolean>();
		}

		for(Clause clause : cnf.getClauses()){
			if(clause.getTerms().isEmpty()){
				// an empty clause
				// no way there is a mapping to make it true
				return null;
			}
		}


		Boolean allLiteralsMapped = true;

		for(Clause clause : cnf.getClauses()){
			Boolean satisfyClause = null;
			for(Term literal : clause.getTerms()){

				Boolean map = hashMap.get(literal.getName());
				if(map != null){
					if(!literal.isNegated() == map){
						satisfyClause = true;
					} else {
						satisfyClause = false;
					}
				}

				if(map == null){
					allLiteralsMapped = false;
				}
			}

			if(satisfyClause != null && satisfyClause == false){
				// the mapping cant satisfy the cnf
				return null;
			}
		}

		if(allLiteralsMapped){
			// everything mapped and no clause is false
			return hashMap;
		}

		Term term = findPureLiteral(symbols, cnf, hashMap);

		if(term != null){
			List<String> symbols2 = new LinkedList<String>(symbols);
			symbols2.remove(term.getName());

			HashMap<String, Boolean> hashMap2 = new HashMap<String, Boolean>(hashMap);
			hashMap2.put(term.getName(), !term.isNegated());

			return DPLL(cnf, symbols2, hashMap2);
		}

		term = unitClause(cnf, hashMap);

		if(term != null){
			List<String> symbols2 = new LinkedList<String>(symbols);
			symbols2.remove(term.getName());

			HashMap<String, Boolean> hashMap2 = new HashMap<String, Boolean>(hashMap);
			hashMap2.put(term.getName(), !term.isNegated());

			return DPLL(cnf, symbols2, hashMap2);
		}

		// nothing found -> branch
		String symbol = findUnused(symbols, hashMap);

		List<String> symbols2 = new LinkedList<String>(symbols);
		symbols2.remove(symbol);

		HashMap<String, Boolean> hashMap2 = new HashMap<String, Boolean>(hashMap);
		hashMap2.put(symbol, true);

		HashMap<String, Boolean> result = DPLL(cnf, symbols2, hashMap2);

		if(result != null){
			return result;
		} else {
			HashMap<String, Boolean> hashMap3 = new HashMap<String, Boolean>(hashMap);
			hashMap3.put(symbol, false);

			return DPLL(cnf, symbols2, hashMap3);
		}
		
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

	private List<String> collectVariables(CnfExpression cnf) {
		List<String> collect = new LinkedList<String>();
		for(Clause clause : cnf.getClauses()){
			for(Term literal : clause.getTerms()){
				String var = literal.getName();
				if(!collect.contains(var)){
					collect.add(var);
				}
			}
		}

		return collect;
	}

	private Term findPureLiteral(List<String> symbols, CnfExpression cnf, HashMap<String, Boolean> hashMap) {
		for(String symbol : symbols){
			Boolean mapping = null;
			Boolean result = false;
			for(Clause clause : cnf.getClauses()){
				for(Term literal : clause.getTerms()){
					if(literal.getName().equals(symbol)){
						if(mapping == null){
							mapping = literal.isNegated();
							result = true;
						} else {
							if(mapping != literal.isNegated()){
								result = false;
								break;
							}
						}
					}
				}
			}
			if(result == true){
				return new Term(symbol, mapping);
			}
		}
		return null;
	}

	private String findUnused(List<String> symbols, HashMap<String, Boolean> hashMap) {
		for(String symbol : symbols){
			if(hashMap.containsKey(symbol)){
				return symbol;
			}
		}
		return null;
	}

	private CnfExpression reduceCNF(CnfExpression cnf, Term kill) {
		ArrayList<Clause> clauses = new ArrayList<Clause>();

		for(Clause c : cnf.getClauses()){
			ArrayList<Term> terms = new ArrayList<Term>();
			for(Term t : c.getTerms()){
				if(!c.equals(kill)){
					terms.add(t);
				}
			}
			clauses.add(new Clause(terms));
		}


		return new CnfExpression(clauses);
	}

	private void surround() {
		ArrayList<Expression> trans = new ArrayList<Expression>();
		trans.add(expression);
		expression = new OrExpression(trans);
//		System.out.println("or \n"+this.expression);
		trans = new ArrayList<Expression>();
		trans.add(expression);
		expression = new AndExpression(trans);
//		System.out.println("and \n"+this.expression);
		// <[ formula ]>
	}

	private void transform() {


		Boolean flag = true;
		while(flag){
			flag = false;

			AndExpression big = (AndExpression) expression;
//			System.out.println("big \n"+big);
			for(Expression clause : big.getExpressions()){
//				System.out.println("little \n"+clause);
				OrExpression little = (OrExpression) clause;
				for(Expression exp : little.getExpressions()){

					if(!isLiteral(exp)){
						flag = true;
						if(exp.getTyp().equals(Expression.Typ.OR)){

							// from <[ (a | b), d], C>
							// to	<[ a, b, d], C>
							Iterator<Expression> iter = ((OrExpression) exp).getExpressions().iterator();
							List<Expression> ab = new ArrayList<Expression>();
							while(iter.hasNext()){
								ab.add(iter.next());
							}

							ArrayList<Expression> C = new ArrayList<Expression>();
							for(Expression e : big.getExpressions()){
								if(!e.equals(clause))
									C.add(e);
							}
							ArrayList<Expression> d = new ArrayList<Expression>();
							for(Expression f : little.getExpressions()){
								if(!f.equals(exp))
									d.add(f);
							}

							d.addAll(ab);

							C.add(new OrExpression(d));

							expression = new AndExpression(C);
						} else {
							// <[(a & b), d], C]>
							// <[a, d], [b, d], C]>
							Iterator<Expression> iter = ((AndExpression) exp).getExpressions().iterator();
							List<Expression> ab = new ArrayList<Expression>();
							while(iter.hasNext()){
								ab.add(iter.next());
							}

							ArrayList<Expression> d = new ArrayList<Expression>();
							ArrayList<Expression> C = new ArrayList<Expression>();
							for(Expression e : big.getExpressions()){
								if(!e.equals(clause))
									C.add(e);
							}
							for(Expression f : little.getExpressions()){
								if(!f.equals(exp))
									d.add(f);
							}
							for(Expression a : ab){
								ArrayList<Expression> l = new ArrayList<Expression>(d);
								l.add(a);
								C.add(new OrExpression(l));
							}

							expression = new AndExpression(C);
						}
					}
				}
			}
		}
	}

	private boolean isLiteral(Expression exp) {
		if(exp.getTyp().equals(Expression.Typ.CONSTANT))
			return true;
		if(exp.getTyp().equals(Expression.Typ.SYMBOL))
			return true;
		if(exp.getTyp().equals(Expression.Typ.NOT)){
			Expression inner = ((NotExpression) exp).getInnerExpression();
			if(inner.getTyp().equals(Expression.Typ.CONSTANT))
				return true;
			if(inner.getTyp().equals(Expression.Typ.SYMBOL))
				return true;
		}
		return false;
	}

	private ArrayList<Clause> makeResult() {
		ArrayList<Clause> result = new ArrayList<Clause>();
        List<Expression> queue = new LinkedList<Expression>();
		queue.add(expression);
		while(!queue.isEmpty()){
			Expression exp = queue.remove(0);
			if(exp.getTyp().equals(Expression.Typ.AND)){
				queue.addAll(traverse(exp));
			} else if(exp.getTyp().equals(Expression.Typ.OR)){
				ArrayList<Expression> children = new ArrayList<Expression>();
				List<Expression> childQueue = new LinkedList<Expression>();
				childQueue.add(exp);
				while(!childQueue.isEmpty()){
					Expression child = childQueue.remove(0);
					if(child.getTyp().equals(Expression.Typ.OR)){
						childQueue.addAll(traverse(child));
					} else {
						children.add(child);
					}
				}
				// collected all children
				ArrayList<Term> collect = new ArrayList<Term>();
				Boolean deleteClause = false;
				Boolean emptyClause = false;
				for(Expression child : children){
					if(child.getTyp().equals(Expression.Typ.CONSTANT)){
						// true -> clause is true (can be deleted)
						// false -> clause is false (must be empty)
						if(((ConstExpression) child).getValue()){
							deleteClause = true;
							break;
						} else {
							emptyClause = true;
							break;
						}
					} else {
						Boolean negated = false;
						SymbolExpression  var;
						if(child.getTyp().equals(Expression.Typ.NOT)){
							negated = true;
							var = (SymbolExpression) ((NotExpression) child).getInnerExpression();
						} else {
							var = (SymbolExpression) child;
						}
						Term term = new Term(var.getName(), negated);
						// check of already in there
						if(!collect.contains(term)){
							// if the negated version is in there -> tautology -> delete the clause
							if(collect.contains(new Term(term.getName(), !term.isNegated()))){
								deleteClause = true;
								break;
							} else {
								collect.add(term);
							}
						}
					}
				}
				if(!deleteClause){
					if(emptyClause){
						result.add(new Clause(new ArrayList<Term>()));
					} else {
						result.add(new Clause(collect));
					}
				}


			} else {
				// no and and no or -> const or symbol
				if(exp.getTyp().equals(Expression.Typ.CONSTANT)){
					// true -> fill in an empty clause
					// false -> return an empty cnf
					if(((ConstExpression) exp).getValue()){
						result.add(new Clause(new ArrayList<Term>()));
					} else {
						return new ArrayList<Clause>();
					}
				} else {
					// Symbol or ¬Symbol
					Boolean negated = false;
					SymbolExpression var;
					if(exp.getTyp().equals(Expression.Typ.NOT)){
						negated = true;
						var = (SymbolExpression) ((NotExpression) exp).getInnerExpression();
					} else {
						var = (SymbolExpression) exp;
					}
					Term term = new Term(var.getName(), negated);
					ArrayList<Term> collect = new ArrayList<Term>();
					collect.add(term);
					result.add(new Clause(collect));
				}
			}
		}
		return result;
	}

	private void deMorganNot() {
		Boolean flag = true;
        List<Expression> queue = new LinkedList<Expression>();
		while(flag){
			flag = false;
			queue.add(expression);
			while(!queue.isEmpty()){
				Expression exp = queue.remove(queue.size() - 1);
				if(exp.getTyp().equals(Expression.Typ.NOT)){
					Expression child = ((NotExpression) exp).getInnerExpression();
					if(child.getTyp().equals(Expression.Typ.NOT)){
						// from ¬¬a
						// to	a
						NotExpression var = (NotExpression) child;
						Expression newChild = var.getInnerExpression();
						replace(exp, newChild);
						exp = newChild;
						flag = true;
						queue.clear();break;
					} else
						if(child.getTyp().equals(Expression.Typ.CONSTANT)){
						// from ¬const
						// to	!const
						ConstExpression var = (ConstExpression) child;
						Expression newChild = new ConstExpression(!var.getValue());
						replace(exp, newChild);
						exp = newChild;
						flag = true;
						queue.clear();break;
					} else
						if(child.getTyp().equals(Expression.Typ.AND)){
						// from ¬(a & b)
						// to	(¬a | ¬b)
						AndExpression var =(AndExpression) child;
						ArrayList<Expression> grandChildren = new ArrayList<Expression>();
						for(Expression e : var.getExpressions()){
							grandChildren.add(new NotExpression(e));
						}
						Expression newChild = new OrExpression(grandChildren);
						replace(exp, newChild);
						exp = newChild;
						flag = true;
						queue.clear();break;
					} else if(child.getTyp().equals(Expression.Typ.OR)){
						// from ¬(a | b)
						// to	(¬a & ¬b)
						OrExpression var =(OrExpression) child;
						ArrayList<Expression> grandChildren = new ArrayList<Expression>();
						for(Expression e : var.getExpressions()){
							grandChildren.add(new NotExpression(e));
						}
						Expression newChild = new AndExpression(grandChildren);
						replace(exp, newChild);
						exp = newChild;
						flag = true;
						queue.clear();break;
					}
				}
				if(flag == false){
					queue.addAll(traverse(exp));
				}
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

	/**
	 * from a & b & c & d
	 * to	a & (b & (c & d))
	 *
	 */
	private void onlyBinaryFunctions() {
		List<Expression> queue = new LinkedList<Expression>();
		Boolean flag = true;
		while(flag){
			flag = false;
			queue.add(expression);
			while(!queue.isEmpty()){
				Expression exp = queue.remove(0);
				if(exp.getTyp().equals(Expression.Typ.AND)){
					List<Expression> children = traverse(exp);
					if(children.size() > 2){
						flag = true;
						while(children.size() > 2){
							Expression a = children.remove(children.size() - 1);
							Expression b = children.remove(children.size() - 1);
							children.add(new AndExpression(a, b));
						}
						replace(exp, new AndExpression(children.get(0), children.get(1)));
					}
				} else if(exp.getTyp().equals(Expression.Typ.OR)){
					List<Expression> children = traverse(exp);
					if(children.size() > 2){
						flag = true;
						while(children.size() > 2){
							Expression a = children.remove(children.size() - 1);
							Expression b = children.remove(children.size() - 1);
							children.add(new AndExpression(a, b));
						}
						replace(exp, new AndExpression(children.get(0), children.get(1)));
					}
				}
				if(flag == false){
					queue.addAll(traverse(exp));
				} else {
					queue.clear();
				}
			}
		}



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

	private Term unitClause(CnfExpression cnf, HashMap<String, Boolean> hashMap) {
		for(Clause clause : cnf.getClauses()){
			int counter = 0;
			Term term = null;
			for(Term literal : clause.getTerms()){
				Boolean map = hashMap.get(literal.getName());
				if(map == null){
					counter++;
					term = new Term(literal.getName(), !literal.isNegated());
				}
			}
			if(counter == 1){
				return term;
			}
		}



		return null;
	}

}