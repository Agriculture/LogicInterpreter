
import logicinterpreter.base.*;

public class Main
{
    public static void main(String[] args) 
    {
        //Zunächst wird ein Interpreter benötigt
        MyInterpreter interpreter=new MyInterpreter();
        
        //1. Möglichkeit: Anzeige im Editorfenster
        Controler.run(interpreter);

        //2.Möglichkeit: Steuerung über die Konsole
        /*
        try
        {
            //2.1. aus Datei, direkt aus String logischen Ausdruck parsen
            // oder zufällig erzeugen
            Expression expression=
                    Controler.parseFile("test.logic"); //a) Datei
                    //Controler.parseExpression("(A=>B)&(B=>C)&!(A=>C)"); //b) String
                    //Controler.parseExpression(Controler.random3CnfExpression(50, 200)); //c) zufälliges 3-Cnf-Problem
                    //Controler.parseExpression(Controler.randomGeneralExpression(10, 1)); //d) zufälliger genereller Ausdruck
         
            //2.2. Das geparste Ergebnis kann angezeigt werden
            System.out.println(expression);
            
            //2.3. die Ausgabe des Interpreters kann formatiert angezeigt werden
            Controler.outputResult(expression, interpreter);
        }
        catch(Exception ex)
        {
            System.err.println("Fehler: "+ex.getMessage());
            ex.printStackTrace(System.err);
        }
         */
    }
}
